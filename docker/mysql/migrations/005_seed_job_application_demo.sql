-- Demo job application: student1 applies to enterprise1 Java job
INSERT INTO job_application (job_id, user_id, cover_message, status)
SELECT jp.id, 2, 'I am interested in Java backend development and would like to join your team.', 0
FROM job_posting jp
WHERE jp.org_id = 2 AND jp.title = 'Java Developer'
  AND NOT EXISTS (
      SELECT 1 FROM job_application ja WHERE ja.job_id = jp.id AND ja.user_id = 2
  )
LIMIT 1;
