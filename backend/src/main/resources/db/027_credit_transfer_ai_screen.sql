-- 学分转换申请：提交时自动 AI 初筛结果落库
SET NAMES utf8mb4;

SET @add_ai_suggestion = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_transfer_application' AND COLUMN_NAME = 'ai_suggestion') = 0,
  'ALTER TABLE credit_transfer_application ADD COLUMN ai_suggestion VARCHAR(20) NULL COMMENT ''AI初筛建议 approve/reject/uncertain'' AFTER apply_reason',
  'SELECT 1'
);
PREPARE s1 FROM @add_ai_suggestion; EXECUTE s1; DEALLOCATE PREPARE s1;

SET @add_ai_reason = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_transfer_application' AND COLUMN_NAME = 'ai_reason') = 0,
  'ALTER TABLE credit_transfer_application ADD COLUMN ai_reason VARCHAR(500) NULL COMMENT ''AI初筛理由'' AFTER ai_suggestion',
  'SELECT 1'
);
PREPARE s2 FROM @add_ai_reason; EXECUTE s2; DEALLOCATE PREPARE s2;

SET @add_ai_llm_used = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_transfer_application' AND COLUMN_NAME = 'ai_llm_used') = 0,
  'ALTER TABLE credit_transfer_application ADD COLUMN ai_llm_used TINYINT NULL DEFAULT 0 COMMENT ''是否调用大模型 0否1是'' AFTER ai_reason',
  'SELECT 1'
);
PREPARE s3 FROM @add_ai_llm_used; EXECUTE s3; DEALLOCATE PREPARE s3;

SET @add_ai_screen_time = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'credit_transfer_application' AND COLUMN_NAME = 'ai_screen_time') = 0,
  'ALTER TABLE credit_transfer_application ADD COLUMN ai_screen_time DATETIME NULL COMMENT ''AI初筛时间'' AFTER ai_llm_used',
  'SELECT 1'
);
PREPARE s4 FROM @add_ai_screen_time; EXECUTE s4; DEALLOCATE PREPARE s4;
