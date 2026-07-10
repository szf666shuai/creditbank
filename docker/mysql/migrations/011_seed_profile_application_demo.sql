-- Demo job applications with varied status for student1 (user_id=2)
UPDATE job_application ja
JOIN job_posting jp ON ja.job_id = jp.id
SET ja.status = 2, ja.update_time = '2026-07-05 10:00:00'
WHERE ja.user_id = 2 AND jp.title = 'Java Developer'
  AND ja.status = 0;

INSERT INTO job_application (job_id, user_id, cover_message, status, create_time, update_time)
SELECT jp.id, 2, 'I have Vue3 and TypeScript experience and hope to join the frontend team.', 1, '2026-06-20 14:30:00', '2026-06-22 09:15:00'
FROM job_posting jp
WHERE jp.org_id = 2 AND jp.title = 'Go Developer'
  AND NOT EXISTS (SELECT 1 FROM job_application ja WHERE ja.job_id = jp.id AND ja.user_id = 2);
