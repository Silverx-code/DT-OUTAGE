-- Durable, database-owned allocation prevents duplicate outage references
-- across restarts and horizontally scaled application instances.
CREATE SEQUENCE outage_ref_sequence START WITH 1 INCREMENT BY 1;
