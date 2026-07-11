SET @add_course_comment_parent_id = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'course_comment' AND COLUMN_NAME = 'parent_id') = 0,
  'ALTER TABLE course_comment ADD COLUMN parent_id BIGINT NULL COMMENT ''父评论ID(回复)'' AFTER user_id',
  'SELECT 1'
);
PREPARE statement FROM @add_course_comment_parent_id;
EXECUTE statement;
DEALLOCATE PREPARE statement;

UPDATE course_material
SET file_url = '/materials/course-outline.pdf'
WHERE title = '课程大纲.pdf' AND file_url <> '/materials/course-outline.pdf';

UPDATE course_material
SET file_url = '/materials/mindmap.svg', file_type = 'link'
WHERE title = '知识点思维导图.png';

UPDATE course_material
SET file_url = '/materials/sample-code.zip'
WHERE title = '示例代码.zip' AND file_url <> '/materials/sample-code.zip';

UPDATE course_material
SET file_url = '/materials/exercises.md'
WHERE title = '课后练习.md' AND file_url <> '/materials/exercises.md';

INSERT INTO course_comment (course_id, user_id, parent_id, content, like_count, create_time)
SELECT parent.course_id, u.id, parent.id, '同感，这一节我反复看了两遍才理解。', 3, DATE_SUB(NOW(), INTERVAL 6 HOUR)
FROM course_comment parent
JOIN sys_user u ON u.username = 'admin'
WHERE parent.parent_id IS NULL
  AND parent.content = '这节课讲得很清楚，适合零基础入门。'
  AND NOT EXISTS (
      SELECT 1 FROM course_comment reply
      WHERE reply.parent_id = parent.id AND reply.content = '同感，这一节我反复看了两遍才理解。'
  )
LIMIT 1;
