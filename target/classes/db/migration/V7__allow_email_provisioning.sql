-- Users can be invited by email before their first Entra sign-in. The
-- immutable oid is populated from the signed JWT on first sign-in.
ALTER TABLE users ALTER COLUMN auth_id DROP NOT NULL;
