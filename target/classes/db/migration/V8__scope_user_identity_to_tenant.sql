-- Entra object IDs are only unique within a tenant. Existing rows were
-- created in the original Gridline tenant and are assigned that tenant ID.
ALTER TABLE users ADD COLUMN tenant_id VARCHAR(36);
UPDATE users
SET tenant_id = '503085e6-8bb7-44c4-86bf-319a33b22534'
WHERE tenant_id IS NULL AND auth_id IS NOT NULL;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_auth_id_key;
CREATE UNIQUE INDEX uq_users_tenant_auth_id ON users (tenant_id, auth_id);
