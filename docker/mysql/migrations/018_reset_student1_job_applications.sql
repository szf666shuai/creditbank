-- 清理学员测试投递数据，便于重新走投递/面试流程（幂等）
DELETE FROM interview_invitation
WHERE application_id IN (SELECT id FROM job_application WHERE user_id = 2)
   OR to_user_id = 2;

DELETE FROM user_message
WHERE msg_type = 2
  AND (to_user_id = 2 OR from_user_id = 2);

DELETE FROM job_application
WHERE user_id = 2;
