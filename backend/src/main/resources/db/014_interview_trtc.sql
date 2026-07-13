-- 面试邀请：支持平台内 TRTC 视频面试
ALTER TABLE interview_invitation
    ADD COLUMN interview_mode TINYINT NOT NULL DEFAULT 0 COMMENT '0现场 1视频' AFTER location,
    ADD COLUMN room_id VARCHAR(64) COMMENT 'TRTC 字符串房间号' AFTER interview_mode;

-- 历史「线上视频」类邀请默认视为视频模式
UPDATE interview_invitation
SET `interview_mode` = 1
WHERE location LIKE '%视频%'
   OR location LIKE '%线上%'
   OR location LIKE '%腾讯会议%';

UPDATE interview_invitation
SET `room_id` = CONCAT('interview-', id)
WHERE `interview_mode` = 1 AND (`room_id` IS NULL OR `room_id` = '');
