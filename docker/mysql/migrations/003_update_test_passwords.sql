-- 更新测试账号密码为 admin123 (BCrypt)
UPDATE sys_user SET password = '$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu'
WHERE username IN ('admin', 'student1', 'enterprise1');
