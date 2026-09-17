-- Remove development-only transformer seed rows. Real DT records are now
-- expected to be populated through the Admin data-management screen.
DELETE FROM dt_master d
WHERE d.dt_code LIKE 'DT-MOCK-%'
  AND NOT EXISTS (SELECT 1 FROM dt_outages o WHERE o.dt_id = d.dt_id);
