-- 学分转换功能：新增转换规则表、转换申请表，修改相关表结构
SET NAMES utf8mb4;

-- 1. 新增学分转换规则表
CREATE TABLE IF NOT EXISTS credit_transfer_rule (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    org_id                  BIGINT NOT NULL COMMENT '规则所属组织ID',
    source_course_id        BIGINT NULL COMMENT '源课程ID（为空表示匹配标签）',
    source_tags             VARCHAR(500) NULL COMMENT '源课程标签（逗号分隔，用于匹配知识技能要求）',
    target_course_id        BIGINT NULL COMMENT '目标课程ID（课程间转换）',
    target_certificate_id   BIGINT NULL COMMENT '目标证书ID（课程转证书）',
    target_org_id           BIGINT NULL COMMENT '目标组织ID',
    credit_ratio            DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '学分换算比例',
    description             VARCHAR(500) NULL COMMENT '规则描述',
    status                  TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
    create_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_transfer_rule_org (org_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学分转换规则';

-- 2. 新增学分转换申请表
CREATE TABLE IF NOT EXISTS credit_transfer_application (
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    user_id                 BIGINT NOT NULL COMMENT '申请人ID',
    source_course_id        BIGINT NOT NULL COMMENT '已修课程ID',
    source_org_id           BIGINT NOT NULL COMMENT '原修课组织ID',
    source_credit           DECIMAL(10,2) NOT NULL COMMENT '原获得学分',
    target_course_id        BIGINT NULL COMMENT '目标课程ID',
    target_certificate_id   BIGINT NULL COMMENT '目标证书ID',
    target_org_id           BIGINT NOT NULL COMMENT '目标组织ID',
    apply_reason            VARCHAR(500) NULL COMMENT '申请理由',
    status                  TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
    reviewer_id             BIGINT NULL COMMENT '审核人ID',
    review_comment          VARCHAR(500) NULL COMMENT '审核意见',
    actual_credit           DECIMAL(10,2) NULL COMMENT '实际转换学分',
    apply_time              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    review_time             DATETIME NULL COMMENT '审核时间',
    INDEX idx_transfer_app_user (user_id, status),
    INDEX idx_transfer_app_org (target_org_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学分转换申请';

-- 3. 修改course表：添加学分值字段
SET @add_course_credit_value = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course' AND COLUMN_NAME = 'credit_value') = 0,
  'ALTER TABLE course ADD COLUMN credit_value DECIMAL(10,2) NOT NULL DEFAULT 3.00 COMMENT ''课程学分值'' AFTER credit_reward',
  'SELECT 1'
);
PREPARE s1 FROM @add_course_credit_value; EXECUTE s1; DEALLOCATE PREPARE s1;

-- 4. 修改learning_archive表：添加转换相关字段
SET @add_archive_transfer_to_org = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'learning_archive' AND COLUMN_NAME = 'transferred_to_org_id') = 0,
  'ALTER TABLE learning_archive ADD COLUMN transferred_to_org_id BIGINT NULL COMMENT ''转换到的组织ID'' AFTER status',
  'SELECT 1'
);
PREPARE s2 FROM @add_archive_transfer_to_org; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @add_archive_transfer_to_course = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'learning_archive' AND COLUMN_NAME = 'transferred_to_course_id') = 0,
  'ALTER TABLE learning_archive ADD COLUMN transferred_to_course_id BIGINT NULL COMMENT ''转换为的课程ID'' AFTER transferred_to_org_id',
  'SELECT 1'
);
PREPARE s3 FROM @add_archive_transfer_to_course; EXECUTE s3; DEALLOCATE PREPARE s3;

SET @add_archive_transfer_to_certificate = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'learning_archive' AND COLUMN_NAME = 'transferred_to_certificate_id') = 0,
  'ALTER TABLE learning_archive ADD COLUMN transferred_to_certificate_id BIGINT NULL COMMENT ''转换为的证书ID'' AFTER transferred_to_course_id',
  'SELECT 1'
);
PREPARE s4 FROM @add_archive_transfer_to_certificate; EXECUTE s4; DEALLOCATE PREPARE s4;

SET @add_archive_transfer_application = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'learning_archive' AND COLUMN_NAME = 'transfer_application_id') = 0,
  'ALTER TABLE learning_archive ADD COLUMN transfer_application_id BIGINT NULL COMMENT ''关联转换申请ID'' AFTER transferred_to_certificate_id',
  'SELECT 1'
);
PREPARE s5 FROM @add_archive_transfer_application; EXECUTE s5; DEALLOCATE PREPARE s5;

SET @add_archive_transfer_status = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'learning_archive' AND COLUMN_NAME = 'transfer_status') = 0,
  'ALTER TABLE learning_archive ADD COLUMN transfer_status TINYINT NOT NULL DEFAULT 0 COMMENT ''0未转换 1转换中 2已转换'' AFTER transfer_application_id',
  'SELECT 1'
);
PREPARE s6 FROM @add_archive_transfer_status; EXECUTE s6; DEALLOCATE PREPARE s6;

-- 5. 修改credit_account表：删除balance和total_spent字段（不再作为货币使用）
SET @drop_credit_account_balance = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_account' AND COLUMN_NAME = 'balance') > 0,
  'ALTER TABLE credit_account DROP COLUMN balance',
  'SELECT 1'
);
PREPARE s7 FROM @drop_credit_account_balance; EXECUTE s7; DEALLOCATE PREPARE s7;

SET @drop_credit_account_total_spent = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_account' AND COLUMN_NAME = 'total_spent') > 0,
  'ALTER TABLE credit_account DROP COLUMN total_spent',
  'SELECT 1'
);
PREPARE s8 FROM @drop_credit_account_total_spent; EXECUTE s8; DEALLOCATE PREPARE s8;