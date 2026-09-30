-- PAT is a read-only outage reporting/analytics role.
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('USER', 'PAT', 'ADMIN', 'SUPERADMIN'));

ALTER TABLE access_requests DROP CONSTRAINT IF EXISTS access_requests_requested_role_check;
ALTER TABLE access_requests ADD CONSTRAINT access_requests_requested_role_check
    CHECK (requested_role IN ('USER', 'PAT', 'ADMIN', 'SUPERADMIN'));
