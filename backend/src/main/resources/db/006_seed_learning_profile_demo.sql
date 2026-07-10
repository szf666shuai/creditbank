-- 学习画像演示数据（student1 = user_id 2）
-- 可重复执行：先清理该用户的演示学习记录

DELETE FROM user_course WHERE user_id = 2 AND course_id IN (
  SELECT id FROM (SELECT id FROM course WHERE title LIKE '[测试]%') t
);
DELETE FROM learning_archive WHERE user_id = 2 AND title LIKE '[画像演示]%';
DELETE FROM learning_achievement WHERE user_id = 2 AND title LIKE '[画像演示]%';
DELETE FROM user_target_tag WHERE user_id = 2;
DELETE FROM credit_transaction WHERE user_id = 2 AND source LIKE '[画像演示]%';

-- 选课进度：Java 基础已完成，Spring Boot 学习中，Python 刚开始
INSERT INTO user_course (user_id, course_id, progress, status, paid_credit, start_time, complete_time)
SELECT 2, c.id, 100, 1, 20.00, DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)
FROM course c WHERE c.title = '[测试] Java 程序设计基础';

INSERT INTO user_course (user_id, course_id, progress, status, paid_credit, start_time, complete_time)
SELECT 2, c.id, 65, 0, 35.00, DATE_SUB(NOW(), INTERVAL 20 DAY), NULL
FROM course c WHERE c.title = '[测试] Spring Boot 与 Java 微服务实战';

INSERT INTO user_course (user_id, course_id, progress, status, paid_credit, start_time, complete_time)
SELECT 2, c.id, 15, 0, 15.00, DATE_SUB(NOW(), INTERVAL 5 DAY), NULL
FROM course c WHERE c.title = '[测试] Python 数据分析入门';

-- 学习档案
INSERT INTO learning_archive (user_id, title, archive_type, course_id, category, description, start_date, end_date, credit_earned, status)
SELECT 2, '[画像演示] 完成 Java 程序设计基础', 1, c.id, '编程基础',
       '系统学习 Java 语法、面向对象与集合框架，通过平台结业测验。',
       DATE_SUB(CURDATE(), INTERVAL 40 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 10.00, 1
FROM course c WHERE c.title = '[测试] Java 程序设计基础';

INSERT INTO learning_archive (user_id, title, archive_type, course_id, category, description, start_date, end_date, credit_earned, status)
SELECT 2, '[画像演示] Spring Boot 微服务实战（进行中）', 1, c.id, '后端开发',
       '正在学习 REST API 与微服务部署，已完成约 65%。',
       DATE_SUB(CURDATE(), INTERVAL 20 DAY), NULL, 0, 0
FROM course c WHERE c.title = '[测试] Spring Boot 与 Java 微服务实战';

INSERT INTO learning_archive (user_id, title, archive_type, category, description, start_date, end_date, credit_earned, status)
VALUES
(2, '[画像演示] 参加 Java 技术沙龙', 2, '活动实践',
 '参与企业工程师分享，了解 Spring Boot 项目经验与学分认定流程。',
 DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 5.00, 1);

-- 学习成果
INSERT INTO learning_achievement (user_id, title, type, credit_value, verify_status)
VALUES
(2, '[画像演示] Java 基础结业证书', 1, 10.00, 1),
(2, '[画像演示] 个人博客项目（Spring Boot）', 3, 8.00, 0);

-- 目标技能标签
INSERT INTO user_target_tag (user_id, tag_id, source)
SELECT 2, t.id, 'manual' FROM sys_tag t WHERE t.name IN ('Java', 'Spring Boot', '数据库');

-- 学分流水（与账户大致对齐的演示记录）
INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, create_time)
VALUES
(2, 1, 20.00, 20.00, 'register', '[画像演示] 注册奖励', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2, 1, 10.00, 30.00, 'complete_course', '[画像演示] 完成 Java 基础课程', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 5.00, 35.00, 'activity_checkin', '[画像演示] Java 技术沙龙签到', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 1, 3.00, 38.00, 'forum_post', '[画像演示] 论坛发帖奖励', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 1, 12.00, 50.00, 'daily_checkin', '[画像演示] 累计签到奖励', DATE_SUB(NOW(), INTERVAL 3 DAY));
