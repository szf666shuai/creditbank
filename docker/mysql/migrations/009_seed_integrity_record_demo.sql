-- Demo integrity records for student1 (user_id=2)
INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, -3, 97, 2, 'Missed course assignment deadline', 'course', 1, '2026-06-05 09:30:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Missed course assignment deadline');

INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, 2, 99, 1, 'Completed learning task on time', 'course', 1, '2026-06-15 14:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Completed learning task on time');

INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, -5, 94, 2, 'Forum post removed for policy violation', 'forum_post', 101, '2026-06-25 11:20:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Forum post removed for policy violation');

INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, 3, 97, 1, 'Active participation in enterprise open day', 'activity', 1, '2026-07-01 17:30:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Active participation in enterprise open day');

INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, 2, 99, 1, 'Submitted high-quality resume', 'resume', 1, '2026-07-05 10:15:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Submitted high-quality resume');

INSERT INTO integrity_record (user_id, change_value, score_after, event_type, reason, ref_type, ref_id, create_time)
SELECT 2, -1, 98, 2, 'Late response to interview invitation', 'interview', 1, '2026-07-07 16:45:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM integrity_record WHERE user_id = 2 AND reason = 'Late response to interview invitation');

UPDATE integrity_score SET score = 98, update_time = '2026-07-07 16:45:00' WHERE user_id = 2;
