-- Fix forum demo data: remove corrupted rows and re-seed with UTF-8 text
SET NAMES utf8mb4;

DELETE FROM forum_reply WHERE user_id = 2;

DELETE r FROM forum_reply r
INNER JOIN forum_post p ON r.post_id = p.id
WHERE p.user_id IN (2, 3);

DELETE FROM forum_post WHERE user_id IN (2, 3);

INSERT INTO forum_post (board_id, user_id, title, content, view_count, reply_count, like_count, status, create_time)
VALUES
(1, 2, 'Java 入门学习路线分享', '最近完成了 Java 基础课程，整理了一份学习路线：环境搭建 → 语法基础 → 面向对象 → 集合框架。欢迎交流！', 56, 1, 8, 1, '2026-06-10 09:20:00'),
(3, 2, '简历项目经历怎么写？', '投递了几家企业，想请教大家项目经历应该怎么突出亮点，尤其是没有大型项目经验的情况下。', 42, 1, 5, 1, '2026-06-28 15:40:00'),
(1, 3, '企业开放日学习资源推荐', '示例科技集团将在本月底举办开放日，现场提供 Spring Boot 实战资料，欢迎学员参加。', 88, 1, 12, 1, '2026-07-02 11:00:00');

INSERT INTO forum_reply (post_id, user_id, parent_id, content, like_count, status, create_time)
SELECT p.id, 2, 0, '感谢分享！请问有没有推荐的练习项目？', 3, 1, '2026-07-03 14:10:00'
FROM forum_post p
WHERE p.user_id = 3 AND p.title = '企业开放日学习资源推荐';

INSERT INTO forum_reply (post_id, user_id, parent_id, content, like_count, status, create_time)
SELECT p.id, 2, 0, '补充一下：可以把课程作业包装成小型项目，重点写清楚技术栈和个人贡献。', 2, 1, '2026-07-01 10:30:00'
FROM forum_post p
WHERE p.user_id = 2 AND p.title = '简历项目经历怎么写？';

UPDATE forum_post p
SET reply_count = (
    SELECT COUNT(*) FROM forum_reply r WHERE r.post_id = p.id AND r.deleted = 0
)
WHERE p.deleted = 0;
