-- Ensure every forum board and information category has enough demo records.

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：课程学习打卡互助',
       '大家可以在这里记录每日学习进度，也可以互相推荐适合学分认定的课程资料。',
       36, 0, 4, 1, '2026-07-12 09:00:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园频道：课程学习打卡互助' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：数据库课程笔记交换',
       '整理了一份 MySQL 索引和事务笔记，想和正在学习数据库课程的同学互相补充。',
       28, 0, 3, 1, '2026-07-12 09:20:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园频道：数据库课程笔记交换' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：学习资源推荐清单',
       '欢迎补充 Java、前端、数据库、人工智能方向的入门资料，方便新同学快速开始。',
       41, 0, 6, 1, '2026-07-12 09:40:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园频道：学习资源推荐清单' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：线上学习小组招募',
       '计划每周一起完成一次课程复盘，目标是月底前完成 Spring Boot 项目实践。',
       33, 0, 5, 1, '2026-07-12 10:00:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园频道：线上学习小组招募' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：成果展示页面怎么写',
       '想把课程项目沉淀到学习成果里，大家一般怎么描述项目背景、技术栈和个人贡献？',
       25, 0, 2, 1, '2026-07-12 10:20:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园频道：成果展示页面怎么写' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：转让机械键盘',
       '九成新机械键盘，适合写代码和做课程作业，校园内可面交。',
       30, 0, 4, 1, '2026-07-12 10:40:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园集市：转让机械键盘' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：求二手显示器',
       '想收一台 24 寸显示器，用来做前端页面和后端接口联调，有闲置的同学可以联系。',
       22, 0, 2, 1, '2026-07-12 11:00:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园集市：求二手显示器' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：课程资料打印拼单',
       '准备打印 Java 和数据库复习资料，有同学一起拼单可以降低成本。',
       19, 0, 2, 1, '2026-07-12 11:20:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园集市：课程资料打印拼单' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：项目答辩演示设备借用',
       '下周项目展示需要投屏设备，想借用一套 HDMI 转接头和激光笔。',
       17, 0, 1, 1, '2026-07-12 11:40:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园集市：项目答辩演示设备借用' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：开放日同行拼车',
       '周末去企业开放日，想找同方向同学一起拼车，方便准时到场签到。',
       26, 0, 3, 1, '2026-07-12 12:00:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '校园集市：开放日同行拼车' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '求职经验：后端实习面试复盘',
       '分享一次后端实习面试经历，主要问了 Java 集合、MySQL 索引和项目接口设计。',
       58, 0, 8, 1, '2026-07-12 13:00:00'
FROM forum_board b
WHERE b.name = '求职经验'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '求职经验：后端实习面试复盘' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '求职经验：简历技能标签怎么选',
       '投递岗位时，技能标签会影响企业筛选吗？Java、Spring Boot、数据库应该怎么排序？',
       47, 0, 6, 1, '2026-07-12 13:20:00'
FROM forum_board b
WHERE b.name = '求职经验'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '求职经验：简历技能标签怎么选' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '求职经验：求职信模板分享',
       '整理了一个适合投递实习岗位的求职信模板，重点突出学习成果和项目实践。',
       39, 0, 4, 1, '2026-07-12 13:40:00'
FROM forum_board b
WHERE b.name = '求职经验'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '求职经验：求职信模板分享' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '求职经验：线上笔试准备路线',
       '准备后端岗位笔试，建议先刷基础算法，再复盘数据库和网络常见题。',
       44, 0, 5, 1, '2026-07-12 14:00:00'
FROM forum_board b
WHERE b.name = '求职经验'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '求职经验：线上笔试准备路线' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '求职经验：企业开放日要问什么',
       '参加企业开放日时，可以重点了解技术栈、导师机制、实习转正和项目分工。',
       32, 0, 3, 1, '2026-07-12 14:20:00'
FROM forum_board b
WHERE b.name = '求职经验'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '求职经验：企业开放日要问什么' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '政策解读：课程学分如何换算',
       '课程学时、项目实践和考核结果会一起影响学分认定，建议提前整理学习记录。',
       49, 0, 7, 1, '2026-07-12 15:00:00'
FROM forum_board b
WHERE b.name = '政策解读'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '政策解读：课程学分如何换算' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '政策解读：活动签到学分规则',
       '企业活动需要完成报名和签到后才会进入学分奖励流程，部分活动还有参与反馈要求。',
       37, 0, 4, 1, '2026-07-12 15:20:00'
FROM forum_board b
WHERE b.name = '政策解读'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '政策解读：活动签到学分规则' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '政策解读：诚信分影响哪些操作',
       '诚信分较低时可能影响部分活动报名和资料审核，请及时处理违规记录。',
       35, 0, 4, 1, '2026-07-12 15:40:00'
FROM forum_board b
WHERE b.name = '政策解读'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '政策解读：诚信分影响哪些操作' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '政策解读：学习成果审核周期',
       '普通课程成果通常在 3 个工作日内审核，企业项目和线下活动可能需要更长时间。',
       29, 0, 3, 1, '2026-07-12 16:00:00'
FROM forum_board b
WHERE b.name = '政策解读'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '政策解读：学习成果审核周期' AND p.deleted = 0);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '政策解读：企业招聘信息审核说明',
       '企业发布招聘和活动后，平台会对内容完整性、岗位真实性和联系方式进行审核。',
       31, 0, 3, 1, '2026-07-12 16:20:00'
FROM forum_board b
WHERE b.name = '政策解读'
  AND NOT EXISTS (SELECT 1 FROM forum_post p WHERE p.title = '政策解读：企业招聘信息审核说明' AND p.deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '招聘：Java 后端工程师助理', '参与论坛、搜索和学分流水模块开发。', 'Java 基础扎实，了解 Spring Boot。', '8K-12K', '深圳', '本科', 1, '2026-07-12 09:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '招聘：Java 后端工程师助理' AND deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '招聘：Vue 前端工程师助理', '负责学生端页面和个人中心交互实现。', '熟悉 Vue3、TypeScript、Element Plus。', '8K-11K', '广州', '本科', 1, '2026-07-12 09:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '招聘：Vue 前端工程师助理' AND deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '招聘：测试开发实习生', '负责接口测试、页面回归和自动化用例维护。', '了解 HTTP、SQL 和基础测试流程。', '120-180/天', '上海', '本科', 1, '2026-07-12 09:40:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '招聘：测试开发实习生' AND deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '招聘：数据运营实习生', '维护课程、活动和招聘数据质量，输出运营分析报表。', '熟悉 Excel，了解 SQL 优先。', '100-150/天', '远程', '本科', 1, '2026-07-12 10:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '招聘：数据运营实习生' AND deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '招聘：产品助理实习生', '参与学分银行产品需求梳理和用户反馈跟进。', '沟通能力好，能写清楚需求文档。', '120-180/天', '深圳', '本科', 1, '2026-07-12 10:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '招聘：产品助理实习生' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '活动：后端接口设计工作坊', '围绕论坛发帖、回复、点赞接口讲解 REST API 设计。', '线上会议', '2026-08-22 19:30:00', '2026-08-22 21:00:00', 100, 3.00, 1, '2026-07-12 11:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '活动：后端接口设计工作坊' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '活动：前端页面评审会', '讲解资讯中心、论坛列表和个人中心页面交互优化。', '示例科技集团会议室', '2026-08-25 14:00:00', '2026-08-25 16:00:00', 80, 4.00, 1, '2026-07-12 11:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '活动：前端页面评审会' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '活动：简历投递模拟面试', '模拟企业筛选、投递、面试邀请和反馈流程。', '线上会议', '2026-08-28 19:00:00', '2026-08-28 21:00:00', 120, 5.00, 1, '2026-07-12 11:40:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '活动：简历投递模拟面试' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '活动：Docker 本地开发环境搭建', '带领学员使用 Docker 初始化 MySQL、Redis 和项目数据库。', '机房 A201', '2026-09-02 09:30:00', '2026-09-02 11:30:00', 60, 4.00, 1, '2026-07-12 12:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '活动：Docker 本地开发环境搭建' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '活动：学习成果展示日', '学员展示课程项目、企业实践和成果认证材料。', '大学城创新中心', '2026-09-10 13:30:00', '2026-09-10 17:30:00', 150, 6.00, 1, '2026-07-12 12:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '活动：学习成果展示日' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策：论坛内容发布规范', '论坛发帖应围绕学习、求职、校园互助和政策讨论，禁止发布广告、诈骗和侵权内容。', '学分银行平台', '运营中心', 1, '2026-07-12 08:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策：论坛内容发布规范' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策：活动报名与取消规则', '活动报名后如无法参加，应在活动开始前取消，频繁爽约可能影响诚信记录。', '学分银行平台', '教务处', 1, '2026-07-12 08:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策：活动报名与取消规则' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策：招聘信息展示规则', '资讯中心招聘信息按照发布时间、企业状态和岗位状态综合展示，已下架岗位不再公开。', '学分银行平台', '就业中心', 1, '2026-07-12 08:40:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策：招聘信息展示规则' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策：学习档案数据说明', '学习档案会汇总课程学习、活动参与、论坛互动和成果认证等信息。', '学分银行平台', '教务处', 1, '2026-07-12 09:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策：学习档案数据说明' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策：企业加盟与内容审核流程', '企业加盟后可发布招聘、活动和企业资料，平台管理员会对内容进行巡检。', '学分银行平台', '管理员办公室', 1, '2026-07-12 09:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策：企业加盟与内容审核流程' AND deleted = 0);

UPDATE forum_post p
SET reply_count = (
    SELECT COUNT(*) FROM forum_reply r WHERE r.post_id = p.id AND r.deleted = 0
)
WHERE p.deleted = 0;
