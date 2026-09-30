-- Local email/password authentication is now the only identity provider.
-- User rows, roles, outage references, and audit history are preserved.
ALTER TABLE users DROP COLUMN IF EXISTS auth_id;
ALTER TABLE users DROP COLUMN IF EXISTS tenant_id;
