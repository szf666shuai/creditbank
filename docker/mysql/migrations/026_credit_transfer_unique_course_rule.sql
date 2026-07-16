-- 清理同机构同课程的重复启用规则，仅保留最小 id
DELETE r1 FROM credit_transfer_rule r1
INNER JOIN credit_transfer_rule r2
  ON r1.org_id = r2.org_id
 AND r1.target_course_id = r2.target_course_id
 AND r1.target_type = 1
 AND r2.target_type = 1
 AND r1.status = 1
 AND r2.status = 1
 AND r1.target_course_id IS NOT NULL
 AND r1.id > r2.id;
