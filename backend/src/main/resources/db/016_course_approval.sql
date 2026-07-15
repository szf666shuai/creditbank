-- 课程审核功能：添加课程审核状态字段
SET NAMES utf8mb4;

-- 1. 修改course表：添加审核状态字段
SET @add_course_approval_status = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'approval_status') = 0,
  'ALTER TABLE course ADD COLUMN approval_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0待审核 1已通过 2已驳回'' AFTER status',
  'SELECT 1'
);
PREPARE s1 FROM @add_course_approval_status; EXECUTE s1; DEALLOCATE PREPARE s1;

-- 2. 修改course表：添加审核备注字段
SET @add_course_review_remark = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'review_remark') = 0,
  'ALTER TABLE course ADD COLUMN review_remark VARCHAR(500) NULL COMMENT ''审核备注'' AFTER approval_status',
  'SELECT 1'
);
PREPARE s2 FROM @add_course_review_remark; EXECUTE s2; DEALLOCATE PREPARE s2;

-- 3. 修改course表：添加审核人ID字段
SET @add_course_reviewed_by = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'reviewed_by') = 0,
  'ALTER TABLE course ADD COLUMN reviewed_by BIGINT NULL COMMENT ''审核人ID'' AFTER review_remark',
  'SELECT 1'
);
PREPARE s3 FROM @add_course_reviewed_by; EXECUTE s3; DEALLOCATE PREPARE s3;

-- 4. 修改course表：添加审核时间字段
SET @add_course_reviewed_at = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'reviewed_at') = 0,
  'ALTER TABLE course ADD COLUMN reviewed_at DATETIME NULL COMMENT ''审核时间'' AFTER reviewed_by',
  'SELECT 1'
);
PREPARE s4 FROM @add_course_reviewed_at; EXECUTE s4; DEALLOCATE PREPARE s4;

-- 5. 修改course表：添加难度等级字段
SET @add_course_difficulty = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'difficulty') = 0,
  'ALTER TABLE course ADD COLUMN difficulty TINYINT NOT NULL DEFAULT 1 COMMENT ''1入门 2初级 3中级 4高级'' AFTER credit_value',
  'SELECT 1'
);
PREPARE s5 FROM @add_course_difficulty; EXECUTE s5; DEALLOCATE PREPARE s5;

-- 6. 修改course表：添加时长分钟数字段（替代duration_hours）
SET @add_course_duration_minutes = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'duration_minutes') = 0,
  'ALTER TABLE course ADD COLUMN duration_minutes INT NOT NULL DEFAULT 60 COMMENT ''课程时长(分钟)'' AFTER difficulty',
  'SELECT 1'
);
PREPARE s6 FROM @add_course_duration_minutes; EXECUTE s6; DEALLOCATE PREPARE s6;

-- 7. 修改已有的课程数据：设置审核状态为已通过
UPDATE course SET approval_status = 1 WHERE approval_status = 0 AND status = 1;