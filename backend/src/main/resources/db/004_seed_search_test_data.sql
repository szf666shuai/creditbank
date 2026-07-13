-- 搜索功能测试数据（含 Java 相关课程、活动等）
-- 可重复执行：先按标题前缀清理再插入

DELETE FROM course_tag WHERE course_id IN (SELECT id FROM (SELECT id FROM course WHERE title LIKE '[测试]%') t);
DELETE FROM course WHERE title LIKE '[测试]%';
DELETE FROM activity WHERE title LIKE '[测试]%';
DELETE FROM job_posting WHERE title LIKE '[测试]%';
DELETE FROM policy_news WHERE title LIKE '[测试]%';
DELETE FROM mall_product WHERE name LIKE '[测试]%';
DELETE FROM org_material WHERE title LIKE '[测试]%';

-- 课程（enterprise1=3, 示例科技集团=2, 培训机构=3）
INSERT INTO course (org_id, publisher_id, title, description, cover_url, price_credit, duration_hours, credit_reward, status) VALUES
(2, 3, '[测试] Java 程序设计基础', '面向零基础学员，系统学习 Java 语法、面向对象与集合框架，适合学分银行入门课程。', 'https://picsum.photos/seed/java-course-1/400/250', 20.00, 24.0, 10.00, 1),
(3, 3, '[测试] Spring Boot 与 Java 微服务实战', '基于 Spring Boot 构建 REST API 与微服务，涵盖 Java Web 开发与部署实践。', 'https://picsum.photos/seed/java-course-2/400/250', 35.00, 32.0, 15.00, 1),
(3, 3, '[测试] Python 数据分析入门', '使用 Pandas 进行数据清洗与可视化，与 Java 后端形成对比学习路径。', 'https://picsum.photos/seed/python-course/400/250', 15.00, 18.0, 8.00, 1);

INSERT INTO course_tag (course_id, tag_id)
SELECT c.id, t.id FROM course c, sys_tag t
WHERE c.title = '[测试] Java 程序设计基础' AND t.name = 'Java';

INSERT INTO course_tag (course_id, tag_id)
SELECT c.id, t.id FROM course c, sys_tag t
WHERE c.title = '[测试] Spring Boot 与 Java 微服务实战' AND t.name IN ('Java', 'Spring Boot');

-- 活动
INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status) VALUES
(2, 3, '[测试] Java 技术沙龙：Spring Boot 实践', '企业工程师分享 Java 项目经验，主题包含 Spring Boot 开发与学分认定流程。', '示例科技集团报告厅', '2026-08-15 14:00:00', '2026-08-15 17:00:00', 80, 5.00, 1),
(2, 3, '[测试] 校园编程马拉松 Java 赛道', '48 小时编程挑战，Java 组题目涵盖算法与工程实现，优胜者奖励学分。', '大学城创新中心', '2026-09-01 09:00:00', '2026-09-03 18:00:00', 120, 10.00, 1),
(1, 1, '[测试] 区块链技术分享会', '介绍联盟链在学习成果存证中的应用，不涉及 Java 编程。', '示例大学图书馆', '2026-08-20 15:00:00', '2026-08-20 17:00:00', 60, 3.00, 1);

-- 论坛帖子（student1=2）：按标题幂等插入，避免重启后 ID 变化导致详情 404
INSERT INTO forum_post (board_id, user_id, title, content, status)
SELECT 1, 2, '[测试] Java 学习路线求助', '请问学分银行平台上有没有推荐的 Java 入门课程？想先从基础语法学起。', 1
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE title = '[测试] Java 学习路线求助' AND deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, status)
SELECT 3, 2, '[测试] 春招 Java 后端面经', '分享一下 Java 后端岗位面试题，包括 JVM、Spring、MySQL 等。', 1
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE title = '[测试] 春招 Java 后端面经' AND deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, status)
SELECT 2, 2, '[测试] 转让二手显示器', '毕业出显示器，有意私信联系我。', 1
WHERE NOT EXISTS (SELECT 1 FROM forum_post WHERE title = '[测试] 转让二手显示器' AND deleted = 0);

-- 招聘
INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status) VALUES
(2, 3, '[测试] Java 后端开发工程师', '负责学分银行平台后端模块开发，技术栈以 Java / Spring Boot 为主。', '熟悉 Java、Spring Boot、MySQL；有微服务经验优先。', '12K-20K', '深圳', '本科', 1),
(2, 3, '[测试] 前端开发工程师', '负责 Vue3 前端页面开发，与 Java 团队协作。', '熟悉 Vue3、TypeScript、Element Plus。', '10K-18K', '深圳', '本科', 1);

-- 资讯
INSERT INTO policy_news (title, content, source, author, status, publish_time) VALUES
('[测试] 关于 Java 类课程学分认定指引', '为保障 Java 等编程类学习成果认定规范，平台发布课程学分映射标准说明……', '学分银行管委会', '教务处', 1, '2026-07-01 10:00:00'),
('[测试] 2026 年暑期实践活动通知', '鼓励学员参与企业实践活动，含编程马拉松、技术沙龙等。', '平台公告', '运营中心', 1, '2026-06-20 09:00:00');

-- 积分商城商品（category 课程资源=1）
INSERT INTO mall_product (category_id, name, description, cover_url, product_type, price_credit, status) VALUES
(1, '[测试] Java 电子书兑换券', '可兑换一本 Java 核心技术电子书，适合备考与自学。', 'https://picsum.photos/seed/java-mall-1/400/250', 2, 8.00, 1),
(1, '[测试] Spring Boot 实战课程包', '包含视频课程与实验手册的 Java 微服务学习包。', 'https://picsum.photos/seed/java-mall-2/400/250', 3, 25.00, 1),
(2, '[测试] 程序员键盘垫', '印有 Java 咖啡杯图案的桌面配件。', 'https://picsum.photos/seed/java-mall-3/400/250', 1, 5.00, 1);

-- 企业学习资料
INSERT INTO org_material (org_id, publisher_id, title, description, file_url, material_type, status) VALUES
(2, 3, '[测试] Java 编码规范手册', '企业内部 Java 开发规范与 Code Review 清单，供学员参考学习。', '/files/materials/java-coding-guide.pdf', 1, 1),
(2, 3, '[测试] Spring Boot 项目模板', '可快速启动的 Java 微服务项目骨架与部署说明。', '/files/materials/springboot-template.zip', 1, 1);
