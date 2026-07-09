-- 与 docker/mysql/migrations/005_credit_integrity_rules.sql 保持同步
INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description, enabled)
SELECT 'REGISTER_BONUS', '新用户注册奖励', 20.00, 'register', '学员注册成功一次性奖励', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_rule WHERE rule_code = 'REGISTER_BONUS');

INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description, enabled)
SELECT 'DAILY_CHECKIN', '每日签到', 2.00, 'daily_checkin', '每日签到奖励', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_rule WHERE rule_code = 'DAILY_CHECKIN');

INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description, enabled)
SELECT 'CHECKIN_STREAK_7', '连续签到7天', 5.00, 'checkin_streak', '连续签到满7天额外奖励', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_rule WHERE rule_code = 'CHECKIN_STREAK_7');

INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description, enabled)
SELECT 'ACHIEVE_VERIFY', '成果校验通过', 15.00, 'achieve_verify', '学习成果区块链校验通过奖励', 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_rule WHERE rule_code = 'ACHIEVE_VERIFY');
