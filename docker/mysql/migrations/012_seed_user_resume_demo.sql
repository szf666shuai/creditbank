-- Demo resume for student1 (user_id=2)
INSERT INTO user_resume (user_id, title, content_json, is_default, version, create_time, update_time)
SELECT 2, '默认简历',
  JSON_OBJECT(
    'realName', '测试学员',
    'phone', '13800000001',
    'email', 'student1@example.com',
    'education', '本科 · 计算机科学与技术 · 示例大学',
    'workExperience', '2025.06 - 至今 | 示例科技 | Java 开发实习生\n负责学分银行平台后端模块开发与接口联调。',
    'skills', 'Java, Spring Boot, MySQL, Vue3, Git',
    'selfIntro', '热爱编程，具备扎实的 Java 后端基础，参与过多个课程项目与开源实践。',
    'projects', '学分银行平台：Spring Boot + Vue3 全栈项目，负责用户中心与招聘模块。'
  ),
  1, 1, '2026-06-01 10:00:00', '2026-07-01 16:00:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user_resume WHERE user_id = 2 AND is_default = 1);
