-- 现实场景课程与积分商城样本，可重复执行
SET NAMES utf8mb4;

INSERT IGNORE INTO sys_tag (name, category) VALUES
('Java', 'skill'), ('C++', 'skill'), ('Python', 'skill'),
('Spring Boot', 'skill'), ('前端开发', 'skill'), ('数据库', 'skill'),
('人工智能', 'skill'), ('区块链', 'skill');

DELETE FROM course_tag
WHERE tag_id IN (
  SELECT id FROM sys_tag
  WHERE category = 'skill'
    AND name NOT IN ('Java', 'C++', 'Python', 'Spring Boot', '前端开发', '数据库', '人工智能', '区块链')
);

DELETE FROM job_tag
WHERE tag_id IN (
  SELECT id FROM sys_tag
  WHERE category = 'skill'
    AND name NOT IN ('Java', 'C++', 'Python', 'Spring Boot', '前端开发', '数据库', '人工智能', '区块链')
);

DELETE FROM user_target_tag
WHERE tag_id IN (
  SELECT id FROM sys_tag
  WHERE category = 'skill'
    AND name NOT IN ('Java', 'C++', 'Python', 'Spring Boot', '前端开发', '数据库', '人工智能', '区块链')
);

DELETE FROM sys_tag
WHERE category = 'skill'
  AND name NOT IN ('Java', 'C++', 'Python', 'Spring Boot', '前端开发', '数据库', '人工智能', '区块链');

INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '课程资源', 0, 10, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '课程资源');
INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '学习用品', 0, 20, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '学习用品');
INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '虚拟商品', 0, 30, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '虚拟商品');
INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '兑换专区', 0, 40, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '兑换专区');
INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '虚拟权益', 0, 50, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '虚拟权益');
INSERT INTO mall_category (name, parent_id, sort_order, status)
SELECT '技术服务', 0, 60, 1 WHERE NOT EXISTS (SELECT 1 FROM mall_category WHERE name = '技术服务');

DROP TEMPORARY TABLE IF EXISTS obsolete_seed_course;
CREATE TEMPORARY TABLE obsolete_seed_course AS
SELECT id FROM course
WHERE title LIKE '[测试]%' OR title LIKE '[学习资源]%' OR title LIKE '[样本课程]%';

DELETE FROM learning_archive WHERE course_id IN (SELECT id FROM obsolete_seed_course);
DELETE FROM learning_achievement WHERE certificate_id IN (
  SELECT id FROM learning_certificate WHERE course_id IN (SELECT id FROM obsolete_seed_course)
);
DELETE FROM learning_certificate WHERE course_id IN (SELECT id FROM obsolete_seed_course);
DELETE FROM user_course WHERE course_id IN (SELECT id FROM obsolete_seed_course);
DELETE FROM course_tag WHERE course_id IN (SELECT id FROM obsolete_seed_course);
DELETE FROM course WHERE id IN (SELECT id FROM obsolete_seed_course);
DROP TEMPORARY TABLE IF EXISTS obsolete_seed_course;

DROP TEMPORARY TABLE IF EXISTS seed_course;
CREATE TEMPORARY TABLE seed_course (
  title VARCHAR(200) PRIMARY KEY,
  description TEXT NOT NULL,
  cover_url VARCHAR(255) NOT NULL,
  duration_hours DECIMAL(6,1) NOT NULL,
  credit_reward DECIMAL(10,2) NOT NULL,
  tags VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT INTO seed_course (title, description, cover_url, duration_hours, credit_reward, tags) VALUES
('Java 程序设计基础', '从变量、流程控制、数组和方法开始，完成学生成绩管理等控制台项目。', 'https://picsum.photos/seed/course-java-basic/640/360', 24.0, 8.00, 'Java'),
('Java 面向对象编程', '学习封装、继承、多态、接口和常用设计原则，完成图书借阅系统。', 'https://picsum.photos/seed/course-java-oop/640/360', 20.0, 8.00, 'Java'),
('Java 集合框架与泛型', '掌握 List、Set、Map、迭代器、泛型和 Stream API 的实际使用。', 'https://picsum.photos/seed/course-java-collections/640/360', 16.0, 6.00, 'Java'),
('Java 并发编程实战', '学习线程池、锁、并发容器和 CompletableFuture，处理真实并发任务。', 'https://picsum.photos/seed/course-java-concurrency/640/360', 22.0, 10.00, 'Java'),
('JVM 原理与性能调优', '理解类加载、内存区域、垃圾回收和常见线上性能分析方法。', 'https://picsum.photos/seed/course-jvm/640/360', 18.0, 10.00, 'Java'),
('Java Web 项目开发', '使用 Servlet、会话、过滤器和分层架构完成基础 Web 管理系统。', 'https://picsum.photos/seed/course-java-web/640/360', 28.0, 12.00, 'Java'),
('JUnit 5 与 Mockito 单元测试', '为业务服务编写可维护的单元测试、参数化测试和 Mock 测试。', 'https://picsum.photos/seed/course-java-test/640/360', 12.0, 6.00, 'Java'),
('C++ 程序设计入门', '学习变量、指针、函数、结构体和基础输入输出，完成命令行通讯录。', 'https://picsum.photos/seed/course-cpp-basic/640/360', 24.0, 8.00, 'C++'),
('C++ 面向对象与模板', '掌握类、继承、多态、运算符重载和函数模板、类模板。', 'https://picsum.photos/seed/course-cpp-oop/640/360', 20.0, 8.00, 'C++'),
('STL 标准模板库实战', '系统使用 vector、map、set、algorithm 和迭代器解决工程问题。', 'https://picsum.photos/seed/course-cpp-stl/640/360', 16.0, 7.00, 'C++'),
('数据结构与算法 C++ 实现', '用 C++ 实现链表、树、图、排序和查找算法，并分析复杂度。', 'https://picsum.photos/seed/course-cpp-dsa/640/360', 30.0, 12.00, 'C++'),
('现代 C++17 编程', '学习智能指针、移动语义、Lambda、结构化绑定和现代工程规范。', 'https://picsum.photos/seed/course-cpp17/640/360', 20.0, 9.00, 'C++'),
('C++ 内存管理与调试', '定位内存泄漏、越界访问和未定义行为，掌握常用调试工具。', 'https://picsum.photos/seed/course-cpp-memory/640/360', 14.0, 8.00, 'C++'),
('Qt 桌面应用开发', '使用 Qt Widgets、信号槽和网络模块完成跨平台桌面应用。', 'https://picsum.photos/seed/course-qt/640/360', 26.0, 11.00, 'C++'),
('Python 编程零基础入门', '从语法、函数、模块和文件操作开始，完成个人记账工具。', 'https://picsum.photos/seed/course-python-basic/640/360', 20.0, 7.00, 'Python'),
('Python 自动化办公', '使用 openpyxl、python-docx 和脚本批量处理表格、文档与文件。', 'https://picsum.photos/seed/course-python-office/640/360', 14.0, 6.00, 'Python'),
('Pandas 数据分析基础', '完成数据清洗、聚合、透视和时间序列分析，输出业务分析报告。', 'https://picsum.photos/seed/course-pandas/640/360', 18.0, 8.00, 'Python,人工智能'),
('Python 网络爬虫实践', '学习 HTTP、网页解析、请求限速和数据持久化的规范实现。', 'https://picsum.photos/seed/course-python-crawler/640/360', 16.0, 7.00, 'Python'),
('FastAPI 后端接口开发', '使用 FastAPI、Pydantic 和 SQLAlchemy 构建文档完整的 REST API。', 'https://picsum.photos/seed/course-fastapi/640/360', 22.0, 10.00, 'Python,数据库'),
('Python 数据可视化', '使用 Matplotlib、Seaborn 和 Plotly 制作常见业务图表与仪表盘。', 'https://picsum.photos/seed/course-python-visualization/640/360', 14.0, 7.00, 'Python'),
('HTML5 与 CSS3 网页制作', '从语义化标签、盒模型、Flex 和 Grid 开始制作响应式网站。', 'https://picsum.photos/seed/course-html-css/640/360', 20.0, 7.00, '前端开发'),
('JavaScript ES6 核心语法', '掌握作用域、闭包、Promise、模块化和异步请求等核心能力。', 'https://picsum.photos/seed/course-javascript/640/360', 22.0, 8.00, '前端开发'),
('TypeScript 工程实践', '为前端项目建立类型模型、泛型工具和可靠的接口调用层。', 'https://picsum.photos/seed/course-typescript/640/360', 16.0, 8.00, '前端开发'),
('Vue 3 企业级项目开发', '使用 Composition API、Vue Router、Pinia 和组件化完成管理平台。', 'https://picsum.photos/seed/course-vue3/640/360', 30.0, 12.00, '前端开发'),
('React 组件化开发', '学习 Hooks、状态管理、路由和表单，构建单页业务应用。', 'https://picsum.photos/seed/course-react/640/360', 28.0, 11.00, '前端开发'),
('前端工程化与性能优化', '掌握 Vite、代码分包、缓存策略、性能指标和线上问题定位。', 'https://picsum.photos/seed/course-frontend-performance/640/360', 18.0, 9.00, '前端开发'),
('MySQL 数据库基础', '学习表设计、增删改查、约束、连接查询和常用聚合分析。', 'https://picsum.photos/seed/course-mysql-basic/640/360', 22.0, 8.00, '数据库'),
('SQL 复杂查询实战', '通过订单、用户和库存场景练习子查询、窗口函数和公共表表达式。', 'https://picsum.photos/seed/course-sql-advanced/640/360', 18.0, 8.00, '数据库'),
('MySQL 索引与查询优化', '使用执行计划、索引设计和慢查询日志优化真实业务 SQL。', 'https://picsum.photos/seed/course-mysql-tuning/640/360', 16.0, 9.00, '数据库'),
('数据库事务与锁机制', '理解隔离级别、MVCC、死锁和高并发交易场景的数据一致性。', 'https://picsum.photos/seed/course-database-transaction/640/360', 14.0, 8.00, '数据库'),
('Redis 缓存应用实践', '掌握字符串、哈希、集合、过期策略和缓存穿透等常见方案。', 'https://picsum.photos/seed/course-redis/640/360', 18.0, 9.00, '数据库,Spring Boot'),
('关系数据库建模', '从业务需求提炼实体关系，完成规范化、字段设计和数据字典。', 'https://picsum.photos/seed/course-database-modeling/640/360', 14.0, 7.00, '数据库'),
('Spring Boot REST API 开发', '从项目配置、分层设计到统一响应和异常处理构建 REST 服务。', 'https://picsum.photos/seed/course-spring-rest/640/360', 24.0, 10.00, 'Spring Boot,Java'),
('Spring Boot 与 MyBatis-Plus', '完成实体映射、条件查询、分页、事务和常见数据访问功能。', 'https://picsum.photos/seed/course-mybatis-plus/640/360', 20.0, 9.00, 'Spring Boot,Java,数据库'),
('Spring Security 与 JWT 认证', '实现登录认证、令牌校验、角色权限和接口安全控制。', 'https://picsum.photos/seed/course-spring-security/640/360', 18.0, 10.00, 'Spring Boot,Java'),
('Spring Boot Redis 缓存实战', '在真实查询场景中实现缓存、失效更新、分布式锁和限流。', 'https://picsum.photos/seed/course-spring-redis/640/360', 16.0, 9.00, 'Spring Boot,Java,数据库'),
('Spring Boot 自动化测试', '使用 Spring Boot Test、MockMvc 和 Testcontainers 验证接口与数据层。', 'https://picsum.photos/seed/course-spring-test/640/360', 14.0, 8.00, 'Spring Boot,Java'),
('Spring Boot Docker 部署', '完成应用打包、Docker 镜像、环境变量配置和 Compose 部署。', 'https://picsum.photos/seed/course-spring-docker/640/360', 16.0, 9.00, 'Spring Boot,Java'),
('机器学习基础与实践', '学习监督学习、特征工程、模型评估，并完成分类预测项目。', 'https://picsum.photos/seed/course-machine-learning/640/360', 28.0, 12.00, '人工智能,Python'),
('深度学习入门', '理解神经网络、反向传播、卷积网络，并使用框架训练图像模型。', 'https://picsum.photos/seed/course-deep-learning/640/360', 30.0, 13.00, '人工智能,Python'),
('自然语言处理基础', '完成文本预处理、分类、关键词抽取和基础语义分析任务。', 'https://picsum.photos/seed/course-nlp/640/360', 24.0, 11.00, '人工智能,Python'),
('计算机视觉项目实践', '学习图像处理、目标检测和模型部署的完整项目流程。', 'https://picsum.photos/seed/course-computer-vision/640/360', 26.0, 12.00, '人工智能,Python'),
('推荐系统原理与实现', '使用协同过滤、内容推荐和排序指标构建课程推荐原型。', 'https://picsum.photos/seed/course-recommendation/640/360', 22.0, 11.00, '人工智能,Python,数据库'),
('生成式人工智能提示工程', '学习提示设计、结构化输出、知识检索和模型应用安全规范。', 'https://picsum.photos/seed/course-prompt-engineering/640/360', 12.0, 7.00, '人工智能'),
('区块链技术基础', '理解区块、哈希、共识机制、钱包和公私钥等核心概念。', 'https://picsum.photos/seed/course-blockchain-basic/640/360', 18.0, 8.00, '区块链'),
('Solidity 智能合约开发', '使用 Solidity 编写、测试和部署代币及业务智能合约。', 'https://picsum.photos/seed/course-solidity/640/360', 24.0, 11.00, '区块链'),
('智能合约安全审计基础', '识别重入、权限、整数和业务逻辑漏洞，建立安全检查清单。', 'https://picsum.photos/seed/course-contract-security/640/360', 18.0, 10.00, '区块链'),
('Hyperledger Fabric 联盟链实践', '搭建联盟链网络，编写链码并完成组织间业务数据流转。', 'https://picsum.photos/seed/course-fabric/640/360', 28.0, 13.00, '区块链'),
('区块链证书存证与验证', '将学习证书摘要上链，实现二维码查询、哈希校验和可信验证。', 'https://picsum.photos/seed/course-certificate-chain/640/360', 16.0, 9.00, '区块链,Java,Spring Boot'),
('Web3 DApp 前端开发', '连接浏览器钱包、调用智能合约并展示链上交易状态。', 'https://picsum.photos/seed/course-web3-dapp/640/360', 20.0, 10.00, '区块链,前端开发');

DROP TEMPORARY TABLE IF EXISTS incorrectly_encoded_course;
CREATE TEMPORARY TABLE incorrectly_encoded_course AS
SELECT course_item.id
FROM course course_item
JOIN seed_course seed ON seed.cover_url = course_item.cover_url
WHERE course_item.title <> seed.title;

DELETE FROM learning_archive WHERE course_id IN (SELECT id FROM incorrectly_encoded_course);
DELETE FROM learning_achievement WHERE certificate_id IN (
  SELECT id FROM learning_certificate WHERE course_id IN (SELECT id FROM incorrectly_encoded_course)
);
DELETE FROM learning_certificate WHERE course_id IN (SELECT id FROM incorrectly_encoded_course);
DELETE FROM user_course WHERE course_id IN (SELECT id FROM incorrectly_encoded_course);
DELETE FROM course_tag WHERE course_id IN (SELECT id FROM incorrectly_encoded_course);
DELETE FROM course WHERE id IN (SELECT id FROM incorrectly_encoded_course);
DROP TEMPORARY TABLE IF EXISTS incorrectly_encoded_course;

INSERT INTO course (org_id, publisher_id, title, description, cover_url, video_url, video_duration_seconds,
                    price_credit, price_money, duration_hours, credit_reward, status)
SELECT COALESCE(u.org_id, (SELECT MIN(id) FROM sys_organization)), u.id,
       seed.title, seed.description, seed.cover_url,
       CASE
         WHEN FIND_IN_SET('Spring Boot', seed.tags) > 0 THEN '/videos/spring-boot.mp4'
         WHEN FIND_IN_SET('人工智能', seed.tags) > 0 THEN '/videos/ai.mp4'
         WHEN FIND_IN_SET('区块链', seed.tags) > 0 THEN '/videos/blockchain.mp4'
         WHEN FIND_IN_SET('前端开发', seed.tags) > 0 THEN '/videos/frontend.mp4'
         WHEN FIND_IN_SET('数据库', seed.tags) > 0 THEN '/videos/database.mp4'
         WHEN FIND_IN_SET('Python', seed.tags) > 0 THEN '/videos/python.mp4'
         WHEN FIND_IN_SET('C++', seed.tags) > 0 THEN '/videos/cpp.mp4'
         ELSE '/videos/java.mp4'
       END,
       CASE
         WHEN FIND_IN_SET('Spring Boot', seed.tags) > 0 THEN 70
         WHEN FIND_IN_SET('人工智能', seed.tags) > 0 THEN 908
         WHEN FIND_IN_SET('区块链', seed.tags) > 0 THEN 88
         WHEN FIND_IN_SET('前端开发', seed.tags) > 0 THEN 448
         WHEN FIND_IN_SET('数据库', seed.tags) > 0 THEN 234
         WHEN FIND_IN_SET('Python', seed.tags) > 0 THEN 603
         WHEN FIND_IN_SET('C++', seed.tags) > 0 THEN 322
         ELSE 192
       END,
       0.00, 0.00,
       seed.duration_hours, seed.credit_reward, 1
FROM seed_course seed
JOIN sys_user u ON u.username = 'enterprise1'
WHERE NOT EXISTS (SELECT 1 FROM course existing WHERE existing.title = seed.title);

UPDATE course course_item
JOIN seed_course seed ON seed.title = course_item.title
SET course_item.description = seed.description,
    course_item.cover_url = seed.cover_url,
    course_item.video_url = CASE
      WHEN FIND_IN_SET('Spring Boot', seed.tags) > 0 THEN '/videos/spring-boot.mp4'
      WHEN FIND_IN_SET('人工智能', seed.tags) > 0 THEN '/videos/ai.mp4'
      WHEN FIND_IN_SET('区块链', seed.tags) > 0 THEN '/videos/blockchain.mp4'
      WHEN FIND_IN_SET('前端开发', seed.tags) > 0 THEN '/videos/frontend.mp4'
      WHEN FIND_IN_SET('数据库', seed.tags) > 0 THEN '/videos/database.mp4'
      WHEN FIND_IN_SET('Python', seed.tags) > 0 THEN '/videos/python.mp4'
      WHEN FIND_IN_SET('C++', seed.tags) > 0 THEN '/videos/cpp.mp4'
      ELSE '/videos/java.mp4'
    END,
    course_item.video_duration_seconds = CASE
      WHEN FIND_IN_SET('Spring Boot', seed.tags) > 0 THEN 70
      WHEN FIND_IN_SET('人工智能', seed.tags) > 0 THEN 908
      WHEN FIND_IN_SET('区块链', seed.tags) > 0 THEN 88
      WHEN FIND_IN_SET('前端开发', seed.tags) > 0 THEN 448
      WHEN FIND_IN_SET('数据库', seed.tags) > 0 THEN 234
      WHEN FIND_IN_SET('Python', seed.tags) > 0 THEN 603
      WHEN FIND_IN_SET('C++', seed.tags) > 0 THEN 322
      ELSE 192
    END,
    course_item.duration_hours = seed.duration_hours,
    course_item.credit_reward = seed.credit_reward,
    course_item.status = 1,
    course_item.deleted = 0;

DELETE relation FROM course_tag relation
JOIN course course_item ON course_item.id = relation.course_id
JOIN seed_course seed ON seed.title = course_item.title;

INSERT IGNORE INTO course_tag (course_id, tag_id)
SELECT course_item.id, tag.id
FROM seed_course seed
JOIN course course_item ON course_item.title = seed.title
JOIN sys_tag tag ON FIND_IN_SET(tag.name, seed.tags) > 0;

DROP TEMPORARY TABLE IF EXISTS seed_course;

DELETE FROM mall_product
WHERE name LIKE '[测试]%' OR name LIKE '[积分商城]%' OR name LIKE '[样本商品]%';

DROP TEMPORARY TABLE IF EXISTS seed_product;
CREATE TEMPORARY TABLE seed_product (
  category_name VARCHAR(100) NOT NULL,
  name VARCHAR(200) PRIMARY KEY,
  description TEXT NOT NULL,
  cover_url VARCHAR(255) NOT NULL,
  product_type TINYINT NOT NULL,
  price_credit DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

INSERT INTO seed_product (category_name, name, description, cover_url, product_type, price_credit, stock) VALUES
('课程资源', 'Java 核心技术电子书兑换券', '兑换一本 Java 核心技术方向正版电子书的电子券。', 'https://picsum.photos/seed/product-java-book/640/480', 2, 18.00, 100),
('课程资源', 'Spring Boot 项目实战课兑换码', '兑换 Spring Boot 企业项目实战在线课程学习资格。', 'https://picsum.photos/seed/product-spring-course/640/480', 3, 45.00, 80),
('课程资源', 'Python 数据分析训练营名额', '包含数据清洗、可视化和结业项目的线上训练营名额。', 'https://picsum.photos/seed/product-python-camp/640/480', 3, 55.00, 60),
('课程资源', 'C++ 算法刷题课程月卡', '兑换一个月 C++ 数据结构与算法刷题课程权限。', 'https://picsum.photos/seed/product-cpp-course/640/480', 3, 35.00, 90),
('课程资源', 'Vue 3 前端实战课程包', '包含 Vue 3、Pinia、路由和组件设计的课程包。', 'https://picsum.photos/seed/product-vue-course/640/480', 3, 40.00, 70),
('课程资源', 'MySQL 性能优化案例集', '包含执行计划、索引设计和慢查询优化案例的电子资料。', 'https://picsum.photos/seed/product-mysql-cases/640/480', 2, 22.00, 120),
('课程资源', '人工智能入门视频课兑换码', '兑换机器学习与生成式人工智能入门视频课程。', 'https://picsum.photos/seed/product-ai-course/640/480', 3, 48.00, 75),
('课程资源', '区块链技术学习资料包', '包含区块链基础、智能合约和联盟链实践资料。', 'https://picsum.photos/seed/product-blockchain-pack/640/480', 2, 28.00, 100),
('课程资源', '软件测试与质量保障课程包', '覆盖单元测试、接口测试和持续集成的在线课程包。', 'https://picsum.photos/seed/product-test-course/640/480', 3, 38.00, 80),
('学习用品', 'A5 网格学习笔记本', '适合记录代码思路、算法草稿和课程重点的网格笔记本。', 'https://picsum.photos/seed/product-notebook/640/480', 1, 12.00, 200),
('学习用品', '程序员主题鼠标垫', '大尺寸防滑鼠标垫，适合宿舍和办公室学习桌面。', 'https://picsum.photos/seed/product-mousepad/640/480', 1, 25.00, 120),
('学习用品', '双头荧光标记笔套装', '六色双头标记笔，用于教材重点和学习计划标注。', 'https://picsum.photos/seed/product-highlighter/640/480', 1, 10.00, 180),
('学习用品', '便携式笔记本电脑支架', '可折叠铝合金支架，改善长时间在线学习姿势。', 'https://picsum.photos/seed/product-laptop-stand/640/480', 1, 60.00, 60),
('学习用品', '静音无线鼠标', '适合图书馆和宿舍使用的便携静音无线鼠标。', 'https://picsum.photos/seed/product-mouse/640/480', 1, 45.00, 80),
('学习用品', 'Type-C 多功能转接器', '提供 HDMI、USB 和读卡接口，方便课程演示与实验。', 'https://picsum.photos/seed/product-usb-hub/640/480', 1, 75.00, 50),
('学习用品', '桌面番茄钟计时器', '用于专注学习和休息管理的实体倒计时器。', 'https://picsum.photos/seed/product-timer/640/480', 1, 30.00, 100),
('学习用品', '护眼阅读台灯', '三档色温与亮度调节，适合夜间阅读和编程学习。', 'https://picsum.photos/seed/product-lamp/640/480', 1, 90.00, 40),
('学习用品', '数据线收纳包', '分区收纳充电器、移动硬盘、数据线和转接头。', 'https://picsum.photos/seed/product-cable-bag/640/480', 1, 20.00, 130),
('虚拟商品', 'JetBrains IDE 快捷键手册', 'IntelliJ IDEA、WebStorm 和 PyCharm 常用快捷键电子手册。', 'https://picsum.photos/seed/product-ide-guide/640/480', 2, 8.00, 9999),
('虚拟商品', '程序员简历模板包', '提供后端、前端、测试和数据岗位的可编辑简历模板。', 'https://picsum.photos/seed/product-resume-template/640/480', 2, 12.00, 9999),
('虚拟商品', '技术面试题库电子版', '覆盖 Java、数据库、前端和计算机基础的面试练习题。', 'https://picsum.photos/seed/product-interview-bank/640/480', 2, 20.00, 9999),
('虚拟商品', '项目需求文档模板包', '包含需求说明、接口清单、测试用例和验收记录模板。', 'https://picsum.photos/seed/product-requirement-template/640/480', 2, 10.00, 9999),
('虚拟商品', '数据库设计文档模板', '提供数据字典、ER 图说明和表结构评审模板。', 'https://picsum.photos/seed/product-database-template/640/480', 2, 10.00, 9999),
('虚拟商品', '前端 UI 组件设计素材包', '包含后台管理、表单、图表和移动端界面设计素材。', 'https://picsum.photos/seed/product-ui-kit/640/480', 2, 18.00, 9999),
('虚拟商品', 'Git 常用命令速查表', '覆盖分支、合并、回退、标签和远程协作的电子速查表。', 'https://picsum.photos/seed/product-git-cheatsheet/640/480', 2, 6.00, 9999),
('虚拟商品', 'Linux 运维命令手册', '包含文件、网络、进程、日志和服务管理的常用命令。', 'https://picsum.photos/seed/product-linux-guide/640/480', 2, 10.00, 9999),
('兑换专区', '校园打印额度 50 页', '兑换校内合作打印点黑白打印额度五十页。', 'https://picsum.photos/seed/product-printing/640/480', 2, 15.00, 200),
('兑换专区', '咖啡饮品兑换券', '可在校园合作咖啡店兑换指定中杯饮品一杯。', 'https://picsum.photos/seed/product-coffee/640/480', 2, 20.00, 150),
('兑换专区', '校园书店 20 元代金券', '可在校园合作书店购买教材和学习用品时抵扣。', 'https://picsum.photos/seed/product-bookstore/640/480', 2, 35.00, 100),
('兑换专区', '共享自习室两小时券', '兑换合作自习室两小时独立座位使用时间。', 'https://picsum.photos/seed/product-study-room/640/480', 2, 25.00, 120),
('兑换专区', '校园文创帆布袋', '印有校园学习主题图案的环保帆布袋。', 'https://picsum.photos/seed/product-canvas-bag/640/480', 1, 40.00, 80),
('兑换专区', '图书馆逾期费抵扣券', '用于抵扣一次不超过十元的图书逾期费用。', 'https://picsum.photos/seed/product-library-coupon/640/480', 2, 18.00, 100),
('兑换专区', '运动场馆单次体验券', '兑换校内合作羽毛球馆或健身房单次体验。', 'https://picsum.photos/seed/product-sport-pass/640/480', 2, 30.00, 90),
('兑换专区', '学习主题徽章套装', '包含编程、阅读和终身学习主题的三枚金属徽章。', 'https://picsum.photos/seed/product-badges/640/480', 1, 28.00, 100),
('虚拟权益', '在线题库会员月卡', '一个月在线编程题库会员权益，支持题解和学习统计。', 'https://picsum.photos/seed/product-question-bank/640/480', 2, 30.00, 500),
('虚拟权益', '云端开发环境 20 小时', '兑换浏览器云端开发环境二十小时使用额度。', 'https://picsum.photos/seed/product-cloud-ide/640/480', 2, 35.00, 300),
('虚拟权益', '电子图书馆会员月卡', '一个月技术电子书和期刊阅读权限。', 'https://picsum.photos/seed/product-library-member/640/480', 2, 28.00, 400),
('虚拟权益', 'AI 学习助手使用额度', '兑换五十次课程答疑、知识总结和学习计划生成额度。', 'https://picsum.photos/seed/product-ai-assistant/640/480', 2, 25.00, 500),
('虚拟权益', '在线绘图工具专业版月卡', '用于流程图、架构图和思维导图制作的月度权益。', 'https://picsum.photos/seed/product-diagram-tool/640/480', 2, 22.00, 300),
('虚拟权益', '英语技术阅读训练月卡', '提供一个月技术英语词汇和文档阅读训练内容。', 'https://picsum.photos/seed/product-tech-english/640/480', 2, 24.00, 300),
('虚拟权益', '在线简历制作工具月卡', '使用专业模板制作、导出和维护技术简历。', 'https://picsum.photos/seed/product-resume-tool/640/480', 2, 20.00, 400),
('虚拟权益', '开发者社区高级会员月卡', '获得技术专栏、直播回放和问答优先回复权益。', 'https://picsum.photos/seed/product-community-member/640/480', 2, 32.00, 250),
('技术服务', '技术简历一对一诊断', '由企业导师进行一次三十分钟技术简历结构与内容诊断。', 'https://picsum.photos/seed/product-resume-review/640/480', 4, 45.00, 40),
('技术服务', '模拟技术面试一次', '完成一次四十五分钟岗位模拟面试并获得书面反馈。', 'https://picsum.photos/seed/product-mock-interview/640/480', 4, 70.00, 30),
('技术服务', 'GitHub 项目代码评审', '导师对一个学习项目的目录、代码质量和文档进行评审。', 'https://picsum.photos/seed/product-code-review/640/480', 4, 60.00, 35),
('技术服务', '学习路径规划咨询', '根据基础和目标岗位制定三个月学习路径与阶段目标。', 'https://picsum.photos/seed/product-learning-plan/640/480', 4, 50.00, 45),
('技术服务', '数据库设计评审', '对一个项目的数据模型、字段、索引和查询方案进行评审。', 'https://picsum.photos/seed/product-database-review/640/480', 4, 65.00, 25),
('技术服务', '前端页面体验评审', '从交互、响应式、可访问性和性能角度评审一个前端页面。', 'https://picsum.photos/seed/product-frontend-review/640/480', 4, 55.00, 30),
('技术服务', '后端接口设计评审', '评审 REST 接口命名、响应结构、鉴权和异常处理设计。', 'https://picsum.photos/seed/product-api-review/640/480', 4, 65.00, 30),
('技术服务', '职业方向咨询', '结合技能、项目和求职目标进行一次职业方向咨询。', 'https://picsum.photos/seed/product-career-consulting/640/480', 4, 55.00, 40);

DELETE product
FROM mall_product product
JOIN seed_product seed ON seed.cover_url = product.cover_url
WHERE product.name <> seed.name;

INSERT INTO mall_product (category_id, name, description, cover_url, product_type, ref_course_id, price_credit, price_money, stock, status)
SELECT category.id, seed.name, seed.description, seed.cover_url, seed.product_type,
       NULL, seed.price_credit, 0.00, seed.stock, 1
FROM seed_product seed
JOIN mall_category category ON category.name = seed.category_name
WHERE NOT EXISTS (SELECT 1 FROM mall_product existing WHERE existing.name = seed.name);

UPDATE mall_product product
JOIN seed_product seed ON seed.name = product.name
JOIN mall_category category ON category.name = seed.category_name
SET product.category_id = category.id,
    product.description = seed.description,
    product.cover_url = seed.cover_url,
    product.product_type = seed.product_type,
    product.price_credit = seed.price_credit,
    product.price_money = 0.00,
    product.stock = seed.stock,
    product.status = 1,
    product.deleted = 0;

UPDATE mall_product product
JOIN course ON course.title = CASE product.name
  WHEN 'Spring Boot 项目实战课兑换码' THEN 'Spring Boot REST API 开发'
  WHEN 'Python 数据分析训练营名额' THEN 'Pandas 数据分析基础'
  WHEN 'C++ 算法刷题课程月卡' THEN '数据结构与算法 C++ 实现'
  WHEN 'Vue 3 前端实战课程包' THEN 'Vue 3 企业级项目开发'
  WHEN '人工智能入门视频课兑换码' THEN '机器学习基础与实践'
  WHEN '软件测试与质量保障课程包' THEN 'Spring Boot 自动化测试'
  ELSE NULL
END
SET product.ref_course_id = course.id
WHERE product.product_type = 3;

DELETE category
FROM mall_category category
LEFT JOIN mall_product product ON product.category_id = category.id
WHERE product.id IS NULL
  AND category.name NOT IN ('课程资源', '学习用品', '虚拟商品', '兑换专区', '虚拟权益', '技术服务');

DROP TEMPORARY TABLE IF EXISTS seed_product;
