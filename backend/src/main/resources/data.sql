INSERT INTO student (roll_number, name, email, department, semester)
SELECT demo.roll_number, demo.name, demo.email, demo.department, demo.semester
FROM (VALUES
    ('24CSE1001', 'Avery Rowan', 'avery.rowan@example.test', 'Computer Science', 2),
    ('23CSE1042', 'Jordan Vale', 'jordan.vale@example.test', 'Computer Science', 4),
    ('22ECE2017', 'Morgan Reed', 'morgan.reed@example.test', 'Electronics Engineering', 6),
    ('21ECE2033', 'Taylor Quinn', 'taylor.quinn@example.test', 'Electronics Engineering', 8),
    ('24ME3011', 'Riley Parker', 'riley.parker@example.test', 'Mechanical Engineering', 2),
    ('23ME3048', 'Casey Ellis', 'casey.ellis@example.test', 'Mechanical Engineering', 4),
    ('22CE4012', 'Jamie Blair', 'jamie.blair@example.test', 'Civil Engineering', 6),
    ('21CE4065', 'Drew Harper', 'drew.harper@example.test', 'Civil Engineering', 8),
    ('24BBA5019', 'Skyler Brooks', 'skyler.brooks@example.test', 'Business Administration', 2),
    ('23BIO6014', 'Emerson Lane', 'emerson.lane@example.test', 'Biotechnology', 4)
) AS demo(roll_number, name, email, department, semester)
WHERE NOT EXISTS (
    SELECT 1
    FROM student existing
    WHERE existing.roll_number = demo.roll_number
);