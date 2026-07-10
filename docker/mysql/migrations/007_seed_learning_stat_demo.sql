-- Demo daily learning stats for student1 (user_id=2), last 14 days from 2026-06-25
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-25', 35, 0, 0.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-25');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-26', 50, 0, 1.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-26');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-27', 20, 0, 0.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-27');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-28', 65, 1, 2.50 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-28');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-29', 40, 0, 0.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-29');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-06-30', 55, 0, 1.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-06-30');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-01', 75, 1, 3.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-01');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-02', 30, 0, 0.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-02');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-03', 90, 1, 4.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-03');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-04', 45, 0, 0.50 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-04');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-05', 60, 0, 1.50 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-05');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-06', 80, 1, 2.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-06');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-07', 70, 0, 1.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-07');
INSERT INTO learning_stat_daily (user_id, stat_date, study_minutes, courses_completed, credit_earned)
SELECT 2, '2026-07-08', 55, 1, 2.50 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM learning_stat_daily WHERE user_id = 2 AND stat_date = '2026-07-08');
