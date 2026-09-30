-- Temporary demo transformer directory. Remove with a future migration after
-- the real DT master list has been imported.
INSERT INTO dt_master (
    dt_code, dt_name, business_unit, undertaking, feeder,
    capacity_kva, supply_band, is_active
)
SELECT
    'DT-DEMO-' || lpad(n::text, 3, '0'),
    locations[((n - 1) % array_length(locations, 1)) + 1]
        || ' Transformer ' || (((n - 1) / array_length(locations, 1)) + 1),
    business_units[((n - 1) % array_length(business_units, 1)) + 1],
    undertakings[((n - 1) % array_length(undertakings, 1)) + 1],
    feeders[((n - 1) % array_length(feeders, 1)) + 1]
        || ' Feeder ' || (((n - 1) % 4) + 1),
    capacities[((n - 1) % array_length(capacities, 1)) + 1],
    bands[((n - 1) % array_length(bands, 1)) + 1],
    TRUE
FROM generate_series(1, 100) AS series(n)
CROSS JOIN (
    SELECT
        ARRAY['Ikeja', 'Yaba', 'Surulere', 'Ojota', 'Maryland', 'Alausa', 'Oshodi', 'Agege', 'Ogba', 'Gbagada']::text[] AS locations,
        ARRAY['Ikeja BU', 'Lagos Mainland BU', 'Lagos Island BU', 'Eko BU']::text[] AS business_units,
        ARRAY['Alausa', 'Opebi', 'Allen', 'Anthony', 'Fadeyi', 'Palmgrove', 'Yaba', 'Shomolu', 'Ojota', 'Ogba']::text[] AS undertakings,
        ARRAY['Ikeja North', 'Alausa', 'Opebi', 'Yaba Central', 'Anthony', 'Maryland', 'Oshodi', 'Agege']::text[] AS feeders,
        ARRAY[100.00, 200.00, 300.00, 500.00, 750.00, 1000.00]::numeric[] AS capacities,
        ARRAY['A', 'B', 'C']::text[] AS bands
) AS seed_values
ON CONFLICT (dt_code) DO NOTHING;
