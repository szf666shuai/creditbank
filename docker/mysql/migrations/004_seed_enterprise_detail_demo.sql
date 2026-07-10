INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status) VALUES
(2, 3, 'Java Developer', 'Backend development for credit bank platform', 'Spring Boot, MySQL, Redis', '15k-25k', 'Shanghai', 'Bachelor', 1),
(2, 3, 'Frontend Developer', 'Vue3 frontend development', 'Vue3, TypeScript, Element Plus', '12k-20k', 'Shanghai', 'Bachelor', 1);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status) VALUES
(2, 3, 'Open Day', 'Visit our company and learn about culture', 'Shanghai Pudong', '2026-08-01 09:00:00', '2026-08-01 17:00:00', 50, 10.00, 1),
(2, 3, 'Spring Boot Meetup', 'Microservices and Spring Boot 3 practice', 'Online', '2026-08-15 14:00:00', '2026-08-15 16:00:00', 200, 5.00, 1);

INSERT INTO org_material (org_id, publisher_id, title, description, file_url, material_type, status) VALUES
(2, 3, 'Company Brochure', 'Official company introduction', 'https://example.com/files/intro.pdf', 1, 1),
(2, 3, 'Tech Stack Guide', 'Technology stack and dev standards', 'https://example.com/files/tech-stack.pdf', 1, 1);
