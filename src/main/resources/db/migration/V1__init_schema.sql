-- Requires pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------
-- dt_master — source of truth for DT data (read-only to normal users)
-- ---------------------------------------------------------------------
CREATE TABLE dt_master (
    dt_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dt_code         VARCHAR(50) UNIQUE NOT NULL,
    dt_name         VARCHAR(200) NOT NULL,
    business_unit   VARCHAR(100) NOT NULL,
    undertaking     VARCHAR(100) NOT NULL,
    feeder          VARCHAR(100) NOT NULL,
    capacity_kva    NUMERIC(10,2) NOT NULL,
    supply_band     VARCHAR(20) NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_dt_master_search ON dt_master (dt_code, dt_name, business_unit, feeder);

-- ---------------------------------------------------------------------
-- users — app users, linked to Entra ID identity. Three-tier role model:
-- USER < ADMIN < SUPERADMIN, mirrored by Spring Security's RoleHierarchy.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_id         VARCHAR(200) UNIQUE,  -- Entra ID `oid`, bound on first sign-in
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(200) UNIQUE NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'USER'
                        CHECK (role IN ('USER', 'ADMIN', 'SUPERADMIN')),
    business_unit   VARCHAR(100),
    is_active       BOOLEAN NOT NULL DEFAULT FALSE, -- inert until an access_request is approved
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- fault_categories / challenge_categories — admin-editable lookups
-- ---------------------------------------------------------------------
CREATE TABLE fault_categories (
    category_id     SERIAL PRIMARY KEY,
    category_name   VARCHAR(100) UNIQUE NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT DEFAULT 0
);

CREATE TABLE challenge_categories (
    challenge_id    SERIAL PRIMARY KEY,
    challenge_name  VARCHAR(150) UNIQUE NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT DEFAULT 0
);

-- ---------------------------------------------------------------------
-- dt_outages — the core transactional table
-- ---------------------------------------------------------------------
CREATE TABLE dt_outages (
    outage_id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    outage_ref           VARCHAR(20) UNIQUE NOT NULL,
    dt_id                UUID NOT NULL REFERENCES dt_master(dt_id),

    -- Snapshot of DT attributes AT TIME OF REPORT — deliberate, so later
    -- corrections to dt_master don't rewrite historical outage records.
    dt_code_snapshot        VARCHAR(50) NOT NULL,
    business_unit_snapshot  VARCHAR(100) NOT NULL,
    undertaking_snapshot    VARCHAR(100) NOT NULL,
    feeder_snapshot         VARCHAR(100) NOT NULL,
    capacity_snapshot       NUMERIC(10,2) NOT NULL,
    band_snapshot           VARCHAR(20) NOT NULL,

    outage_date          DATE NOT NULL,
    outage_time          TIME NOT NULL,
    outage_datetime       TIMESTAMPTZ NOT NULL,

    fault_category_id    INT NOT NULL REFERENCES fault_categories(category_id),
    fault_description    TEXT NOT NULL,
    challenge_id          INT REFERENCES challenge_categories(challenge_id),
    additional_comment    TEXT,

    status                VARCHAR(20) NOT NULL DEFAULT 'OUT'
                              CHECK (status IN ('OUT', 'RESTORED')),

    restoration_date      DATE,
    restoration_time      TIME,
    restoration_datetime  TIMESTAMPTZ,
    restoration_remarks   TEXT,

    outage_duration_minutes INT,

    reported_by           UUID NOT NULL REFERENCES users(user_id),
    reported_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    restored_by            UUID REFERENCES users(user_id),
    restored_at             TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_restoration_after_outage
        CHECK (restoration_date IS NULL OR
               (restoration_date + restoration_time) >= (outage_date + outage_time))
);

-- PostgreSQL generated columns require immutable expressions. Converting a
-- date/time pair to timestamptz depends on timezone settings, so maintain the
-- derived values in a trigger with an explicit UTC interpretation instead.
CREATE OR REPLACE FUNCTION set_outage_derived_values()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.outage_datetime := (NEW.outage_date + NEW.outage_time) AT TIME ZONE 'UTC';

    IF NEW.restoration_date IS NOT NULL AND NEW.restoration_time IS NOT NULL THEN
        NEW.restoration_datetime :=
            (NEW.restoration_date + NEW.restoration_time) AT TIME ZONE 'UTC';
        NEW.outage_duration_minutes := EXTRACT(EPOCH FROM (
            (NEW.restoration_date + NEW.restoration_time)
            - (NEW.outage_date + NEW.outage_time)
        ))::INT / 60;
    ELSE
        NEW.restoration_datetime := NULL;
        NEW.outage_duration_minutes := NULL;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_set_outage_derived_values
    BEFORE INSERT OR UPDATE ON dt_outages
    FOR EACH ROW
    EXECUTE FUNCTION set_outage_derived_values();

-- THE critical rule: only one active (status = 'OUT') outage per DT at a time.
-- Atomic and race-condition-proof — this is what makes the duplicate check
-- bulletproof, not the app-layer pre-check the UI also does for UX.
CREATE UNIQUE INDEX idx_one_active_outage_per_dt
    ON dt_outages (dt_id)
    WHERE status = 'OUT';

CREATE INDEX idx_outages_status ON dt_outages (status);
CREATE INDEX idx_outages_bu ON dt_outages (business_unit_snapshot);
CREATE INDEX idx_outages_dt ON dt_outages (dt_id);

-- ---------------------------------------------------------------------
-- outage_audit_log — full audit trail, append-only
-- ---------------------------------------------------------------------
CREATE TABLE outage_audit_log (
    log_id        BIGSERIAL PRIMARY KEY,
    outage_id     UUID NOT NULL REFERENCES dt_outages(outage_id),
    action         VARCHAR(30) NOT NULL, -- REPORTED | RESTORED | EDITED | REOPENED
    performed_by   UUID NOT NULL REFERENCES users(user_id),
    performed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    old_values     JSONB,
    new_values     JSONB
);

-- ---------------------------------------------------------------------
-- access_requests — UAR-style profiling workflow for provisioning
-- USER / ADMIN / SUPERADMIN accounts, modeled on the UAR-form pattern.
-- ---------------------------------------------------------------------
CREATE TABLE access_requests (
    request_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requested_by         UUID REFERENCES users(user_id), -- null if requester has no account yet
    requester_name        VARCHAR(150) NOT NULL,
    requester_email       VARCHAR(200) NOT NULL,
    requested_role        VARCHAR(20) NOT NULL
                              CHECK (requested_role IN ('USER', 'ADMIN', 'SUPERADMIN')),
    business_unit         VARCHAR(100) NOT NULL,
    reference_user_id     UUID REFERENCES users(user_id), -- profile being cloned
    justification         TEXT,
    status                 VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by            UUID REFERENCES users(user_id),
    approved_at             TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_access_requests_status ON access_requests (status);

-- ---------------------------------------------------------------------
-- access_audit_log — immutable trail of who approved/rejected what
-- ---------------------------------------------------------------------
CREATE TABLE access_audit_log (
    log_id        BIGSERIAL PRIMARY KEY,
    request_id    UUID NOT NULL REFERENCES access_requests(request_id),
    action         VARCHAR(30) NOT NULL, -- SUBMITTED | APPROVED | REJECTED | ESCALATED
    performed_by   UUID REFERENCES users(user_id),
    performed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    notes          TEXT
);

-- Audit trails are append-only, even for admins, at the DB layer.
REVOKE DELETE ON outage_audit_log FROM PUBLIC;
REVOKE DELETE ON access_audit_log FROM PUBLIC;

-- ---------------------------------------------------------------------
-- Reporting view — feeds Power BI directly
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW vw_outage_report AS
SELECT
    o.outage_id, o.outage_ref, o.dt_code_snapshot, o.business_unit_snapshot,
    o.undertaking_snapshot, o.feeder_snapshot, o.capacity_snapshot, o.band_snapshot,
    fc.category_name AS fault_category, o.fault_description,
    cc.challenge_name AS restoration_challenge, o.additional_comment,
    o.status, o.outage_datetime, o.restoration_datetime,
    o.outage_duration_minutes,
    ROUND(o.outage_duration_minutes / 60.0, 1) AS outage_duration_hours,
    CASE WHEN o.status = 'OUT'
         THEN EXTRACT(DAY FROM (now() - o.outage_datetime))
         ELSE EXTRACT(DAY FROM (o.restoration_datetime - o.outage_datetime))
    END AS age_days,
    reporter.full_name AS reported_by_name,
    restorer.full_name AS restored_by_name,
    o.reported_at, o.restored_at
FROM dt_outages o
JOIN fault_categories fc ON fc.category_id = o.fault_category_id
LEFT JOIN challenge_categories cc ON cc.challenge_id = o.challenge_id
JOIN users reporter ON reporter.user_id = o.reported_by
LEFT JOIN users restorer ON restorer.user_id = o.restored_by;

CREATE OR REPLACE VIEW vw_current_outages AS
SELECT * FROM vw_outage_report WHERE status = 'OUT';
