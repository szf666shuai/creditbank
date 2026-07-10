-- Demo credit transactions for student1 (user_id=2)
INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 1, 10.00, 10.00, 'register_bonus', 'New user registration bonus', NULL, NULL, '2026-06-01 10:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'register_bonus');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 1, 15.00, 25.00, 'course_complete', 'Completed Java Programming Basics', 'course', 1, '2026-06-15 14:30:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'course_complete' AND ref_id = 1);

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 3, 5.00, 30.00, 'activity_reward', 'Enterprise Open Day participation reward', 'activity', 1, '2026-07-01 17:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'activity_reward');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 2, 8.00, 38.00, 'achievement_convert', 'Converted learning achievement to credits', 'achievement', 1, '2026-07-03 11:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'achievement_convert');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 4, -12.00, 26.00, 'mall_exchange', 'Redeemed notebook in credit mall', 'mall_order', 1, '2026-07-05 09:15:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'mall_exchange');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 1, 12.00, 38.00, 'forum_post', 'Quality forum post reward', 'forum_post', 101, '2026-07-06 20:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'forum_post');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 4, -5.00, 33.00, 'course_buy', 'Purchased advanced course', 'course', 2, '2026-07-07 16:45:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'course_buy');

INSERT INTO credit_transaction (user_id, type, amount, balance_after, biz_type, source, ref_type, ref_id, create_time)
SELECT 2, 3, 17.00, 50.00, 'daily_learning', 'Daily learning credit growth', NULL, NULL, '2026-07-08 18:30:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM credit_transaction WHERE user_id = 2 AND biz_type = 'daily_learning');

UPDATE credit_account SET balance = 50.00, total_earned = 59.00, total_spent = 17.00 WHERE user_id = 2;
