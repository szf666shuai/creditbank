-- 面试邀请：支持平台内 TRTC 视频面试（幂等）
SET @has_interview_mode := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE table_schema = DATABASE() AND table_name = 'interview_invitation' AND column_name = 'interview_mode'
);
SET @sql := IF(
  @has_interview_mode = 0,
  'ALTER TABLE interview_invitation ADD COLUMN interview_mode TINYINT NOT NULL DEFAULT 0 COMMENT ''0现场 1视频'' AFTER location',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_room_id := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE table_schema = DATABASE() AND table_name = 'interview_invitation' AND column_name = 'room_id'
);
SET @sql := IF(
  @has_room_id = 0,
  'ALTER TABLE interview_invitation ADD COLUMN room_id VARCHAR(64) COMMENT ''TRTC 字符串房间号'' AFTER interview_mode',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE interview_invitation
SET interview_mode = 1
WHERE location LIKE '%视频%'
   OR location LIKE '%线上%'
   OR location LIKE '%腾讯会议%';

UPDATE interview_invitation
SET room_id = CONCAT('interview-', id)
WHERE interview_mode = 1 AND (room_id IS NULL OR room_id = '');
