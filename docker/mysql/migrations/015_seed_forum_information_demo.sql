-- Additional forum and information center demo data.

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：学分认定材料怎么准备？',
       '准备申请 Java 课程学分认定，想请教大家学习证明、项目截图和课程证书分别需要怎么整理。',
       24, 0, 3, 1, '2026-07-11 09:20:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (
    SELECT 1 FROM forum_post p
    WHERE p.title = '校园频道：学分认定材料怎么准备？' AND p.deleted = 0
  );

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园频道：有没有适合新手的 Spring Boot 小项目？',
       '刚学完基础语法，想找一个能放进简历里的 Spring Boot 小项目，最好能结合学分银行业务场景。',
       31, 0, 5, 1, '2026-07-11 10:10:00'
FROM forum_board b
WHERE b.name = '校园频道'
  AND NOT EXISTS (
    SELECT 1 FROM forum_post p
    WHERE p.title = '校园频道：有没有适合新手的 Spring Boot 小项目？' AND p.deleted = 0
  );

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：出一本 Java 核心技术和算法笔记',
       '二手书九成新，适合准备后端岗位的同学，支持校园内自提。',
       18, 0, 2, 1, '2026-07-11 11:05:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (
    SELECT 1 FROM forum_post p
    WHERE p.title = '校园集市：出一本 Java 核心技术和算法笔记' AND p.deleted = 0
  );

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
SELECT b.id, 2, '校园集市：组队参加企业开放日',
       '想找两位同学一起报名示例科技集团开放日，主要关注后端研发和简历投递流程。',
       22, 0, 4, 1, '2026-07-11 11:30:00'
FROM forum_board b
WHERE b.name = '校园集市'
  AND NOT EXISTS (
    SELECT 1 FROM forum_post p
    WHERE p.title = '校园集市：组队参加企业开放日' AND p.deleted = 0
  );

INSERT INTO forum_reply (post_id, user_id, parent_id, content, like_count, status, create_time)
SELECT p.id, 3, 0, '建议把课程证书、学习时长截图和项目仓库地址放在同一个 PDF 里，审核会更清楚。', 1, 1, '2026-07-11 12:00:00'
FROM forum_post p
WHERE p.title = '校园频道：学分认定材料怎么准备？'
  AND NOT EXISTS (
    SELECT 1 FROM forum_reply r
    WHERE r.post_id = p.id AND r.content = '建议把课程证书、学习时长截图和项目仓库地址放在同一个 PDF 里，审核会更清楚。'
  );

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '校园招聘：后端开发实习生',
       '参与学分银行平台后端接口、论坛模块和资讯中心功能开发，导师带教。',
       '熟悉 Java、Spring Boot、MySQL；了解 Vue 或 Docker 加分。',
       '150-220/天', '深圳/远程', '本科', 1, '2026-07-11 09:00:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '校园招聘：后端开发实习生' AND deleted = 0);

INSERT INTO job_posting (org_id, publisher_id, title, description, requirements, salary_range, location, edu_requirement, status, create_time)
SELECT 2, 3, '校园招聘：前端开发实习生',
       '负责学生端页面、个人中心交互和 Element Plus 组件封装。',
       '熟悉 Vue3、TypeScript、路由和基础接口联调。',
       '140-200/天', '广州', '本科', 1, '2026-07-11 09:30:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM job_posting WHERE title = '校园招聘：前端开发实习生' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '企业开放日：学分银行产品体验',
       '参观企业研发环境，体验论坛、招聘投递和活动报名闭环流程。',
       '示例科技集团深圳研发中心', '2026-08-10 14:00:00', '2026-08-10 17:00:00', 60, 5.00, 1, '2026-07-11 10:00:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '企业开放日：学分银行产品体验' AND deleted = 0);

INSERT INTO activity (org_id, publisher_id, title, description, location, start_time, end_time, max_participants, credit_reward, status, create_time)
SELECT 2, 3, '简历工作坊：项目经历打磨',
       '企业导师现场点评简历，重点讲解项目经历、技能标签和求职信写法。',
       '线上会议', '2026-08-18 19:30:00', '2026-08-18 21:00:00', 120, 3.00, 1, '2026-07-11 10:20:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE title = '简历工作坊：项目经历打磨' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策资讯：学习成果认定材料规范更新',
       '平台更新学习成果认定材料规范，新增课程证书、项目仓库、活动参与记录等材料提交建议。',
       '学分银行平台', '运营中心', 1, '2026-07-11 08:30:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策资讯：学习成果认定材料规范更新' AND deleted = 0);

INSERT INTO policy_news (title, content, source, author, status, publish_time)
SELECT '政策资讯：企业活动学分奖励说明',
       '参与企业开放日、技术沙龙、简历工作坊等活动并完成签到后，可根据活动配置获得对应学分奖励。',
       '学分银行平台', '教务处', 1, '2026-07-11 08:45:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM policy_news WHERE title = '政策资讯：企业活动学分奖励说明' AND deleted = 0);

UPDATE forum_post p
SET reply_count = (
    SELECT COUNT(*) FROM forum_reply r WHERE r.post_id = p.id AND r.deleted = 0
)
WHERE p.deleted = 0;
