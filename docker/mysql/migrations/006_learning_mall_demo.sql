-- 学习资源与积分商城演示数据，可重复执行
SET NAMES utf8mb4;

INSERT IGNORE INTO sys_tag (name, category) VALUES
('Java', 'skill'),
('C++', 'skill'),
('Python', 'skill'),
('Spring Boot', 'skill'),
('区块链', 'skill'),
('数据库', 'skill');

INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '课程资源', 0, 10, 1
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '课程资源');

INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '学习用品', 0, 20, 1
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '学习用品');

INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '虚拟权益', 0, 30, 1
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '虚拟权益');

INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '技术服务', 0, 40, 1
WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '技术服务');

DELETE FROM course_tag WHERE course_id IN (
  SELECT id FROM course WHERE title LIKE '[学习资源]%'
);
DELETE FROM course WHERE title LIKE '[学习资源]%';

INSERT INTO course (org_id, publisher_id, title, description, cover_url, price_credit, price_money, duration_hours, credit_reward, status) VALUES
(3, 3, '[学习资源] Java 后端开发入门', '从 Java 基础、面向对象、集合到 Spring Boot REST API，完成后生成区块链校验证书。', NULL, 0, 0, 18.0, 10.00, 1),
(3, 3, '[学习资源] C++ 算法与数据结构', '面向工程实践的 C++ 语法、STL、复杂度分析和常见算法训练。', NULL, 0, 0, 20.0, 12.00, 1),
(2, 3, '[学习资源] 区块链证书校验实践', '理解学习成果存证、证书哈希、二维码校验和可信档案流转。', NULL, 0, 0, 8.0, 8.00, 1);

INSERT INTO course_tag (course_id, tag_id)
SELECT c.id, t.id FROM course c JOIN sys_tag t ON t.name IN ('Java', 'Spring Boot')
WHERE c.title = '[学习资源] Java 后端开发入门';

INSERT INTO course_tag (course_id, tag_id)
SELECT c.id, t.id FROM course c JOIN sys_tag t ON t.name = 'C++'
WHERE c.title = '[学习资源] C++ 算法与数据结构';

INSERT INTO course_tag (course_id, tag_id)
SELECT c.id, t.id FROM course c JOIN sys_tag t ON t.name = '区块链'
WHERE c.title = '[学习资源] 区块链证书校验实践';

DELETE FROM mall_product WHERE name LIKE '[积分商城]%';

INSERT INTO mall_product (category_id, name, description, cover_url, product_type, ref_course_id, price_credit, price_money, stock, status)
SELECT id, '[积分商城] Java 电子书兑换券', '兑换 Java 核心技术电子书，适合课程配套学习。', NULL, 2, NULL, 8.00, 0.00, 100, 1
FROM mall_category WHERE name = '课程资源';

INSERT INTO mall_product (category_id, name, description, cover_url, product_type, ref_course_id, price_credit, price_money, stock, status)
SELECT id, '[积分商城] C++ 刷题会员月卡', '一个月算法刷题会员权益，支持虚拟权益兑换。', NULL, 2, NULL, 18.00, 0.00, 80, 1
FROM mall_category WHERE name = '虚拟权益';

INSERT INTO mall_product (category_id, name, description, cover_url, product_type, ref_course_id, price_credit, price_money, stock, status)
SELECT id, '[积分商城] 程序员键盘垫', '学习用品实物兑换，订单会进入个人中心订单记录。', NULL, 1, NULL, 25.00, 0.00, 30, 1
FROM mall_category WHERE name = '学习用品';

INSERT INTO mall_product (category_id, name, description, cover_url, product_type, ref_course_id, price_credit, price_money, stock, status)
SELECT id, '[积分商城] 简历与学习档案诊断', '由企业导师提供一次学习档案和技术简历诊断服务。', NULL, 4, NULL, 35.00, 0.00, 20, 1
FROM mall_category WHERE name = '技术服务';
