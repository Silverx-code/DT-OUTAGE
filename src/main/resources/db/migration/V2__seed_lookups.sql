INSERT INTO fault_categories (category_name, sort_order) VALUES
    ('Failed DT', 1),
    ('Ruptured Fuse', 2),
    ('Cable Fault', 3),
    ('Vandalism', 4),
    ('Oil Leakage', 5),
    ('Vehicular Collision', 6),
    ('Open Circuit', 7),
    ('Low Voltage', 8),
    ('Low Insulation', 9),
    ('Broken Pole', 10),
    ('Plinth Reconstruction', 11),
    ('Other', 99);

INSERT INTO challenge_categories (challenge_name, sort_order) VALUES
    ('No replacement DT available', 1),
    ('No fuse/material available', 2),
    ('Transportation issue', 3),
    ('Manpower unavailable', 4),
    ('Funding/approval delay', 5),
    ('Access/location issue', 6),
    ('Security issue', 7),
    ('Customer-related issue', 8),
    ('Awaiting technical team', 9),
    ('Other', 99);
