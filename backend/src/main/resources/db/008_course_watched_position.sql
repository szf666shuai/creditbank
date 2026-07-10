-- 将课程进度改为按从开头连续观看到的最远位置计算，可重复执行
SET NAMES utf8mb4;

SET @add_max_watched_position = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_course'
     AND COLUMN_NAME = 'max_watched_position_seconds') = 0,
  'ALTER TABLE user_course ADD COLUMN max_watched_position_seconds INT NOT NULL DEFAULT 0 COMMENT ''从开头连续看过的最远位置(秒)'' AFTER watched_seconds',
  'SELECT 1'
);
PREPARE statement FROM @add_max_watched_position;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE user_course uc
JOIN course c ON c.id = uc.course_id
SET uc.max_watched_position_seconds = CASE
      WHEN uc.status = 1 THEN c.video_duration_seconds
      WHEN uc.max_watched_position_seconds = 0
        THEN LEAST(uc.watched_seconds, uc.last_position_seconds, c.video_duration_seconds)
      ELSE LEAST(uc.max_watched_position_seconds, c.video_duration_seconds)
    END,
    uc.progress = CASE
      WHEN uc.status = 1 THEN 100
      WHEN c.video_duration_seconds > 0 THEN FLOOR(
        (CASE
          WHEN uc.max_watched_position_seconds = 0
            THEN LEAST(uc.watched_seconds, uc.last_position_seconds, c.video_duration_seconds)
          ELSE LEAST(uc.max_watched_position_seconds, c.video_duration_seconds)
        END) * 100 / c.video_duration_seconds
      )
      ELSE 0
    END;
