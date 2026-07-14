CREATE TABLE IF NOT EXISTS course_episode (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分集ID',
    course_id               BIGINT NOT NULL COMMENT '课程ID',
    title                   VARCHAR(200) NOT NULL COMMENT '分集标题',
    video_url               VARCHAR(1000) COMMENT '视频地址',
    video_duration_seconds  INT NOT NULL DEFAULT 0 COMMENT '视频时长(秒)',
    sort_order              INT NOT NULL DEFAULT 0 COMMENT '排序',
    status                  TINYINT NOT NULL DEFAULT 1 COMMENT '0下架 1上架',
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                 TINYINT NOT NULL DEFAULT 0,
    INDEX idx_course_episode_course (course_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分集';

CREATE TABLE IF NOT EXISTS user_episode_progress (
    id                          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id                     BIGINT NOT NULL,
    course_id                   BIGINT NOT NULL,
    episode_id                  BIGINT NOT NULL,
    progress                    TINYINT NOT NULL DEFAULT 0,
    watched_seconds             INT NOT NULL DEFAULT 0,
    max_watched_position_seconds INT NOT NULL DEFAULT 0,
    last_position_seconds       INT NOT NULL DEFAULT 0,
    status                      TINYINT NOT NULL DEFAULT 0 COMMENT '0学习中 1已完成',
    create_time                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_episode (user_id, episode_id),
    INDEX idx_user_episode_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户分集学习进度';

CREATE TABLE IF NOT EXISTS user_learning_checkin (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT NOT NULL,
    course_id   BIGINT NOT NULL,
    checkin_date DATE NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course_checkin (user_id, course_id, checkin_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程学习打卡';

CREATE TABLE IF NOT EXISTS user_learning_reminder (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT NOT NULL,
    course_id        BIGINT NOT NULL,
    enabled          TINYINT NOT NULL DEFAULT 1,
    interval_minutes INT NOT NULL DEFAULT 30,
    last_remind_at   DATETIME NULL,
    create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_course_reminder (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程学习提醒';

SET @add_mall_org_id = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'org_id') = 0,
  'ALTER TABLE mall_product ADD COLUMN org_id BIGINT NULL COMMENT ''发布机构ID'' AFTER category_id',
  'SELECT 1'
);
PREPARE s1 FROM @add_mall_org_id; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @add_mall_publisher_id = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'publisher_id') = 0,
  'ALTER TABLE mall_product ADD COLUMN publisher_id BIGINT NULL COMMENT ''发布人ID'' AFTER org_id',
  'SELECT 1'
);
PREPARE s2 FROM @add_mall_publisher_id; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @add_mall_approval = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'approval_status') = 0,
  'ALTER TABLE mall_product ADD COLUMN approval_status TINYINT NOT NULL DEFAULT 1 COMMENT ''0待审 1通过 2驳回'' AFTER status',
  'SELECT 1'
);
PREPARE s3 FROM @add_mall_approval; EXECUTE s3; DEALLOCATE PREPARE s3;

SET @add_mall_review_remark = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'review_remark') = 0,
  'ALTER TABLE mall_product ADD COLUMN review_remark VARCHAR(255) NULL COMMENT ''审核备注'' AFTER approval_status',
  'SELECT 1'
);
PREPARE s4 FROM @add_mall_review_remark; EXECUTE s4; DEALLOCATE PREPARE s4;

SET @add_mall_reviewed_by = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'reviewed_by') = 0,
  'ALTER TABLE mall_product ADD COLUMN reviewed_by BIGINT NULL COMMENT ''审核人'' AFTER review_remark',
  'SELECT 1'
);
PREPARE s5 FROM @add_mall_reviewed_by; EXECUTE s5; DEALLOCATE PREPARE s5;

SET @add_mall_reviewed_at = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'reviewed_at') = 0,
  'ALTER TABLE mall_product ADD COLUMN reviewed_at DATETIME NULL COMMENT ''审核时间'' AFTER reviewed_by',
  'SELECT 1'
);
PREPARE s6 FROM @add_mall_reviewed_at; EXECUTE s6; DEALLOCATE PREPARE s6;

UPDATE mall_product SET approval_status = 1 WHERE approval_status IS NULL OR approval_status = 0;

INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description, enabled)
SELECT 'LEARNING_CHECKIN', '课程学习打卡', 2.00, 'learning_checkin', '课程每日学习打卡奖励', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_rule WHERE rule_code = 'LEARNING_CHECKIN');

INSERT INTO course_episode (course_id, title, video_url, video_duration_seconds, sort_order, status)
SELECT c.id,
       CONCAT(c.title, ' · 第1集'),
       c.video_url,
       COALESCE(c.video_duration_seconds, 0),
       1,
       1
FROM course c
WHERE c.deleted = 0 AND c.status = 1 AND c.video_url IS NOT NULL AND c.video_url <> ''
  AND NOT EXISTS (SELECT 1 FROM course_episode e WHERE e.course_id = c.id AND e.sort_order = 1);

INSERT INTO course_episode (course_id, title, video_url, video_duration_seconds, sort_order, status)
SELECT c.id,
       CONCAT(c.title, ' · 第2集'),
       CASE
         WHEN c.video_url LIKE '%java%' THEN '/videos/spring-boot.mp4'
         WHEN c.video_url LIKE '%python%' THEN '/videos/database.mp4'
         WHEN c.video_url LIKE '%frontend%' THEN '/videos/blockchain.mp4'
         ELSE '/videos/java.mp4'
       END,
       CASE
         WHEN c.video_url LIKE '%spring-boot%' THEN 70
         WHEN c.video_url LIKE '%database%' THEN 234
         WHEN c.video_url LIKE '%blockchain%' THEN 88
         ELSE 192
       END,
       2,
       1
FROM course c
WHERE c.deleted = 0 AND c.status = 1 AND c.video_url IS NOT NULL AND c.video_url <> ''
  AND NOT EXISTS (SELECT 1 FROM course_episode e WHERE e.course_id = c.id AND e.sort_order = 2);

-- 修复历史写入时「 · 第N集」中文被连接乱码替换成 ? 的标题
UPDATE course_episode e
INNER JOIN course c ON c.id = e.course_id
SET e.title = CONCAT(c.title, ' · 第', e.sort_order, '集')
WHERE e.deleted = 0
  AND e.title LIKE '%?%';
