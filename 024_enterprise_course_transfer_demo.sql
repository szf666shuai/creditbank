-- 企业课程 / 学分转换演示数据（dev/jyw）
SET NAMES utf8mb4;

-- 1) 为本机构(示例科技集团 org_id=2)课程补标签与奖励，便于概览与规则演示
UPDATE course
SET tags = CASE
        WHEN title LIKE '%Java%' OR title LIKE '%Spring%' THEN 'Java,后端,编程'
        WHEN title LIKE '%Python%' OR title LIKE '%FastAPI%' THEN 'Python,数据分析'
        WHEN title LIKE '%C++%' OR title LIKE '%C＋＋%' THEN 'C++,算法'
        WHEN title LIKE '%视觉%' OR title LIKE '%NLP%' OR title LIKE '%自然语言%' OR title LIKE '%生成式%' OR title LIKE '%人工%' THEN 'AI,机器学习'
        WHEN title LIKE '%区块链%' OR title LIKE '%Fabric%' THEN '区块链,分布式'
        ELSE IFNULL(NULLIF(tags, ''), '通用,职业技能')
    END,
    credit_reward = IF(IFNULL(credit_reward, 0) = 0, 8.00, credit_reward),
    credit_value = IFNULL(credit_value, 3.00),
    difficulty = IFNULL(difficulty, 2),
    duration_minutes = IFNULL(duration_minutes, 90),
    approval_status = 1,
    status = 1,
    publisher_id = IFNULL(publisher_id, 3)
WHERE org_id = 2 AND deleted = 0;

-- 2) 各加盟机构补充演示课程（幂等：按标题防重）
INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 1, 3, '[演示] 高等数学与学分互认基础', '面向跨校互认的数学基础课程，完成后可申请转换。',
       'https://picsum.photos/seed/org1-math/640/360', '/videos/spring-boot.mp4', 1800,
       2.0, 10.00, 4.00, '数学,基础课,互认', 2, 120, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 高等数学与学分互认基础' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 1, 3, '[演示] 数据结构跨校选修', '高校开放选修课，标签匹配后可转换至科技集团等效课程。',
       'https://picsum.photos/seed/org1-ds/640/360', '/videos/spring-boot.mp4', 2400,
       2.5, 12.00, 3.50, '数据结构,算法,编程', 3, 150, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 数据结构跨校选修' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 3, 3, '[演示] Java 企业级开发实训', '培训机构 Java 实训课，可按标签申请转换到科技集团课程学分。',
       'https://picsum.photos/seed/org3-java/640/360', '/videos/spring-boot.mp4', 3600,
       3.0, 15.00, 4.00, 'Java,后端,编程,实训', 3, 180, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] Java 企业级开发实训' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 3, 3, '[演示] Python 数据分析工作坊', '短期工作坊，匹配 Python/数据分析标签转换规则。',
       'https://picsum.photos/seed/org3-py/640/360', '/videos/spring-boot.mp4', 2700,
       1.5, 9.00, 2.50, 'Python,数据分析', 2, 90, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] Python 数据分析工作坊' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 6, 3, '[演示] 星河学院 · 云计算运维', '职业院校云运维课程，演示跨机构转换。',
       'https://picsum.photos/seed/org4-cloud/640/360', '/videos/spring-boot.mp4', 3000,
       2.0, 11.00, 3.00, '云计算,运维,Linux', 2, 120, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 星河学院 · 云计算运维' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 7, 3, '[演示] 云启 · 前端工程化实战', '培训机构前端课程，标签：前端,工程化,JavaScript。',
       'https://picsum.photos/seed/org5-fe/640/360', '/videos/spring-boot.mp4', 3200,
       2.5, 10.00, 3.00, '前端,工程化,JavaScript', 2, 140, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 云启 · 前端工程化实战' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 8, 3, '[演示] 江城开大 · 终身学习导论', '开放大学通识课，可用于通用技能标签匹配。',
       'https://picsum.photos/seed/org6-ll/640/360', '/videos/spring-boot.mp4', 1500,
       1.0, 6.00, 2.00, '通识,终身学习,通用', 1, 60, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 江城开大 · 终身学习导论' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 2, 3, '[演示] 科技集团 · Java 后端认证课', '本机构目标课程：接受外部 Java/后端标签转换。',
       'https://picsum.photos/seed/org2-java-cert/640/360', '/videos/spring-boot.mp4', 4000,
       3.5, 16.00, 5.00, 'Java,后端,认证', 3, 200, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · Java 后端认证课' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 2, 3, '[演示] 科技集团 · AI 应用实践课', '本机构目标课程：接受 AI/机器学习标签转换。',
       'https://picsum.photos/seed/org2-ai/640/360', '/videos/spring-boot.mp4', 3600,
       3.0, 14.00, 4.00, 'AI,机器学习,实践', 3, 180, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · AI 应用实践课' AND deleted = 0);

INSERT INTO course (
  org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
  duration_hours, credit_reward, credit_value, tags, difficulty, duration_minutes,
  status, approval_status, deleted
)
SELECT 2, 3, '[演示] 科技集团 · 数据分析入门课', '本机构目标课程：接受 Python/数据分析标签转换。',
       'https://picsum.photos/seed/org2-data/640/360', '/videos/spring-boot.mp4', 2400,
       2.0, 10.00, 3.00, 'Python,数据分析', 2, 100, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · 数据分析入门课' AND deleted = 0);

-- 3) 示例科技集团（org_id=2）学分转换/互认规则
DELETE FROM credit_transfer_rule WHERE org_id = 2 AND description LIKE '[演示]%';

INSERT INTO credit_transfer_rule (
  org_id, source_type, source_course_id, source_tags,
  target_type, target_course_id, target_certificate_id, target_achievement_id, target_org_id,
  credit_ratio, description, status
)
SELECT
  2, 1, NULL, 'Java,后端,编程',
  1,
  (SELECT id FROM course WHERE title = '[演示] 科技集团 · Java 后端认证课' AND deleted = 0 LIMIT 1),
  NULL, NULL, 2,
  1.00, '[演示] 外部 Java/后端课程 → 本机构 Java 后端认证课', 1
WHERE EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · Java 后端认证课' AND deleted = 0);

INSERT INTO credit_transfer_rule (
  org_id, source_type, source_course_id, source_tags,
  target_type, target_course_id, target_certificate_id, target_achievement_id, target_org_id,
  credit_ratio, description, status
)
SELECT
  2, 1, NULL, 'AI,机器学习',
  1,
  (SELECT id FROM course WHERE title = '[演示] 科技集团 · AI 应用实践课' AND deleted = 0 LIMIT 1),
  NULL, NULL, 2,
  0.90, '[演示] 外部 AI/机器学习课程 → 本机构 AI 应用实践课（0.9 倍）', 1
WHERE EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · AI 应用实践课' AND deleted = 0);

INSERT INTO credit_transfer_rule (
  org_id, source_type, source_course_id, source_tags,
  target_type, target_course_id, target_certificate_id, target_achievement_id, target_org_id,
  credit_ratio, description, status
)
SELECT
  2, 1, NULL, 'Python,数据分析',
  1,
  (SELECT id FROM course WHERE title = '[演示] 科技集团 · 数据分析入门课' AND deleted = 0 LIMIT 1),
  NULL, NULL, 2,
  1.00, '[演示] 外部 Python/数据分析课程 → 本机构数据分析入门课', 1
WHERE EXISTS (SELECT 1 FROM course WHERE title = '[演示] 科技集团 · 数据分析入门课' AND deleted = 0);

INSERT INTO credit_transfer_rule (
  org_id, source_type, source_course_id, source_tags,
  target_type, target_course_id, target_certificate_id, target_achievement_id, target_org_id,
  credit_ratio, description, status
)
VALUES
  (2, 2, NULL, '证书,职业资格', 2, NULL, NULL, NULL, 2, 0.80,
   '[演示] 外部职业资格类学习成果 → 本机构等效成果（0.8 倍）', 1);

INSERT INTO credit_transfer_rule (
  org_id, source_type, source_course_id, source_tags,
  target_type, target_course_id, target_certificate_id, target_achievement_id, target_org_id,
  credit_ratio, description, status
)
SELECT
  2, 1, NULL, '通识,终身学习,通用',
  1,
  (SELECT id FROM course WHERE org_id = 2 AND deleted = 0 AND status = 1 ORDER BY id DESC LIMIT 1),
  NULL, NULL, 2,
  0.70, '[演示] 通识/终身学习类课程 → 本机构课程（0.7 倍）', 1;
