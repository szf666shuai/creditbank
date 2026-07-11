CREATE TABLE IF NOT EXISTS course_comment (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    course_id   BIGINT NOT NULL COMMENT '课程ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    content     VARCHAR(500) NOT NULL COMMENT '评论内容',
    like_count  INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_course_comment_course (course_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评论';

CREATE TABLE IF NOT EXISTS course_danmaku (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '弹幕ID',
    course_id           BIGINT NOT NULL COMMENT '课程ID',
    user_id             BIGINT NOT NULL COMMENT '用户ID',
    content             VARCHAR(100) NOT NULL COMMENT '弹幕内容',
    video_time_seconds  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '视频时间点(秒)',
    color               VARCHAR(16) NOT NULL DEFAULT '#ffffff' COMMENT '弹幕颜色',
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted             TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_course_danmaku_course (course_id, video_time_seconds)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程弹幕';

CREATE TABLE IF NOT EXISTS course_material (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课件ID',
    course_id   BIGINT NOT NULL COMMENT '课程ID',
    title       VARCHAR(200) NOT NULL COMMENT '课件标题',
    file_type   VARCHAR(30) NOT NULL DEFAULT 'pdf' COMMENT '文件类型: pdf/ppt/code/link/zip',
    file_url    VARCHAR(1000) NOT NULL COMMENT '下载或访问地址',
    sort_order  INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_course_material_course (course_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课件';

INSERT INTO course_comment (course_id, user_id, content, like_count, create_time)
SELECT c.id, u.id, seed.content, seed.like_count, DATE_SUB(NOW(), INTERVAL seed.days_ago DAY)
FROM course c
JOIN (
    SELECT '这节课讲得很清楚，适合零基础入门。' AS content, 12 AS like_count, 2 AS days_ago
    UNION ALL SELECT '已收藏，准备二刷重点章节。', 8, 5
    UNION ALL SELECT '弹幕区氛围很好，有问题随时交流。', 15, 1
) seed
JOIN sys_user u ON u.username = 'student1'
WHERE c.deleted = 0 AND c.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM course_comment existing
      WHERE existing.course_id = c.id AND existing.user_id = u.id AND existing.content = seed.content
  )
LIMIT 3;

INSERT INTO course_danmaku (course_id, user_id, content, video_time_seconds, color, create_time)
SELECT c.id, u.id, seed.content, seed.video_time, seed.color, NOW()
FROM course c
JOIN (
    SELECT '打卡学习' AS content, 8.00 AS video_time, '#ffffff' AS color
    UNION ALL SELECT '这里讲得很细', 24.50, '#fb7299'
    UNION ALL SELECT '记笔记+1', 45.00, '#6bcbff'
    UNION ALL SELECT '原来如此！', 72.00, '#ffd93d'
    UNION ALL SELECT '继续加油', 110.00, '#00d1b2'
) seed
JOIN sys_user u ON u.username = 'student1'
WHERE c.deleted = 0 AND c.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM course_danmaku existing
      WHERE existing.course_id = c.id AND existing.content = seed.content
  )
LIMIT 5;

INSERT INTO course_material (course_id, title, file_type, file_url, sort_order)
SELECT c.id, seed.title, seed.file_type, seed.file_url, seed.sort_order
FROM course c
JOIN (
    SELECT '课程大纲.pdf' AS title, 'pdf' AS file_type, '/materials/course-outline.pdf' AS file_url, 1 AS sort_order
    UNION ALL SELECT '知识点思维导图.png', 'link', 'https://example.com/mindmap', 2
    UNION ALL SELECT '示例代码.zip', 'zip', '/materials/sample-code.zip', 3
    UNION ALL SELECT '课后练习.md', 'code', '/materials/exercises.md', 4
) seed
WHERE c.deleted = 0 AND c.status = 1
  AND NOT EXISTS (
      SELECT 1 FROM course_material existing
      WHERE existing.course_id = c.id AND existing.title = seed.title
  );
