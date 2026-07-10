-- 为已有数据库补充视频与观看进度字段，可重复执行
SET NAMES utf8mb4;

SET @add_course_video_url = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'video_url') = 0,
  'ALTER TABLE course ADD COLUMN video_url VARCHAR(1000) NULL COMMENT ''课程视频播放地址'' AFTER cover_url',
  'SELECT 1'
);
PREPARE statement FROM @add_course_video_url;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @add_course_video_duration = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'video_duration_seconds') = 0,
  'ALTER TABLE course ADD COLUMN video_duration_seconds INT NOT NULL DEFAULT 0 COMMENT ''视频时长(秒)'' AFTER video_url',
  'SELECT 1'
);
PREPARE statement FROM @add_course_video_duration;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @add_watched_seconds = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_course' AND COLUMN_NAME = 'watched_seconds') = 0,
  'ALTER TABLE user_course ADD COLUMN watched_seconds INT NOT NULL DEFAULT 0 COMMENT ''累计有效观看秒数'' AFTER progress',
  'SELECT 1'
);
PREPARE statement FROM @add_watched_seconds;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @add_last_position = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_course' AND COLUMN_NAME = 'last_position_seconds') = 0,
  'ALTER TABLE user_course ADD COLUMN last_position_seconds INT NOT NULL DEFAULT 0 COMMENT ''最后播放位置(秒)'' AFTER watched_seconds',
  'SELECT 1'
);
PREPARE statement FROM @add_last_position;
EXECUTE statement;
DEALLOCATE PREPARE statement;
