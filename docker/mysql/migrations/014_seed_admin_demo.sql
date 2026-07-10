SET NAMES utf8mb4;

INSERT INTO sys_organization (name, code, type, intro, contact, phone, join_status, status)
SELECT '未来职业培训中心', 'TRAIN_PENDING_001', 2, '申请加盟学分银行平台的培训机构', '张老师', '13800001111', 0, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_organization WHERE code = 'TRAIN_PENDING_001');

INSERT INTO forum_report (user_id, target_type, target_id, reason, status, create_time)
SELECT 2, 1, p.id, '内容疑似广告引流，请审核', 0, '2026-07-08 10:00:00'
FROM forum_post p
WHERE p.title = '简历项目经历怎么写？'
  AND NOT EXISTS (
    SELECT 1 FROM forum_report r
    WHERE r.user_id = 2 AND r.target_id = p.id AND r.reason LIKE '内容疑似广告%'
  );

INSERT INTO forum_report (user_id, target_type, target_id, reason, status, create_time)
SELECT 3, 1, p.id, '标题与内容不符，建议核查', 0, '2026-07-09 14:30:00'
FROM forum_post p
WHERE p.user_id = 2 AND p.title = 'Java 入门学习路线分享'
  AND NOT EXISTS (
    SELECT 1 FROM forum_report r
    WHERE r.user_id = 3 AND r.target_id = p.id AND r.reason LIKE '标题与内容不符%'
  );
