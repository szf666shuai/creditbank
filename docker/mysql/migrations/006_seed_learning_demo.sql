-- Demo learning data for student1 (user_id=2)
INSERT INTO learning_archive (user_id, title, archive_type, category, description, start_date, end_date, credit_earned, status)
SELECT 2, 'Java Programming Basics', 1, 'Computer Science', 'Completed core Java syntax and OOP modules.', '2026-01-10', '2026-03-15', 12.00, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_archive WHERE user_id = 2 AND title = 'Java Programming Basics');

INSERT INTO learning_archive (user_id, title, archive_type, category, description, start_date, end_date, credit_earned, status)
SELECT 2, 'Enterprise Open Day Visit', 2, 'Career', 'Participated in enterprise open day and career sharing session.', '2026-08-01', '2026-08-01', 5.00, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_archive WHERE user_id = 2 AND title = 'Enterprise Open Day Visit');

INSERT INTO learning_archive (user_id, title, archive_type, category, description, start_date, end_date, credit_earned, status)
SELECT 2, 'Microservices Project Practice', 3, 'Project', 'Team project on Spring Boot microservices.', '2026-04-01', NULL, 0.00, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_archive WHERE user_id = 2 AND title = 'Microservices Project Practice');

INSERT INTO learning_achievement (user_id, title, type, org_id, credit_value, file_url, verify_status, blockchain_hash)
SELECT 2, 'Java Developer Certificate', 1, 2, 20.00, 'https://example.com/certs/java-dev.pdf', 1, '0xabc123learning001'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_achievement WHERE user_id = 2 AND title = 'Java Developer Certificate');

INSERT INTO learning_achievement (user_id, title, type, org_id, credit_value, verify_status)
SELECT 2, 'Credit Bank Platform Course Completion', 2, 2, 15.00, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_achievement WHERE user_id = 2 AND title = 'Credit Bank Platform Course Completion');

INSERT INTO learning_achievement (user_id, title, type, org_id, credit_value, verify_status)
SELECT 2, 'Open Source Contribution Record', 3, NULL, 8.00, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_achievement WHERE user_id = 2 AND title = 'Open Source Contribution Record');
