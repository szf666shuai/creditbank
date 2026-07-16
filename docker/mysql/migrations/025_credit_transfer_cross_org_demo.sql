SET NAMES utf8mb4;
INSERT INTO learning_archive (user_id, title, archive_type, course_id, category, description, start_date, end_date, credit_earned, status, transfer_status)
SELECT 2, c.title, 1, c.id, '学分转换演示', CONCAT('演示：已完成 ', c.title), CURDATE(), CURDATE(), IFNULL(c.credit_value, 3), 1, 0
FROM course c
WHERE c.deleted = 0 AND c.title IN (
  '[演示] Java 企业级开发实训',
  '[演示] Python 数据分析工作坊',
  '[演示] 高等数学与学分互认基础',
  '[演示] 数据结构跨校选修'
)
AND NOT EXISTS (
  SELECT 1 FROM learning_archive a WHERE a.user_id = 2 AND a.course_id = c.id AND IFNULL(a.deleted,0) = 0
);
INSERT INTO credit_transfer_rule (org_id, source_type, source_tags, target_type, target_course_id, target_org_id, credit_ratio, description, status)
SELECT 1, 1, 'AI,机器学习', 1,
  (SELECT id FROM course WHERE title = '[演示] 数据结构跨校选修' AND deleted = 0 LIMIT 1),
  1, 1.00, '[演示] 外部 AI/机器学习课程 → 本校数据结构跨校选修', 1
WHERE EXISTS (SELECT 1 FROM course WHERE title = '[演示] 数据结构跨校选修' AND deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM credit_transfer_rule WHERE org_id = 1 AND description LIKE '[演示] 外部 AI%');
INSERT INTO credit_transfer_rule (org_id, source_type, source_tags, target_type, target_course_id, target_org_id, credit_ratio, description, status)
SELECT 3, 1, 'AI,机器学习,Java,后端', 1,
  (SELECT id FROM course WHERE title = '[演示] Java 企业级开发实训' AND deleted = 0 LIMIT 1),
  3, 0.95, '[演示] 外部 AI/Java 课程 → 本机构 Java 企业级开发实训', 1
WHERE EXISTS (SELECT 1 FROM course WHERE title = '[演示] Java 企业级开发实训' AND deleted = 0)
  AND NOT EXISTS (SELECT 1 FROM credit_transfer_rule WHERE org_id = 3 AND description LIKE '[演示] 外部 AI/Java%');
