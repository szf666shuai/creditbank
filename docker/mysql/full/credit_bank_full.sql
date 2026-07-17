-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: credit_bank
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `credit_bank`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `credit_bank` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `credit_bank`;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `org_id` bigint NOT NULL COMMENT '主办企业/机构',
  `publisher_id` bigint NOT NULL COMMENT '发布人',
  `title` varchar(200) NOT NULL COMMENT '活动名称',
  `description` text COMMENT '活动详情',
  `location` varchar(255) DEFAULT NULL COMMENT '地点',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `max_participants` int DEFAULT NULL COMMENT '人数上限',
  `credit_reward` decimal(10,2) DEFAULT '0.00' COMMENT '参与奖励学分',
  `recognition_enabled` tinyint NOT NULL DEFAULT '0' COMMENT 'æ˜¯å¦æ”¯æŒæœºæž„å­¦åˆ†äº’è®¤',
  `recognition_credit` decimal(10,2) DEFAULT '0.00' COMMENT 'äº’è®¤æœºæž„å­¦åˆ†å€¼',
  `recognition_subject_id` bigint DEFAULT NULL COMMENT '互认发放科目',
  `status` tinyint DEFAULT '1' COMMENT '0取消 1报名中 2进行中 3已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_activity_org` (`org_id`),
  KEY `idx_activity_time` (`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=190 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (187,2,3,'[测试] Java 技术沙龙：Spring Boot 实践','企业工程师分享 Java 项目经验，主题包含 Spring Boot 开发与学分认定流程。','示例科技集团报告厅','2026-08-15 14:00:00','2026-08-15 17:00:00',80,5.00,0,0.00,NULL,1,'2026-07-17 19:39:13','2026-07-17 19:39:13',0),(188,2,3,'[测试] 校园编程马拉松 Java 赛道','48 小时编程挑战，Java 组题目涵盖算法与工程实现，优胜者奖励学分。','大学城创新中心','2026-09-01 09:00:00','2026-09-03 18:00:00',120,10.00,0,0.00,NULL,1,'2026-07-17 19:39:13','2026-07-17 19:39:13',0),(189,1,1,'[测试] 区块链技术分享会','介绍联盟链在学习成果存证中的应用，不涉及 Java 编程。','示例大学图书馆','2026-08-20 15:00:00','2026-08-20 17:00:00',60,3.00,0,0.00,NULL,1,'2026-07-17 19:39:13','2026-07-17 19:39:13',0);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_invitation`
--

DROP TABLE IF EXISTS `activity_invitation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_invitation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `from_user_id` bigint NOT NULL COMMENT '企业发送人',
  `to_user_id` bigint NOT NULL COMMENT '受邀学员',
  `message_id` bigint DEFAULT NULL COMMENT '关联私信',
  `status` tinyint DEFAULT '0' COMMENT '0待回复 1已接受 2已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_act_invite_to` (`to_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动参与邀请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_invitation`
--

LOCK TABLES `activity_invitation` WRITE;
/*!40000 ALTER TABLE `activity_invitation` DISABLE KEYS */;
INSERT INTO `activity_invitation` VALUES (1,118,3,2,15,1,'2026-07-15 11:26:38');
/*!40000 ALTER TABLE `activity_invitation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_registration`
--

DROP TABLE IF EXISTS `activity_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint DEFAULT '0' COMMENT '0已报名 1已签到 2已取消 3已拒绝邀请',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_reg_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_registration`
--

LOCK TABLES `activity_registration` WRITE;
/*!40000 ALTER TABLE `activity_registration` DISABLE KEYS */;
INSERT INTO `activity_registration` VALUES (4,179,2,0,NULL,'2026-07-17 13:58:30'),(5,188,2,0,NULL,'2026-07-17 21:03:13');
/*!40000 ALTER TABLE `activity_registration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_recommend_log`
--

DROP TABLE IF EXISTS `ai_recommend_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_recommend_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL,
  `recommend_type` varchar(50) NOT NULL COMMENT 'course/job/learning_path',
  `input_context` json DEFAULT NULL COMMENT '输入上下文',
  `output_result` json DEFAULT NULL COMMENT '推荐结果',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ai_rec_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI推荐记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_recommend_log`
--

LOCK TABLES `ai_recommend_log` WRITE;
/*!40000 ALTER TABLE `ai_recommend_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_recommend_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `org_id` bigint DEFAULT NULL COMMENT '发布机构/企业ID',
  `publisher_id` bigint DEFAULT NULL COMMENT '发布人用户ID',
  `title` varchar(200) NOT NULL COMMENT '课程标题',
  `description` text COMMENT '课程描述',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面',
  `video_url` varchar(1000) DEFAULT NULL COMMENT '????????????',
  `video_duration_seconds` int NOT NULL DEFAULT '0' COMMENT '??????(??',
  `price_credit` decimal(10,2) DEFAULT '0.00' COMMENT '学分定价',
  `price_money` decimal(10,2) DEFAULT '0.00' COMMENT '现金定价(模拟)',
  `duration_hours` decimal(6,1) DEFAULT NULL COMMENT '学时',
  `credit_reward` decimal(10,2) DEFAULT '0.00' COMMENT '完成奖励学分',
  `credit_value` decimal(10,2) NOT NULL DEFAULT '3.00',
  `tags` varchar(500) DEFAULT NULL COMMENT 'è¯¾ç¨‹æ ‡ç­¾',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '1入门 2初级 3中级 4高级',
  `duration_minutes` int NOT NULL DEFAULT '60' COMMENT '课程时长(分钟)',
  `credit_subject_id` bigint DEFAULT NULL COMMENT '学分科目',
  `org_credit_reward` decimal(10,2) DEFAULT '0.00' COMMENT '完成奖励机构学分',
  `status` tinyint DEFAULT '1' COMMENT '0下架 1上架',
  `approval_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1已通过 2已驳回',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_course_org` (`org_id`),
  KEY `idx_course_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习资源/课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (58,2,3,'C++ 内存管理与调试','定位内存泄漏、越界访问和未定义行为，掌握常用调试工具。','https://picsum.photos/seed/course-cpp-memory/640/360','/videos/cpp.mp4',322,0.00,0.00,14.0,8.00,3.00,'C++,算法',1,60,2,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(59,2,3,'C++ 程序设计入门','学习变量、指针、函数、结构体和基础输入输出，完成命令行通讯录。','https://picsum.photos/seed/course-cpp-basic/640/360','/videos/cpp.mp4',322,0.00,0.00,24.0,8.00,3.00,'C++,算法',1,60,2,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(60,2,3,'C++ 面向对象与模板','掌握类、继承、多态、运算符重载和函数模板、类模板。','https://picsum.photos/seed/course-cpp-oop/640/360','/videos/cpp.mp4',322,0.00,0.00,20.0,8.00,3.00,'C++,算法',1,60,2,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(61,2,3,'FastAPI 后端接口开发','使用 FastAPI、Pydantic 和 SQLAlchemy 构建文档完整的 REST API。','https://picsum.photos/seed/course-fastapi/640/360','/videos/database.mp4',234,0.00,0.00,22.0,10.00,3.00,'Python,数据分析',1,60,3,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(62,2,3,'HTML5 与 CSS3 网页制作','从语义化标签、盒模型、Flex 和 Grid 开始制作响应式网站。','https://picsum.photos/seed/course-html-css/640/360','/videos/frontend.mp4',448,0.00,0.00,20.0,7.00,3.00,'通用,职业技能',1,60,4,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(63,2,3,'Hyperledger Fabric 联盟链实践','搭建联盟链网络，编写链码并完成组织间业务数据流转。','https://picsum.photos/seed/course-fabric/640/360','/videos/blockchain.mp4',88,0.00,0.00,28.0,13.00,3.00,'区块链,分布式',1,60,4,4.60,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(64,2,3,'Java Web 项目开发','使用 Servlet、会话、过滤器和分层架构完成基础 Web 管理系统。','https://picsum.photos/seed/course-java-web/640/360','/videos/java.mp4',192,0.00,0.00,28.0,12.00,3.00,'Java,后端,编程',1,60,1,4.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(65,2,3,'Java 并发编程实战','学习线程池、锁、并发容器和 CompletableFuture，处理真实并发任务。','https://picsum.photos/seed/course-java-concurrency/640/360','/videos/java.mp4',192,0.00,0.00,22.0,10.00,3.00,'Java,后端,编程',1,60,1,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(66,2,3,'Java 程序设计基础','从变量、流程控制、数组和方法开始，完成学生成绩管理等控制台项目。','https://picsum.photos/seed/course-java-basic/640/360','/videos/java.mp4',192,0.00,0.00,24.0,8.00,3.00,'Java,后端,编程',1,60,1,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(67,2,3,'Java 集合框架与泛型','掌握 List、Set、Map、迭代器、泛型和 Stream API 的实际使用。','https://picsum.photos/seed/course-java-collections/640/360','/videos/java.mp4',192,0.00,0.00,16.0,6.00,3.00,'Java,后端,编程',1,60,1,2.10,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(68,2,3,'Java 面向对象编程','学习封装、继承、多态、接口和常用设计原则，完成图书借阅系统。','https://picsum.photos/seed/course-java-oop/640/360','/videos/java.mp4',192,0.00,0.00,20.0,8.00,3.00,'Java,后端,编程',1,60,1,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(69,2,3,'JavaScript ES6 核心语法','掌握作用域、闭包、Promise、模块化和异步请求等核心能力。','https://picsum.photos/seed/course-javascript/640/360','/videos/frontend.mp4',448,0.00,0.00,22.0,8.00,3.00,'Java,后端,编程',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(70,2,3,'JUnit 5 与 Mockito 单元测试','为业务服务编写可维护的单元测试、参数化测试和 Mock 测试。','https://picsum.photos/seed/course-java-test/640/360','/videos/java.mp4',192,0.00,0.00,12.0,6.00,3.00,'通用,职业技能',1,60,1,2.10,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(71,2,3,'JVM 原理与性能调优','理解类加载、内存区域、垃圾回收和常见线上性能分析方法。','https://picsum.photos/seed/course-jvm/640/360','/videos/java.mp4',192,0.00,0.00,18.0,10.00,3.00,'通用,职业技能',1,60,1,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(72,2,3,'MySQL 数据库基础','学习表设计、增删改查、约束、连接查询和常用聚合分析。','https://picsum.photos/seed/course-mysql-basic/640/360','/videos/database.mp4',234,0.00,0.00,22.0,8.00,3.00,'通用,职业技能',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(73,2,3,'MySQL 索引与查询优化','使用执行计划、索引设计和慢查询日志优化真实业务 SQL。','https://picsum.photos/seed/course-mysql-tuning/640/360','/videos/database.mp4',234,0.00,0.00,16.0,9.00,3.00,'通用,职业技能',1,60,4,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(74,2,3,'Pandas 数据分析基础','完成数据清洗、聚合、透视和时间序列分析，输出业务分析报告。','https://picsum.photos/seed/course-pandas/640/360','/videos/ai.mp4',908,0.00,0.00,18.0,8.00,3.00,'通用,职业技能',1,60,3,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(75,2,3,'Python 数据可视化','使用 Matplotlib、Seaborn 和 Plotly 制作常见业务图表与仪表盘。','https://picsum.photos/seed/course-python-visualization/640/360','/videos/python.mp4',603,0.00,0.00,14.0,7.00,3.00,'Python,数据分析',1,60,3,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(76,2,3,'Python 编程零基础入门','从语法、函数、模块和文件操作开始，完成个人记账工具。','https://picsum.photos/seed/course-python-basic/640/360','/videos/python.mp4',603,0.00,0.00,20.0,7.00,3.00,'Python,数据分析',1,60,3,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(77,2,3,'Python 网络爬虫实践','学习 HTTP、网页解析、请求限速和数据持久化的规范实现。','https://picsum.photos/seed/course-python-crawler/640/360','/videos/python.mp4',603,0.00,0.00,16.0,7.00,3.00,'Python,数据分析',1,60,3,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(78,2,3,'Python 自动化办公','使用 openpyxl、python-docx 和脚本批量处理表格、文档与文件。','https://picsum.photos/seed/course-python-office/640/360','/videos/python.mp4',603,0.00,0.00,14.0,6.00,3.00,'Python,数据分析',1,60,3,2.10,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(79,2,3,'Qt 桌面应用开发','使用 Qt Widgets、信号槽和网络模块完成跨平台桌面应用。','https://picsum.photos/seed/course-qt/640/360','/videos/cpp.mp4',322,0.00,0.00,26.0,11.00,3.00,'通用,职业技能',1,60,2,3.90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(80,2,3,'React 组件化开发','学习 Hooks、状态管理、路由和表单，构建单页业务应用。','https://picsum.photos/seed/course-react/640/360','/videos/frontend.mp4',448,0.00,0.00,28.0,11.00,3.00,'通用,职业技能',1,60,4,3.90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(81,2,3,'Redis 缓存应用实践','掌握字符串、哈希、集合、过期策略和缓存穿透等常见方案。','https://picsum.photos/seed/course-redis/640/360','/videos/spring-boot.mp4',70,0.00,0.00,18.0,9.00,3.00,'通用,职业技能',1,60,1,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(82,2,3,'Solidity 智能合约开发','使用 Solidity 编写、测试和部署代币及业务智能合约。','https://picsum.photos/seed/course-solidity/640/360','/videos/blockchain.mp4',88,0.00,0.00,24.0,11.00,3.00,'通用,职业技能',1,60,4,3.90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(83,2,3,'Spring Boot Docker 部署','完成应用打包、Docker 镜像、环境变量配置和 Compose 部署。','https://picsum.photos/seed/course-spring-docker/640/360','/videos/spring-boot.mp4',70,0.00,0.00,16.0,9.00,3.00,'Java,后端,编程',1,60,1,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(84,2,3,'Spring Boot Redis 缓存实战','在真实查询场景中实现缓存、失效更新、分布式锁和限流。','https://picsum.photos/seed/course-spring-redis/640/360','/videos/spring-boot.mp4',70,0.00,0.00,16.0,9.00,3.00,'Java,后端,编程',1,60,1,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(85,2,3,'Spring Boot REST API 开发','从项目配置、分层设计到统一响应和异常处理构建 REST 服务。','https://picsum.photos/seed/course-spring-rest/640/360','/videos/spring-boot.mp4',70,0.00,0.00,24.0,10.00,3.00,'Java,后端,编程',1,60,1,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(86,2,3,'Spring Boot 与 MyBatis-Plus','完成实体映射、条件查询、分页、事务和常见数据访问功能。','https://picsum.photos/seed/course-mybatis-plus/640/360','/videos/spring-boot.mp4',70,0.00,0.00,20.0,9.00,3.00,'Java,后端,编程',1,60,1,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(87,2,3,'Spring Boot 自动化测试','使用 Spring Boot Test、MockMvc 和 Testcontainers 验证接口与数据层。','https://picsum.photos/seed/course-spring-test/640/360','/videos/spring-boot.mp4',70,0.00,0.00,14.0,8.00,3.00,'Java,后端,编程',1,60,1,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(88,2,3,'Spring Security 与 JWT 认证','实现登录认证、令牌校验、角色权限和接口安全控制。','https://picsum.photos/seed/course-spring-security/640/360','/videos/spring-boot.mp4',70,0.00,0.00,18.0,10.00,3.00,'Java,后端,编程',1,60,1,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(89,2,3,'SQL 复杂查询实战','通过订单、用户和库存场景练习子查询、窗口函数和公共表表达式。','https://picsum.photos/seed/course-sql-advanced/640/360','/videos/database.mp4',234,0.00,0.00,18.0,8.00,3.00,'通用,职业技能',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(90,2,3,'STL 标准模板库实战','系统使用 vector、map、set、algorithm 和迭代器解决工程问题。','https://picsum.photos/seed/course-cpp-stl/640/360','/videos/cpp.mp4',322,0.00,0.00,16.0,7.00,3.00,'通用,职业技能',1,60,2,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(91,2,3,'TypeScript 工程实践','为前端项目建立类型模型、泛型工具和可靠的接口调用层。','https://picsum.photos/seed/course-typescript/640/360','/videos/frontend.mp4',448,0.00,0.00,16.0,8.00,3.00,'通用,职业技能',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(92,2,3,'Vue 3 企业级项目开发','使用 Composition API、Vue Router、Pinia 和组件化完成管理平台。','https://picsum.photos/seed/course-vue3/640/360','/videos/frontend.mp4',448,0.00,0.00,30.0,12.00,3.00,'通用,职业技能',1,60,4,4.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(93,2,3,'Web3 DApp 前端开发','连接浏览器钱包、调用智能合约并展示链上交易状态。','https://picsum.photos/seed/course-web3-dapp/640/360','/videos/blockchain.mp4',88,0.00,0.00,20.0,10.00,3.00,'通用,职业技能',1,60,4,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(94,2,3,'关系数据库建模','从业务需求提炼实体关系，完成规范化、字段设计和数据字典。','https://picsum.photos/seed/course-database-modeling/640/360','/videos/database.mp4',234,0.00,0.00,14.0,7.00,3.00,'通用,职业技能',1,60,4,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(95,2,3,'前端工程化与性能优化','掌握 Vite、代码分包、缓存策略、性能指标和线上问题定位。','https://picsum.photos/seed/course-frontend-performance/640/360','/videos/frontend.mp4',448,0.00,0.00,18.0,9.00,3.00,'通用,职业技能',1,60,4,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(96,2,3,'区块链技术基础','理解区块、哈希、共识机制、钱包和公私钥等核心概念。','https://picsum.photos/seed/course-blockchain-basic/640/360','/videos/blockchain.mp4',88,0.00,0.00,18.0,8.00,3.00,'区块链,分布式',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(97,2,3,'区块链证书存证与验证','将学习证书摘要上链，实现二维码查询、哈希校验和可信验证。','https://picsum.photos/seed/course-certificate-chain/640/360','/videos/spring-boot.mp4',70,0.00,0.00,16.0,9.00,3.00,'区块链,分布式',1,60,1,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(98,2,3,'推荐系统原理与实现','使用协同过滤、内容推荐和排序指标构建课程推荐原型。','https://picsum.photos/seed/course-recommendation/640/360','/videos/ai.mp4',908,0.00,0.00,22.0,11.00,3.00,'通用,职业技能',1,60,3,3.90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(99,2,3,'数据库事务与锁机制','理解隔离级别、MVCC、死锁和高并发交易场景的数据一致性。','https://picsum.photos/seed/course-database-transaction/640/360','/videos/database.mp4',234,0.00,0.00,14.0,8.00,3.00,'通用,职业技能',1,60,4,2.80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(100,2,3,'数据结构与算法 C++ 实现','用 C++ 实现链表、树、图、排序和查找算法，并分析复杂度。','https://picsum.photos/seed/course-cpp-dsa/640/360','/videos/cpp.mp4',322,0.00,0.00,30.0,12.00,3.00,'C++,算法',1,60,2,4.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(101,2,3,'智能合约安全审计基础','识别重入、权限、整数和业务逻辑漏洞，建立安全检查清单。','https://picsum.photos/seed/course-contract-security/640/360','/videos/blockchain.mp4',88,0.00,0.00,18.0,10.00,3.00,'通用,职业技能',1,60,4,3.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(102,2,3,'机器学习基础与实践','学习监督学习、特征工程、模型评估，并完成分类预测项目。','https://picsum.photos/seed/course-machine-learning/640/360','/videos/ai.mp4',908,0.00,0.00,28.0,12.00,3.00,'通用,职业技能',1,60,3,4.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(103,2,3,'深度学习入门','理解神经网络、反向传播、卷积网络，并使用框架训练图像模型。','https://picsum.photos/seed/course-deep-learning/640/360','/videos/ai.mp4',908,0.00,0.00,30.0,13.00,3.00,'通用,职业技能',1,60,3,4.60,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(104,2,3,'现代 C++17 编程','学习智能指针、移动语义、Lambda、结构化绑定和现代工程规范。','https://picsum.photos/seed/course-cpp17/640/360','/videos/cpp.mp4',322,0.00,0.00,20.0,9.00,3.00,'C++,算法',1,60,2,3.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(105,2,3,'生成式人工智能提示工程','学习提示设计、结构化输出、知识检索和模型应用安全规范。','https://picsum.photos/seed/course-prompt-engineering/640/360','/videos/ai.mp4',908,0.00,0.00,12.0,7.00,3.00,'AI,机器学习',1,60,4,2.50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(106,2,3,'自然语言处理基础','完成文本预处理、分类、关键词抽取和基础语义分析任务。','https://picsum.photos/seed/course-nlp/640/360','/videos/ai.mp4',908,0.00,0.00,24.0,11.00,3.00,'AI,机器学习',1,60,3,3.90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(107,2,3,'计算机视觉项目实践','学习图像处理、目标检测和模型部署的完整项目流程。','https://picsum.photos/seed/course-computer-vision/640/360','/videos/ai.mp4',908,0.00,0.00,26.0,12.00,3.00,'AI,机器学习',1,60,3,4.20,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-16 10:03:01',0),(214,1,3,'[演示] 高等数学与学分互认基础','面向跨校互认的数学基础课程，完成后可申请转换。','https://picsum.photos/seed/org1-math/640/360','/videos/spring-boot.mp4',1800,0.00,0.00,2.0,10.00,4.00,'数学,基础课,互认',2,120,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(215,1,3,'[演示] 数据结构跨校选修','高校开放选修课，标签匹配后可转换至科技集团等效课程。','https://picsum.photos/seed/org1-ds/640/360','/videos/spring-boot.mp4',2400,0.00,0.00,2.5,12.00,3.50,'数据结构,算法,编程',3,150,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(216,3,3,'[演示] Java 企业级开发实训','培训机构 Java 实训课，可按标签申请转换到科技集团课程学分。','https://picsum.photos/seed/org3-java/640/360','/videos/spring-boot.mp4',3600,0.00,0.00,3.0,15.00,4.00,'Java,后端,编程,实训',3,180,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(217,3,3,'[演示] Python 数据分析工作坊','短期工作坊，匹配 Python/数据分析标签转换规则。','https://picsum.photos/seed/org3-py/640/360','/videos/spring-boot.mp4',2700,0.00,0.00,1.5,9.00,2.50,'Python,数据分析',2,90,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(218,6,3,'[演示] 星河学院 · 云计算运维','职业院校云运维课程，演示跨机构转换。','https://picsum.photos/seed/org4-cloud/640/360','/videos/spring-boot.mp4',3000,0.00,0.00,2.0,11.00,3.00,'云计算,运维,Linux',2,120,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(219,7,3,'[演示] 云启 · 前端工程化实战','培训机构前端课程，标签：前端,工程化,JavaScript。','https://picsum.photos/seed/org5-fe/640/360','/videos/spring-boot.mp4',3200,0.00,0.00,2.5,10.00,3.00,'前端,工程化,JavaScript',2,140,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(220,8,3,'[演示] 江城开大 · 终身学习导论','开放大学通识课，可用于通用技能标签匹配。','https://picsum.photos/seed/org6-ll/640/360','/videos/spring-boot.mp4',1500,0.00,0.00,1.0,6.00,2.00,'通识,终身学习,通用',1,60,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(221,2,3,'[演示] 科技集团 · Java 后端认证课','本机构目标课程：接受外部 Java/后端标签转换。','https://picsum.photos/seed/org2-java-cert/640/360','/videos/spring-boot.mp4',4000,0.00,0.00,3.5,16.00,5.00,'Java,后端,认证',3,200,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(222,2,3,'[演示] 科技集团 · AI 应用实践课','本机构目标课程：接受 AI/机器学习标签转换。','https://picsum.photos/seed/org2-ai/640/360','/videos/spring-boot.mp4',3600,0.00,0.00,3.0,14.00,4.00,'AI,机器学习,实践',3,180,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(223,2,3,'[演示] 科技集团 · 数据分析入门课','本机构目标课程：接受 Python/数据分析标签转换。','https://picsum.photos/seed/org2-data/640/360','/videos/spring-boot.mp4',2400,0.00,0.00,2.0,10.00,3.00,'Python,数据分析',2,100,NULL,0.00,1,1,NULL,NULL,NULL,'2026-07-16 10:03:01','2026-07-16 10:03:01',0),(254,2,3,'c语言的开发与实战',NULL,NULL,NULL,0,0.00,0.00,NULL,1.00,1.00,NULL,1,60,NULL,0.00,0,2,NULL,1,'2026-07-17 08:26:08','2026-07-17 08:25:13','2026-07-17 08:25:13',0),(264,2,3,'111',NULL,NULL,NULL,0,0.00,0.00,NULL,1.00,1.00,NULL,1,60,NULL,0.00,0,0,NULL,NULL,NULL,'2026-07-17 21:06:50','2026-07-17 21:06:50',0);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_comment`
--

DROP TABLE IF EXISTS `course_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `course_id` bigint NOT NULL COMMENT '??ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `parent_id` bigint DEFAULT NULL COMMENT '???ID(??)',
  `content` varchar(500) NOT NULL COMMENT '????',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_course_comment_course` (`course_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=163 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_comment`
--

LOCK TABLES `course_comment` WRITE;
/*!40000 ALTER TABLE `course_comment` DISABLE KEYS */;
INSERT INTO `course_comment` VALUES (1,58,2,NULL,'?????????????????',12,'2026-07-09 21:09:31',0),(2,59,2,NULL,'?????????????????',12,'2026-07-09 21:09:31',0),(3,60,2,NULL,'?????????????????',12,'2026-07-09 21:09:31',0),(4,58,1,1,'?????????????????',3,'2026-07-11 15:09:31',0),(5,58,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-09 21:11:05',0),(6,59,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-09 21:11:05',0),(7,60,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-09 21:11:05',0),(8,58,1,5,'同感，这一节我反复看了两遍才理解。',3,'2026-07-11 15:11:05',0),(9,107,2,NULL,'111',0,'2026-07-11 21:42:03',1),(10,107,2,9,'222',0,'2026-07-11 21:42:14',0),(11,61,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 07:53:21',0),(12,62,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 07:53:21',0),(13,63,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 07:53:21',0),(14,59,1,6,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 01:53:21',0),(15,64,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:11:58',0),(16,65,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:11:58',0),(17,66,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:11:58',0),(18,60,1,7,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 03:11:58',0),(19,67,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:39:11',0),(20,68,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:39:11',0),(21,69,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:39:11',0),(22,61,1,11,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 03:39:11',0),(23,70,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:42:05',0),(24,71,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:42:05',0),(25,72,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:42:05',0),(26,62,1,12,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 03:42:05',0),(27,73,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:44:40',0),(28,74,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:44:40',0),(29,75,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 09:44:40',0),(30,63,1,13,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 03:44:40',0),(31,76,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 10:54:11',0),(32,77,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 10:54:11',0),(33,78,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 10:54:11',0),(34,64,1,15,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 04:54:11',0),(35,79,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 11:05:58',0),(36,80,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 11:05:58',0),(37,81,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 11:05:58',0),(38,65,1,16,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 05:05:58',0),(39,82,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 13:53:51',0),(40,83,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 13:53:51',0),(41,84,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 13:53:51',0),(42,66,1,17,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 07:53:51',0),(43,85,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:09:05',0),(44,86,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:09:05',0),(45,87,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:09:05',0),(46,67,1,19,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 08:09:05',0),(47,88,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:17:37',0),(48,89,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:17:37',0),(49,90,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-11 14:17:37',0),(50,68,1,20,'同感，这一节我反复看了两遍才理解。',3,'2026-07-13 08:17:37',0),(51,91,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 08:02:42',0),(52,92,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 08:02:42',0),(53,93,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 08:02:42',0),(54,69,1,21,'同感，这一节我反复看了两遍才理解。',3,'2026-07-14 02:02:42',0),(55,94,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:00:44',0),(56,95,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:00:44',0),(57,96,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:00:44',0),(58,70,1,23,'同感，这一节我反复看了两遍才理解。',3,'2026-07-14 07:00:44',0),(59,97,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:07:54',0),(60,98,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:07:54',0),(61,99,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:07:54',0),(62,71,1,24,'同感，这一节我反复看了两遍才理解。',3,'2026-07-14 07:07:54',0),(63,100,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:08:23',0),(64,101,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:08:23',0),(65,102,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:08:23',0),(66,72,1,25,'同感，这一节我反复看了两遍才理解。',3,'2026-07-14 07:08:23',0),(67,103,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:11:27',0),(68,104,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:11:27',0),(69,105,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-12 13:11:27',0),(70,73,1,27,'同感，这一节我反复看了两遍才理解。',3,'2026-07-14 07:11:27',0),(71,106,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-13 11:37:00',0),(72,107,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-13 11:37:00',0),(73,58,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 11:37:00',0),(74,74,1,28,'同感，这一节我反复看了两遍才理解。',3,'2026-07-15 05:37:00',0),(75,59,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:11:03',0),(76,60,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:11:03',0),(77,61,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:11:03',0),(78,75,1,29,'同感，这一节我反复看了两遍才理解。',3,'2026-07-15 14:11:03',0),(79,62,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:37:14',0),(80,63,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:37:14',0),(81,64,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:37:14',0),(82,76,1,31,'同感，这一节我反复看了两遍才理解。',3,'2026-07-15 14:37:14',0),(83,65,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:55:53',0),(84,66,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:55:53',0),(85,67,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-10 20:55:53',0),(86,77,1,32,'同感，这一节我反复看了两遍才理解。',3,'2026-07-15 14:55:53',0),(87,68,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:01:03',0),(88,69,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:01:03',0),(89,70,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:01:03',0),(90,78,1,33,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 02:01:03',0),(91,71,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:17:58',0),(92,72,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:17:58',0),(93,73,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:17:58',0),(94,79,1,35,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 02:17:58',0),(95,74,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:29:20',0),(96,75,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:29:20',0),(97,76,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 08:29:20',0),(98,80,1,36,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 02:29:20',0),(99,77,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:27:29',0),(100,78,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:27:29',0),(101,79,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:27:29',0),(102,81,1,37,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 03:27:29',0),(103,80,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:41:49',0),(104,81,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:41:49',0),(105,82,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:41:49',0),(106,82,1,39,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 03:41:49',0),(107,83,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:47:49',0),(108,84,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:47:49',0),(109,85,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 09:47:49',0),(110,83,1,40,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 03:47:49',0),(111,214,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:04:46',0),(112,215,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:04:46',0),(113,216,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:04:46',0),(114,84,1,41,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 04:04:46',0),(115,217,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:27:56',0),(116,218,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:27:56',0),(117,219,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:27:56',0),(118,85,1,43,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 04:27:56',0),(119,220,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:36:50',0),(120,221,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:36:50',0),(121,222,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:36:50',0),(122,86,1,44,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 04:36:50',0),(123,223,2,NULL,'这节课讲得很清楚，适合零基础入门。',12,'2026-07-14 10:56:29',0),(124,86,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 10:56:29',0),(125,87,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 10:56:29',0),(126,87,1,45,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 04:56:29',0),(127,88,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:17:51',0),(128,89,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:17:51',0),(129,90,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:17:51',0),(130,88,1,47,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 05:17:51',0),(131,91,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:22:01',0),(132,92,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:22:01',0),(133,93,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:22:01',0),(134,89,1,48,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 05:22:01',0),(135,94,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:28:55',0),(136,95,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:28:55',0),(137,96,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 11:28:55',0),(138,90,1,49,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 05:28:55',0),(139,97,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:06:33',0),(140,98,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:06:33',0),(141,99,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:06:33',0),(142,91,1,51,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 07:06:33',0),(143,100,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:21:01',0),(144,101,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:21:01',0),(145,102,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-11 13:21:01',0),(146,92,1,52,'同感，这一节我反复看了两遍才理解。',3,'2026-07-16 07:21:01',0),(147,103,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 08:05:30',0),(148,104,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 08:05:30',0),(149,105,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 08:05:30',0),(150,93,1,53,'同感，这一节我反复看了两遍才理解。',3,'2026-07-17 02:05:30',0),(151,106,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:27:47',0),(152,107,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:27:47',0),(153,214,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:27:47',0),(154,94,1,55,'同感，这一节我反复看了两遍才理解。',3,'2026-07-17 08:27:47',0),(155,215,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:31:38',0),(156,216,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:31:38',0),(157,217,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 14:31:38',0),(158,95,1,56,'同感，这一节我反复看了两遍才理解。',3,'2026-07-17 08:31:38',0),(159,218,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 19:39:14',0),(160,219,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 19:39:14',0),(161,220,2,NULL,'已收藏，准备二刷重点章节。',8,'2026-07-12 19:39:14',0),(162,96,1,57,'同感，这一节我反复看了两遍才理解。',3,'2026-07-17 13:39:14',0);
/*!40000 ALTER TABLE `course_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_comment_like`
--

DROP TABLE IF EXISTS `course_comment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_comment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_comment_like` (`comment_id`,`user_id`),
  KEY `idx_course_comment_like_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程评论点赞';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_comment_like`
--

LOCK TABLES `course_comment_like` WRITE;
/*!40000 ALTER TABLE `course_comment_like` DISABLE KEYS */;
/*!40000 ALTER TABLE `course_comment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_danmaku`
--

DROP TABLE IF EXISTS `course_danmaku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_danmaku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `course_id` bigint NOT NULL COMMENT '??ID',
  `user_id` bigint NOT NULL COMMENT '??ID',
  `content` varchar(100) NOT NULL COMMENT '????',
  `video_time_seconds` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '?????(?)',
  `color` varchar(16) NOT NULL DEFAULT '#ffffff' COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_course_danmaku_course` (`course_id`,`video_time_seconds`)
) ENGINE=InnoDB AUTO_INCREMENT=282 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_danmaku`
--

LOCK TABLES `course_danmaku` WRITE;
/*!40000 ALTER TABLE `course_danmaku` DISABLE KEYS */;
INSERT INTO `course_danmaku` VALUES (1,58,2,'????',8.00,'#ffffff','2026-07-11 21:09:31',0),(2,59,2,'????',8.00,'#ffffff','2026-07-11 21:09:31',0),(3,60,2,'????',8.00,'#ffffff','2026-07-11 21:09:31',0),(4,61,2,'????',8.00,'#ffffff','2026-07-11 21:09:31',0),(5,62,2,'????',8.00,'#ffffff','2026-07-11 21:09:31',0),(8,58,2,'打卡学习',8.00,'#ffffff','2026-07-11 21:11:05',0),(9,59,2,'打卡学习',8.00,'#ffffff','2026-07-11 21:11:05',0),(10,60,2,'打卡学习',8.00,'#ffffff','2026-07-11 21:11:05',0),(11,61,2,'打卡学习',8.00,'#ffffff','2026-07-11 21:11:05',0),(12,62,2,'打卡学习',8.00,'#ffffff','2026-07-11 21:11:05',0),(15,107,2,'111',0.00,'#ffffff','2026-07-11 21:16:00',1),(16,107,2,'111',7.55,'#ffffff','2026-07-11 21:51:42',1),(17,63,2,'打卡学习',8.00,'#ffffff','2026-07-13 07:53:21',0),(18,64,2,'打卡学习',8.00,'#ffffff','2026-07-13 07:53:21',0),(19,65,2,'打卡学习',8.00,'#ffffff','2026-07-13 07:53:21',0),(20,66,2,'打卡学习',8.00,'#ffffff','2026-07-13 07:53:21',0),(21,67,2,'打卡学习',8.00,'#ffffff','2026-07-13 07:53:21',0),(24,68,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:11:58',0),(25,69,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:11:58',0),(26,70,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:11:58',0),(27,71,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:11:58',0),(28,72,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:11:58',0),(31,73,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:39:11',0),(32,74,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:39:11',0),(33,75,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:39:11',0),(34,76,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:39:11',0),(35,77,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:39:11',0),(38,78,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:42:05',0),(39,79,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:42:05',0),(40,80,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:42:05',0),(41,81,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:42:05',0),(42,82,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:42:05',0),(45,83,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:44:40',0),(46,84,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:44:40',0),(47,85,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:44:40',0),(48,86,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:44:40',0),(49,87,2,'打卡学习',8.00,'#ffffff','2026-07-13 09:44:40',0),(52,88,2,'打卡学习',8.00,'#ffffff','2026-07-13 10:54:11',0),(53,89,2,'打卡学习',8.00,'#ffffff','2026-07-13 10:54:11',0),(54,90,2,'打卡学习',8.00,'#ffffff','2026-07-13 10:54:11',0),(55,91,2,'打卡学习',8.00,'#ffffff','2026-07-13 10:54:11',0),(56,92,2,'打卡学习',8.00,'#ffffff','2026-07-13 10:54:11',0),(59,93,2,'打卡学习',8.00,'#ffffff','2026-07-13 11:05:58',0),(60,94,2,'打卡学习',8.00,'#ffffff','2026-07-13 11:05:58',0),(61,95,2,'打卡学习',8.00,'#ffffff','2026-07-13 11:05:58',0),(62,96,2,'打卡学习',8.00,'#ffffff','2026-07-13 11:05:58',0),(63,97,2,'打卡学习',8.00,'#ffffff','2026-07-13 11:05:58',0),(66,98,2,'打卡学习',8.00,'#ffffff','2026-07-13 13:53:51',0),(67,99,2,'打卡学习',8.00,'#ffffff','2026-07-13 13:53:51',0),(68,100,2,'打卡学习',8.00,'#ffffff','2026-07-13 13:53:51',0),(69,101,2,'打卡学习',8.00,'#ffffff','2026-07-13 13:53:51',0),(70,102,2,'打卡学习',8.00,'#ffffff','2026-07-13 13:53:51',0),(73,103,2,'打卡学习',8.00,'#ffffff','2026-07-13 14:09:05',0),(74,104,2,'打卡学习',8.00,'#ffffff','2026-07-13 14:09:05',0),(75,105,2,'打卡学习',8.00,'#ffffff','2026-07-13 14:09:05',0),(76,106,2,'打卡学习',8.00,'#ffffff','2026-07-13 14:09:05',0),(77,107,2,'打卡学习',8.00,'#ffffff','2026-07-13 14:09:05',0),(80,58,2,'这里讲得很细',24.50,'#fb7299','2026-07-13 14:17:37',0),(81,59,2,'这里讲得很细',24.50,'#fb7299','2026-07-13 14:17:37',0),(82,60,2,'这里讲得很细',24.50,'#fb7299','2026-07-13 14:17:37',0),(83,61,2,'这里讲得很细',24.50,'#fb7299','2026-07-13 14:17:37',0),(84,62,2,'这里讲得很细',24.50,'#fb7299','2026-07-13 14:17:37',0),(85,63,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 08:02:42',0),(86,64,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 08:02:42',0),(87,65,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 08:02:42',0),(88,66,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 08:02:42',0),(89,67,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 08:02:42',0),(92,68,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:00:44',0),(93,69,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:00:44',0),(94,70,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:00:44',0),(95,71,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:00:44',0),(96,72,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:00:44',0),(99,73,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:07:54',0),(100,74,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:07:54',0),(101,75,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:07:54',0),(102,76,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:07:54',0),(103,77,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:07:54',0),(106,78,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:08:23',0),(107,79,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:08:23',0),(108,80,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:08:23',0),(109,81,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:08:23',0),(110,82,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:08:23',0),(113,83,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:11:27',0),(114,84,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:11:27',0),(115,85,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:11:27',0),(116,86,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:11:27',0),(117,87,2,'这里讲得很细',24.50,'#fb7299','2026-07-14 13:11:27',0),(120,107,2,'test',18.76,'#ffffff','2026-07-14 13:13:29',0),(121,107,2,'test',25.10,'#00d1b2','2026-07-14 13:13:35',0),(122,88,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 11:37:00',0),(123,89,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 11:37:00',0),(124,90,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 11:37:00',0),(125,91,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 11:37:00',0),(126,92,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 11:37:00',0),(129,107,2,'test',35.00,'#fb7299','2026-07-15 13:37:45',0),(130,93,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:11:03',0),(131,94,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:11:03',0),(132,95,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:11:03',0),(133,96,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:11:03',0),(134,97,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:11:03',0),(137,98,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:37:14',0),(138,99,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:37:14',0),(139,100,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:37:14',0),(140,101,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:37:14',0),(141,102,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:37:14',0),(144,103,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:55:53',0),(145,104,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:55:53',0),(146,105,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:55:53',0),(147,106,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:55:53',0),(148,107,2,'这里讲得很细',24.50,'#fb7299','2026-07-15 20:55:53',0),(149,58,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:01:03',0),(150,59,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:01:03',0),(151,60,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:01:03',0),(152,61,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:01:03',0),(153,62,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:01:03',0),(156,63,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:17:58',0),(157,64,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:17:58',0),(158,65,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:17:58',0),(159,66,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:17:58',0),(160,67,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:17:58',0),(163,68,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:29:20',0),(164,69,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:29:20',0),(165,70,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:29:20',0),(166,71,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:29:20',0),(167,72,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 08:29:20',0),(170,73,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:27:29',0),(171,74,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:27:29',0),(172,75,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:27:29',0),(173,76,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:27:29',0),(174,77,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:27:29',0),(177,78,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:41:49',0),(178,79,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:41:49',0),(179,80,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:41:49',0),(180,81,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:41:49',0),(181,82,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:41:49',0),(184,83,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:47:49',0),(185,84,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:47:49',0),(186,85,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:47:49',0),(187,86,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:47:49',0),(188,87,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 09:47:49',0),(191,214,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:04:46',0),(192,215,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:04:46',0),(193,216,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:04:46',0),(194,217,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:04:46',0),(195,218,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:04:46',0),(198,219,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:27:56',0),(199,220,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:27:56',0),(200,221,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:27:56',0),(201,222,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:27:56',0),(202,223,2,'打卡学习',8.00,'#ffffff','2026-07-16 10:27:56',0),(205,214,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:36:50',0),(206,215,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:36:50',0),(207,216,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:36:50',0),(208,217,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:36:50',0),(209,218,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:36:50',0),(212,219,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:56:29',0),(213,220,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:56:29',0),(214,221,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:56:29',0),(215,222,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:56:29',0),(216,223,2,'这里讲得很细',24.50,'#fb7299','2026-07-16 10:56:29',0),(219,88,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:17:51',0),(220,89,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:17:51',0),(221,90,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:17:51',0),(222,91,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:17:51',0),(223,92,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:17:51',0),(226,93,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:22:01',0),(227,94,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:22:01',0),(228,95,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:22:01',0),(229,96,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:22:01',0),(230,97,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:22:01',0),(233,98,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:28:55',0),(234,99,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:28:55',0),(235,100,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:28:55',0),(236,101,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:28:55',0),(237,102,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 11:28:55',0),(240,103,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:06:33',0),(241,104,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:06:33',0),(242,105,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:06:33',0),(243,106,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:06:33',0),(244,107,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:06:33',0),(247,214,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:21:01',0),(248,215,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:21:01',0),(249,216,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:21:01',0),(250,217,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:21:01',0),(251,218,2,'记笔记+1',45.00,'#6bcbff','2026-07-16 13:21:01',0),(252,219,2,'记笔记+1',45.00,'#6bcbff','2026-07-17 08:05:30',0),(253,220,2,'记笔记+1',45.00,'#6bcbff','2026-07-17 08:05:30',0),(254,221,2,'记笔记+1',45.00,'#6bcbff','2026-07-17 08:05:30',0),(255,222,2,'记笔记+1',45.00,'#6bcbff','2026-07-17 08:05:30',0),(256,223,2,'记笔记+1',45.00,'#6bcbff','2026-07-17 08:05:30',0),(259,223,2,'111',4.38,'#fb7299','2026-07-17 13:56:24',0),(260,58,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:27:47',0),(261,59,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:27:47',0),(262,60,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:27:47',0),(263,61,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:27:47',0),(264,62,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:27:47',0),(267,63,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:31:38',0),(268,64,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:31:38',0),(269,65,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:31:38',0),(270,66,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:31:38',0),(271,67,2,'原来如此！',72.00,'#ffd93d','2026-07-17 14:31:38',0),(274,68,2,'原来如此！',72.00,'#ffd93d','2026-07-17 19:39:14',0),(275,69,2,'原来如此！',72.00,'#ffd93d','2026-07-17 19:39:14',0),(276,70,2,'原来如此！',72.00,'#ffd93d','2026-07-17 19:39:14',0),(277,71,2,'原来如此！',72.00,'#ffd93d','2026-07-17 19:39:14',0),(278,72,2,'原来如此！',72.00,'#ffd93d','2026-07-17 19:39:14',0),(281,223,2,'222',7.14,'#ffffff','2026-07-17 21:01:34',0);
/*!40000 ALTER TABLE `course_danmaku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_episode`
--

DROP TABLE IF EXISTS `course_episode`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_episode` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `course_id` bigint NOT NULL COMMENT '??ID',
  `title` varchar(200) NOT NULL COMMENT '????',
  `video_url` varchar(1000) DEFAULT NULL COMMENT '????',
  `video_duration_seconds` int NOT NULL DEFAULT '0' COMMENT '????(?)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '??',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0?? 1??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_course_episode_course` (`course_id`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_episode`
--

LOCK TABLES `course_episode` WRITE;
/*!40000 ALTER TABLE `course_episode` DISABLE KEYS */;
INSERT INTO `course_episode` VALUES (1,58,'C++ 内存管理与调试 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(2,59,'C++ 程序设计入门 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(3,60,'C++ 面向对象与模板 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(4,61,'FastAPI 后端接口开发 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(5,62,'HTML5 与 CSS3 网页制作 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(6,63,'Hyperledger Fabric 联盟链实践 · 第1集','/videos/blockchain.mp4',88,1,1,'2026-07-11 21:09:33',0),(7,64,'Java Web 项目开发 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(8,65,'Java 并发编程实战 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(9,66,'Java 程序设计基础 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(10,67,'Java 集合框架与泛型 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(11,68,'Java 面向对象编程 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(12,69,'JavaScript ES6 核心语法 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(13,70,'JUnit 5 与 Mockito 单元测试 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(14,71,'JVM 原理与性能调优 · 第1集','/videos/java.mp4',192,1,1,'2026-07-11 21:09:33',0),(15,72,'MySQL 数据库基础 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(16,73,'MySQL 索引与查询优化 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(17,74,'Pandas 数据分析基础 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(18,75,'Python 数据可视化 · 第1集','/videos/python.mp4',603,1,1,'2026-07-11 21:09:33',0),(19,76,'Python 编程零基础入门 · 第1集','/videos/python.mp4',603,1,1,'2026-07-11 21:09:33',0),(20,77,'Python 网络爬虫实践 · 第1集','/videos/python.mp4',603,1,1,'2026-07-11 21:09:33',0),(21,78,'Python 自动化办公 · 第1集','/videos/python.mp4',603,1,1,'2026-07-11 21:09:33',0),(22,79,'Qt 桌面应用开发 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(23,80,'React 组件化开发 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(24,81,'Redis 缓存应用实践 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(25,82,'Solidity 智能合约开发 · 第1集','/videos/blockchain.mp4',88,1,1,'2026-07-11 21:09:33',0),(26,83,'Spring Boot Docker 部署 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(27,84,'Spring Boot Redis 缓存实战 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(28,85,'Spring Boot REST API 开发 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(29,86,'Spring Boot 与 MyBatis-Plus · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(30,87,'Spring Boot 自动化测试 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(31,88,'Spring Security 与 JWT 认证 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(32,89,'SQL 复杂查询实战 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(33,90,'STL 标准模板库实战 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(34,91,'TypeScript 工程实践 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(35,92,'Vue 3 企业级项目开发 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(36,93,'Web3 DApp 前端开发 · 第1集','/videos/blockchain.mp4',88,1,1,'2026-07-11 21:09:33',0),(37,94,'关系数据库建模 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(38,95,'前端工程化与性能优化 · 第1集','/videos/frontend.mp4',448,1,1,'2026-07-11 21:09:33',0),(39,96,'区块链技术基础 · 第1集','/videos/blockchain.mp4',88,1,1,'2026-07-11 21:09:33',0),(40,97,'区块链证书存证与验证 · 第1集','/videos/spring-boot.mp4',70,1,1,'2026-07-11 21:09:33',0),(41,98,'推荐系统原理与实现 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(42,99,'数据库事务与锁机制 · 第1集','/videos/database.mp4',234,1,1,'2026-07-11 21:09:33',0),(43,100,'数据结构与算法 C++ 实现 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(44,101,'智能合约安全审计基础 · 第1集','/videos/blockchain.mp4',88,1,1,'2026-07-11 21:09:33',0),(45,102,'机器学习基础与实践 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(46,103,'深度学习入门 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(47,104,'现代 C++17 编程 · 第1集','/videos/cpp.mp4',322,1,1,'2026-07-11 21:09:33',0),(48,105,'生成式人工智能提示工程 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(49,106,'自然语言处理基础 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(50,107,'计算机视觉项目实践 · 第1集','/videos/ai.mp4',908,1,1,'2026-07-11 21:09:33',0),(64,58,'C++ 内存管理与调试 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(65,59,'C++ 程序设计入门 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(66,60,'C++ 面向对象与模板 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(67,61,'FastAPI 后端接口开发 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(68,62,'HTML5 与 CSS3 网页制作 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(69,63,'Hyperledger Fabric 联盟链实践 · 第2集','/videos/java.mp4',88,2,1,'2026-07-11 21:09:33',0),(70,64,'Java Web 项目开发 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(71,65,'Java 并发编程实战 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(72,66,'Java 程序设计基础 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(73,67,'Java 集合框架与泛型 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(74,68,'Java 面向对象编程 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(75,69,'JavaScript ES6 核心语法 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(76,70,'JUnit 5 与 Mockito 单元测试 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(77,71,'JVM 原理与性能调优 · 第2集','/videos/spring-boot.mp4',192,2,1,'2026-07-11 21:09:33',0),(78,72,'MySQL 数据库基础 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(79,73,'MySQL 索引与查询优化 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(80,74,'Pandas 数据分析基础 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(81,75,'Python 数据可视化 · 第2集','/videos/database.mp4',192,2,1,'2026-07-11 21:09:33',0),(82,76,'Python 编程零基础入门 · 第2集','/videos/database.mp4',192,2,1,'2026-07-11 21:09:33',0),(83,77,'Python 网络爬虫实践 · 第2集','/videos/database.mp4',192,2,1,'2026-07-11 21:09:33',0),(84,78,'Python 自动化办公 · 第2集','/videos/database.mp4',192,2,1,'2026-07-11 21:09:33',0),(85,79,'Qt 桌面应用开发 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(86,80,'React 组件化开发 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(87,81,'Redis 缓存应用实践 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(88,82,'Solidity 智能合约开发 · 第2集','/videos/java.mp4',88,2,1,'2026-07-11 21:09:33',0),(89,83,'Spring Boot Docker 部署 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(90,84,'Spring Boot Redis 缓存实战 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(91,85,'Spring Boot REST API 开发 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(92,86,'Spring Boot 与 MyBatis-Plus · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(93,87,'Spring Boot 自动化测试 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(94,88,'Spring Security 与 JWT 认证 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(95,89,'SQL 复杂查询实战 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(96,90,'STL 标准模板库实战 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(97,91,'TypeScript 工程实践 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(98,92,'Vue 3 企业级项目开发 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(99,93,'Web3 DApp 前端开发 · 第2集','/videos/java.mp4',88,2,1,'2026-07-11 21:09:33',0),(100,94,'关系数据库建模 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(101,95,'前端工程化与性能优化 · 第2集','/videos/blockchain.mp4',192,2,1,'2026-07-11 21:09:33',0),(102,96,'区块链技术基础 · 第2集','/videos/java.mp4',88,2,1,'2026-07-11 21:09:33',0),(103,97,'区块链证书存证与验证 · 第2集','/videos/java.mp4',70,2,1,'2026-07-11 21:09:33',0),(104,98,'推荐系统原理与实现 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(105,99,'数据库事务与锁机制 · 第2集','/videos/java.mp4',234,2,1,'2026-07-11 21:09:33',0),(106,100,'数据结构与算法 C++ 实现 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(107,101,'智能合约安全审计基础 · 第2集','/videos/java.mp4',88,2,1,'2026-07-11 21:09:33',0),(108,102,'机器学习基础与实践 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(109,103,'深度学习入门 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(110,104,'现代 C++17 编程 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(111,105,'生成式人工智能提示工程 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(112,106,'自然语言处理基础 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(113,107,'计算机视觉项目实践 · 第2集','/videos/java.mp4',192,2,1,'2026-07-11 21:09:33',0),(114,214,'[演示] 高等数学与学分互认基础 · 第1集','/videos/spring-boot.mp4',1800,1,1,'2026-07-16 10:04:46',0),(115,215,'[演示] 数据结构跨校选修 · 第1集','/videos/spring-boot.mp4',2400,1,1,'2026-07-16 10:04:46',0),(116,216,'[演示] Java 企业级开发实训 · 第1集','/videos/spring-boot.mp4',3600,1,1,'2026-07-16 10:04:46',0),(117,217,'[演示] Python 数据分析工作坊 · 第1集','/videos/spring-boot.mp4',2700,1,1,'2026-07-16 10:04:46',0),(118,218,'[演示] 星河学院 · 云计算运维 · 第1集','/videos/spring-boot.mp4',3000,1,1,'2026-07-16 10:04:46',0),(119,219,'[演示] 云启 · 前端工程化实战 · 第1集','/videos/spring-boot.mp4',3200,1,1,'2026-07-16 10:04:46',0),(120,220,'[演示] 江城开大 · 终身学习导论 · 第1集','/videos/spring-boot.mp4',1500,1,1,'2026-07-16 10:04:46',0),(121,221,'[演示] 科技集团 · Java 后端认证课 · 第1集','/videos/spring-boot.mp4',4000,1,1,'2026-07-16 10:04:46',0),(122,222,'[演示] 科技集团 · AI 应用实践课 · 第1集','/videos/spring-boot.mp4',3600,1,1,'2026-07-16 10:04:46',0),(123,223,'[演示] 科技集团 · 数据分析入门课 · 第1集','/videos/spring-boot.mp4',2400,1,1,'2026-07-16 10:04:46',0),(129,214,'[演示] 高等数学与学分互认基础 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(130,215,'[演示] 数据结构跨校选修 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(131,216,'[演示] Java 企业级开发实训 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(132,217,'[演示] Python 数据分析工作坊 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(133,218,'[演示] 星河学院 · 云计算运维 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(134,219,'[演示] 云启 · 前端工程化实战 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(135,220,'[演示] 江城开大 · 终身学习导论 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(136,221,'[演示] 科技集团 · Java 后端认证课 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(137,222,'[演示] 科技集团 · AI 应用实践课 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0),(138,223,'[演示] 科技集团 · 数据分析入门课 · 第2集','/videos/java.mp4',70,2,1,'2026-07-16 10:04:46',0);
/*!40000 ALTER TABLE `course_episode` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_material`
--

DROP TABLE IF EXISTS `course_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `course_id` bigint NOT NULL COMMENT '??ID',
  `title` varchar(200) NOT NULL COMMENT '????',
  `file_type` varchar(30) NOT NULL DEFAULT 'pdf' COMMENT '????: pdf/ppt/code/link/zip',
  `file_url` varchar(1000) NOT NULL COMMENT '???????',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '??',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`),
  KEY `idx_course_material_course` (`course_id`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=496 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_material`
--

LOCK TABLES `course_material` WRITE;
/*!40000 ALTER TABLE `course_material` DISABLE KEYS */;
INSERT INTO `course_material` VALUES (1,58,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(2,58,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(3,58,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(4,58,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(5,59,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(6,59,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(7,59,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(8,59,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(9,60,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(10,60,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(11,60,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(12,60,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(13,61,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(14,61,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(15,61,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(16,61,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(17,62,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(18,62,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(19,62,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(20,62,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(21,63,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(22,63,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(23,63,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(24,63,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(25,64,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(26,64,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(27,64,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(28,64,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(29,65,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(30,65,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(31,65,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(32,65,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(33,66,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(34,66,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(35,66,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(36,66,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(37,67,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(38,67,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(39,67,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(40,67,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(41,68,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(42,68,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(43,68,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(44,68,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(45,69,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(46,69,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(47,69,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(48,69,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(49,70,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(50,70,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(51,70,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(52,70,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(53,71,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(54,71,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(55,71,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(56,71,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(57,72,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(58,72,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(59,72,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(60,72,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(61,73,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(62,73,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(63,73,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(64,73,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(65,74,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(66,74,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(67,74,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(68,74,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(69,75,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(70,75,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(71,75,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(72,75,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(73,76,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(74,76,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(75,76,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(76,76,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(77,77,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(78,77,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(79,77,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(80,77,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(81,78,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(82,78,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(83,78,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(84,78,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(85,79,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(86,79,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(87,79,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(88,79,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(89,80,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(90,80,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(91,80,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(92,80,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(93,81,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(94,81,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(95,81,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(96,81,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(97,82,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(98,82,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(99,82,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(100,82,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(101,83,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(102,83,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(103,83,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(104,83,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(105,84,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(106,84,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(107,84,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(108,84,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(109,85,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(110,85,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(111,85,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(112,85,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(113,86,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(114,86,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(115,86,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(116,86,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(117,87,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(118,87,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(119,87,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(120,87,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(121,88,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(122,88,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(123,88,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(124,88,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(125,89,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(126,89,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(127,89,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(128,89,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(129,90,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(130,90,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(131,90,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(132,90,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(133,91,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(134,91,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(135,91,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(136,91,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(137,92,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(138,92,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(139,92,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(140,92,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(141,93,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(142,93,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(143,93,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(144,93,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(145,94,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(146,94,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(147,94,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(148,94,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(149,95,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(150,95,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(151,95,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(152,95,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(153,96,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(154,96,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(155,96,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(156,96,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(157,97,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(158,97,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(159,97,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(160,97,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(161,98,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(162,98,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(163,98,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(164,98,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(165,99,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(166,99,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(167,99,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(168,99,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(169,100,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(170,100,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(171,100,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(172,100,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(173,101,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(174,101,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(175,101,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(176,101,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(177,102,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(178,102,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(179,102,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(180,102,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(181,103,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(182,103,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(183,103,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(184,103,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(185,104,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(186,104,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(187,104,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(188,104,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(189,105,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(190,105,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(191,105,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(192,105,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(193,106,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(194,106,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(195,106,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(196,106,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(197,107,'????.md','code','/materials/exercises.md',4,'2026-07-11 21:09:31',0),(198,107,'????.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:09:31',0),(199,107,'???????.png','link','/materials/mindmap.svg',2,'2026-07-11 21:09:31',0),(200,107,'????.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:09:31',0),(256,58,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(257,58,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(258,58,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(259,58,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(260,59,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(261,59,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(262,59,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(263,59,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(264,60,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(265,60,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(266,60,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(267,60,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(268,61,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(269,61,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(270,61,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(271,61,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(272,62,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(273,62,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(274,62,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(275,62,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(276,63,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(277,63,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(278,63,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(279,63,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(280,64,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(281,64,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(282,64,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(283,64,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(284,65,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(285,65,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(286,65,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(287,65,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(288,66,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(289,66,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(290,66,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(291,66,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(292,67,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(293,67,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(294,67,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(295,67,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(296,68,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(297,68,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(298,68,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(299,68,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(300,69,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(301,69,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(302,69,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(303,69,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(304,70,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(305,70,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(306,70,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(307,70,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(308,71,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(309,71,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(310,71,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(311,71,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(312,72,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(313,72,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(314,72,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(315,72,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(316,73,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(317,73,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(318,73,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(319,73,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(320,74,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(321,74,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(322,74,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(323,74,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(324,75,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(325,75,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(326,75,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(327,75,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(328,76,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(329,76,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(330,76,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(331,76,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(332,77,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(333,77,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(334,77,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(335,77,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(336,78,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(337,78,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(338,78,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(339,78,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(340,79,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(341,79,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(342,79,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(343,79,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(344,80,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(345,80,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(346,80,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(347,80,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(348,81,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(349,81,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(350,81,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(351,81,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(352,82,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(353,82,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(354,82,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(355,82,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(356,83,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(357,83,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(358,83,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(359,83,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(360,84,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(361,84,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(362,84,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(363,84,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(364,85,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(365,85,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(366,85,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(367,85,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(368,86,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(369,86,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(370,86,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(371,86,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(372,87,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(373,87,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(374,87,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(375,87,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(376,88,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(377,88,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(378,88,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(379,88,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(380,89,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(381,89,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(382,89,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(383,89,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(384,90,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(385,90,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(386,90,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(387,90,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(388,91,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(389,91,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(390,91,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(391,91,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(392,92,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(393,92,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(394,92,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(395,92,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(396,93,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(397,93,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(398,93,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(399,93,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(400,94,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(401,94,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(402,94,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(403,94,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(404,95,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(405,95,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(406,95,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(407,95,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(408,96,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(409,96,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(410,96,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(411,96,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(412,97,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(413,97,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(414,97,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(415,97,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(416,98,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(417,98,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(418,98,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(419,98,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(420,99,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(421,99,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(422,99,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(423,99,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(424,100,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(425,100,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(426,100,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(427,100,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(428,101,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(429,101,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(430,101,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(431,101,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(432,102,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(433,102,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(434,102,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(435,102,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(436,103,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(437,103,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(438,103,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(439,103,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(440,104,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(441,104,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(442,104,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(443,104,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(444,105,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(445,105,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(446,105,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(447,105,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(448,106,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(449,106,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(450,106,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(451,106,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(452,107,'课后练习.md','code','/materials/exercises.md',4,'2026-07-11 21:11:05',0),(453,107,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-11 21:11:05',0),(454,107,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-11 21:11:05',0),(455,107,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-11 21:11:05',0),(456,214,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(457,214,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(458,214,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(459,214,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(460,215,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(461,215,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(462,215,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(463,215,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(464,216,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(465,216,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(466,216,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(467,216,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(468,217,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(469,217,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(470,217,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(471,217,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(472,218,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(473,218,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(474,218,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(475,218,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(476,219,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(477,219,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(478,219,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(479,219,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(480,220,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(481,220,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(482,220,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(483,220,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(484,221,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(485,221,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(486,221,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(487,221,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(488,222,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(489,222,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(490,222,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(491,222,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0),(492,223,'课后练习.md','code','/materials/exercises.md',4,'2026-07-16 10:04:46',0),(493,223,'示例代码.zip','zip','/materials/sample-code.zip',3,'2026-07-16 10:04:46',0),(494,223,'知识点思维导图.png','link','/materials/mindmap.svg',2,'2026-07-16 10:04:46',0),(495,223,'课程大纲.pdf','pdf','/materials/course-outline.pdf',1,'2026-07-16 10:04:46',0);
/*!40000 ALTER TABLE `course_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_tag`
--

DROP TABLE IF EXISTS `course_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_tag` (
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`course_id`,`tag_id`),
  KEY `idx_ct_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程标签关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_tag`
--

LOCK TABLES `course_tag` WRITE;
/*!40000 ALTER TABLE `course_tag` DISABLE KEYS */;
INSERT INTO `course_tag` VALUES (64,1),(65,1),(66,1),(67,1),(68,1),(70,1),(71,1),(83,1),(84,1),(85,1),(86,1),(87,1),(88,1),(97,1),(58,2),(59,2),(60,2),(79,2),(90,2),(100,2),(104,2),(61,3),(74,3),(75,3),(76,3),(77,3),(78,3),(98,3),(102,3),(103,3),(106,3),(107,3),(62,4),(69,4),(80,4),(91,4),(92,4),(93,4),(95,4),(61,5),(72,5),(73,5),(81,5),(84,5),(86,5),(89,5),(94,5),(98,5),(99,5),(81,6),(83,6),(84,6),(85,6),(86,6),(87,6),(88,6),(97,6),(74,7),(98,7),(102,7),(103,7),(105,7),(106,7),(107,7),(63,8),(82,8),(93,8),(96,8),(97,8),(101,8);
/*!40000 ALTER TABLE `course_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_account`
--

DROP TABLE IF EXISTS `credit_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '当前学分余额',
  `total_earned` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计获取',
  `total_spent` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '累计消耗',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学分账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_account`
--

LOCK TABLES `credit_account` WRITE;
/*!40000 ALTER TABLE `credit_account` DISABLE KEYS */;
INSERT INTO `credit_account` VALUES (1,2,41.80,102.80,61.00,'2026-07-07 21:31:38','2026-07-15 20:11:34');
/*!40000 ALTER TABLE `credit_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_certificate_claim`
--

DROP TABLE IF EXISTS `credit_certificate_claim`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_certificate_claim` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `rule_id` bigint NOT NULL,
  `org_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `credit_spent` decimal(12,2) NOT NULL,
  `achievement_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_rule` (`user_id`,`rule_id`),
  KEY `idx_claim_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_certificate_claim`
--

LOCK TABLES `credit_certificate_claim` WRITE;
/*!40000 ALTER TABLE `credit_certificate_claim` DISABLE KEYS */;
INSERT INTO `credit_certificate_claim` VALUES (2,2,1,2,1,20.00,15,'2026-07-15 20:49:42');
/*!40000 ALTER TABLE `credit_certificate_claim` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_certificate_rule`
--

DROP TABLE IF EXISTS `credit_certificate_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_certificate_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` bigint NOT NULL DEFAULT '0',
  `subject_id` bigint NOT NULL,
  `threshold` decimal(12,2) NOT NULL COMMENT '????',
  `title` varchar(200) NOT NULL COMMENT '????',
  `description` varchar(500) DEFAULT NULL,
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cert_rule` (`org_id`,`subject_id`,`title`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_certificate_rule`
--

LOCK TABLES `credit_certificate_rule` WRITE;
/*!40000 ALTER TABLE `credit_certificate_rule` DISABLE KEYS */;
INSERT INTO `credit_certificate_rule` VALUES (1,2,1,20.00,'Java 应用能力合格证','在示例科技集团累计 Java 学分达 20 可领取',1,'2026-07-15 20:26:25'),(2,3,1,15.00,'Java 实训结业证','在示例培训机构累计 Java 学分达 15 可领取',1,'2026-07-15 20:26:25');
/*!40000 ALTER TABLE `credit_certificate_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_recognition_rule`
--

DROP TABLE IF EXISTS `credit_recognition_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_recognition_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_org_id` bigint NOT NULL,
  `to_org_id` bigint NOT NULL,
  `from_subject_id` bigint NOT NULL,
  `to_subject_id` bigint NOT NULL,
  `rate` decimal(8,4) NOT NULL DEFAULT '1.0000' COMMENT '???? = ???? * rate',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `remark` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recog` (`from_org_id`,`to_org_id`,`from_subject_id`,`to_subject_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????/??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_recognition_rule`
--

LOCK TABLES `credit_recognition_rule` WRITE;
/*!40000 ALTER TABLE `credit_recognition_rule` DISABLE KEYS */;
INSERT INTO `credit_recognition_rule` VALUES (1,2,3,1,1,1.0000,1,'科技集团 Java → 培训机构 Java','2026-07-15 20:26:25','2026-07-15 20:48:16'),(2,3,2,1,2,0.6000,1,'培训机构 Java → 科技集团 C语言','2026-07-15 20:26:25','2026-07-15 20:48:16'),(5,2,2,1,2,0.7000,1,'同机构科目换算：科技集团 Java → C语言','2026-07-15 20:26:25','2026-07-15 20:48:16'),(6,0,2,4,4,1.0000,1,'平台通用 → 科技集团通用','2026-07-15 20:26:25','2026-07-15 20:48:16'),(7,2,6,1,1,0.9000,1,'科技集团 Java → 星河职业学院 Java','2026-07-15 20:27:13','2026-07-15 20:48:16'),(8,7,8,3,3,0.8500,1,'云启培训 Python → 江城开放大学 Python','2026-07-15 20:27:13','2026-07-15 20:48:16');
/*!40000 ALTER TABLE `credit_recognition_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_rule`
--

DROP TABLE IF EXISTS `credit_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_code` varchar(50) NOT NULL COMMENT '规则编码',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `amount` decimal(10,2) NOT NULL COMMENT '学分变动值(正为获取负为消耗)',
  `biz_type` varchar(50) NOT NULL COMMENT '业务: post/comment/like/course_buy/course_complete等',
  `description` varchar(255) DEFAULT NULL COMMENT '说明',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rule_code` (`rule_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学分规则配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_rule`
--

LOCK TABLES `credit_rule` WRITE;
/*!40000 ALTER TABLE `credit_rule` DISABLE KEYS */;
INSERT INTO `credit_rule` VALUES (1,'POST_CREATE','发帖奖励',2.00,'post','发布论坛帖子',1,'2026-07-07 21:31:38'),(2,'REPLY_CREATE','回复奖励',1.00,'reply','回复帖子',1,'2026-07-07 21:31:38'),(3,'POST_LIKE','被点赞奖励',0.50,'like_received','帖子/回复被点赞',1,'2026-07-07 21:31:38'),(4,'COURSE_COMPLETE','完成课程',10.00,'course_complete','完成一门课程',1,'2026-07-07 21:31:38'),(5,'ACTIVITY_CHECKIN','活动签到',5.00,'activity_checkin','活动签到奖励',1,'2026-07-07 21:31:38'),(6,'LEARNING_CHECKIN','??????',2.00,'learning_checkin','??????????',1,'2026-07-11 21:09:33');
/*!40000 ALTER TABLE `credit_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_subject`
--

DROP TABLE IF EXISTS `credit_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_subject` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `code` varchar(40) NOT NULL COMMENT '????',
  `name` varchar(50) NOT NULL COMMENT '????',
  `description` varchar(255) DEFAULT NULL COMMENT '??',
  `sort_order` int DEFAULT '0',
  `enabled` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_subject`
--

LOCK TABLES `credit_subject` WRITE;
/*!40000 ALTER TABLE `credit_subject` DISABLE KEYS */;
INSERT INTO `credit_subject` VALUES (1,'JAVA','Java','Java ????',1,1,'2026-07-15 20:26:25'),(2,'C','C/C++','C / C++ 方向学分',2,1,'2026-07-15 20:26:25'),(3,'PYTHON','Python','Python ????',3,1,'2026-07-15 20:26:25'),(4,'GENERAL','通用','????',99,1,'2026-07-15 20:26:25');
/*!40000 ALTER TABLE `credit_subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_transaction`
--

DROP TABLE IF EXISTS `credit_transaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型: 1获取 2转换 3增长 4消耗',
  `amount` decimal(12,2) NOT NULL COMMENT '变动学分(正负)',
  `balance_after` decimal(12,2) DEFAULT NULL COMMENT '变动后余额',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `source` varchar(200) DEFAULT NULL COMMENT '来源/用途说明',
  `ref_type` varchar(50) DEFAULT NULL COMMENT '关联业务表类型',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_credit_user` (`user_id`),
  KEY `idx_credit_biz` (`biz_type`,`ref_type`,`ref_id`),
  KEY `idx_credit_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学分流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_transaction`
--

LOCK TABLES `credit_transaction` WRITE;
/*!40000 ALTER TABLE `credit_transaction` DISABLE KEYS */;
INSERT INTO `credit_transaction` VALUES (16,2,1,20.00,20.00,'register','[画像演示] 注册奖励',NULL,NULL,'2026-05-26 22:13:07'),(17,2,1,10.00,30.00,'complete_course','[画像演示] 完成 Java 基础课程',NULL,NULL,'2026-06-30 22:13:07'),(18,2,1,5.00,35.00,'activity_checkin','[画像演示] Java 技术沙龙签到',NULL,NULL,'2026-06-25 22:13:07'),(19,2,1,3.00,38.00,'forum_post','[画像演示] 论坛发帖奖励',NULL,NULL,'2026-07-02 22:13:07'),(20,2,1,12.00,50.00,'daily_checkin','[画像演示] 累计签到奖励',NULL,NULL,'2026-07-07 22:13:07'),(21,2,4,-10.00,40.00,'mall_order','积分商城订单支付: MO2026071115335621B7DA8B','mall_order',1,'2026-07-11 15:34:00'),(22,2,1,9.60,49.60,'course_complete','完成学习资源: 区块链技术基础','course',96,'2026-07-11 15:36:23'),(23,2,1,14.40,64.00,'course_complete','完成学习资源: 数据结构与算法 C++ 实现','course',100,'2026-07-11 16:51:28'),(24,2,4,-6.00,58.00,'mall_order','积分商城订单支付: MO202607131004087932EA15','mall_order',4,'2026-07-13 10:04:09'),(25,2,4,-45.00,13.00,'mall_order','积分商城订单支付: MO20260714083239EEC993AC','mall_order',5,'2026-07-14 08:32:39'),(26,2,1,2.40,15.40,'learning_checkin','课程学习打卡','learning_checkin',1,'2026-07-14 13:16:08'),(27,2,1,2.40,17.80,'learning_checkin','课程学习打卡','learning_checkin',2,'2026-07-15 13:37:51'),(28,2,2,24.00,41.80,'org_credit_convert','机构学分互认（示例培训机构）：【互认测试】跨机构课程互认学分 ×1.2','achievement',13,'2026-07-15 20:11:34');
/*!40000 ALTER TABLE `credit_transaction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_transfer_application`
--

DROP TABLE IF EXISTS `credit_transfer_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_transfer_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `source_type` tinyint NOT NULL DEFAULT '1',
  `source_course_id` bigint DEFAULT NULL,
  `source_achievement_id` bigint DEFAULT NULL,
  `source_org_id` bigint NOT NULL,
  `source_credit` decimal(10,2) NOT NULL,
  `target_type` tinyint NOT NULL DEFAULT '1',
  `target_course_id` bigint DEFAULT NULL,
  `target_certificate_id` bigint DEFAULT NULL,
  `target_achievement_id` bigint DEFAULT NULL,
  `target_org_id` bigint NOT NULL,
  `apply_reason` varchar(500) DEFAULT NULL,
  `ai_suggestion` varchar(20) DEFAULT NULL COMMENT 'AI???? approve/reject/uncertain',
  `ai_reason` varchar(500) DEFAULT NULL COMMENT 'AI????',
  `ai_llm_used` tinyint DEFAULT '0' COMMENT '??????? 0?1?',
  `ai_screen_time` datetime DEFAULT NULL COMMENT 'AI????',
  `status` tinyint NOT NULL DEFAULT '0',
  `reviewer_id` bigint DEFAULT NULL,
  `review_comment` varchar(500) DEFAULT NULL,
  `actual_credit` decimal(10,2) DEFAULT NULL,
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `review_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_transfer_app_user` (`user_id`,`status`),
  KEY `idx_transfer_app_org` (`target_org_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_transfer_application`
--

LOCK TABLES `credit_transfer_application` WRITE;
/*!40000 ALTER TABLE `credit_transfer_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `credit_transfer_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credit_transfer_rule`
--

DROP TABLE IF EXISTS `credit_transfer_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credit_transfer_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` bigint NOT NULL,
  `source_type` tinyint NOT NULL DEFAULT '1',
  `source_course_id` bigint DEFAULT NULL,
  `source_tags` varchar(500) DEFAULT NULL,
  `target_type` tinyint NOT NULL DEFAULT '1',
  `target_course_id` bigint DEFAULT NULL,
  `target_certificate_id` bigint DEFAULT NULL,
  `target_achievement_id` bigint DEFAULT NULL,
  `target_org_id` bigint DEFAULT NULL,
  `credit_ratio` decimal(10,2) NOT NULL DEFAULT '1.00',
  `description` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_transfer_rule_org` (`org_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credit_transfer_rule`
--

LOCK TABLES `credit_transfer_rule` WRITE;
/*!40000 ALTER TABLE `credit_transfer_rule` DISABLE KEYS */;
INSERT INTO `credit_transfer_rule` VALUES (1,2,1,NULL,'Java,后端,编程',1,221,NULL,NULL,2,1.00,'[演示] 外部 Java/后端课程 → 本机构 Java 后端认证课',1,'2026-07-16 10:03:01','2026-07-16 10:03:01'),(2,2,1,NULL,'AI,机器学习',1,222,NULL,NULL,2,0.90,'[演示] 外部 AI/机器学习课程 → 本机构 AI 应用实践课（0.9 倍）',1,'2026-07-16 10:03:01','2026-07-16 10:03:01'),(3,2,1,NULL,'Python,数据分析',1,223,NULL,NULL,2,1.00,'[演示] 外部 Python/数据分析课程 → 本机构数据分析入门课',1,'2026-07-16 10:03:02','2026-07-16 10:03:02'),(4,2,2,NULL,'证书,职业资格',2,NULL,NULL,NULL,2,0.80,'[演示] 外部职业资格类学习成果 → 本机构等效成果（0.8 倍）',1,'2026-07-16 10:03:02','2026-07-16 10:03:02'),(6,1,1,NULL,'AI,机器学习',1,215,NULL,NULL,1,1.00,'[演示] 外部 AI/机器学习课程 → 本校数据结构跨校选修',1,'2026-07-16 10:34:47','2026-07-16 10:34:47'),(7,3,1,NULL,'AI,机器学习,Java,后端',1,216,NULL,NULL,3,0.95,'[演示] 外部 AI/Java 课程 → 本机构 Java 企业级开发实训',1,'2026-07-16 10:34:47','2026-07-16 10:34:47');
/*!40000 ALTER TABLE `credit_transfer_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_board`
--

DROP TABLE IF EXISTS `forum_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_board` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '板块ID',
  `name` varchar(100) NOT NULL COMMENT '板块名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛板块';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_board`
--

LOCK TABLES `forum_board` WRITE;
/*!40000 ALTER TABLE `forum_board` DISABLE KEYS */;
INSERT INTO `forum_board` VALUES (1,'校园频道','学习交流、课程讨论',NULL,1,1,'2026-07-07 21:31:38'),(2,'校园集市','二手、互助、生活',NULL,2,1,'2026-07-07 21:31:38'),(3,'求职经验','面试、简历、职场',NULL,3,1,'2026-07-07 21:31:38'),(4,'政策解读','学分银行政策讨论',NULL,4,1,'2026-07-07 21:31:38');
/*!40000 ALTER TABLE `forum_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_like`
--

DROP TABLE IF EXISTS `forum_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` tinyint NOT NULL COMMENT '1帖子 2回复',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`user_id`,`target_type`,`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_like`
--

LOCK TABLES `forum_like` WRITE;
/*!40000 ALTER TABLE `forum_like` DISABLE KEYS */;
INSERT INTO `forum_like` VALUES (2,2,2,1,'2026-07-13 09:42:55'),(4,2,2,2,'2026-07-13 09:48:59'),(5,2,2,4,'2026-07-15 13:38:31'),(6,2,1,82,'2026-07-17 13:57:39'),(7,2,2,5,'2026-07-17 13:57:46'),(8,2,2,6,'2026-07-17 21:02:25');
/*!40000 ALTER TABLE `forum_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post`
--

DROP TABLE IF EXISTS `forum_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `board_id` bigint NOT NULL COMMENT '板块ID',
  `user_id` bigint NOT NULL COMMENT '发帖人',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `view_count` int DEFAULT '0',
  `reply_count` int DEFAULT '0',
  `like_count` int DEFAULT '0',
  `is_top` tinyint DEFAULT '0' COMMENT '是否置顶',
  `status` tinyint DEFAULT '1' COMMENT '0隐藏 1正常 2审核中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_post_board` (`board_id`),
  KEY `idx_post_user` (`user_id`),
  KEY `idx_post_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛帖子';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post`
--

LOCK TABLES `forum_post` WRITE;
/*!40000 ALTER TABLE `forum_post` DISABLE KEYS */;
INSERT INTO `forum_post` VALUES (82,1,2,'[测试] Java 学习路线求助','请问学分银行平台上有没有推荐的 Java 入门课程？想先从基础语法学起。',21,8,1,0,1,'2026-07-13 09:39:11','2026-07-13 09:39:11',0),(83,3,2,'[测试] 春招 Java 后端面经','分享一下 Java 后端岗位面试题，包括 JVM、Spring、MySQL 等。',3,0,0,0,1,'2026-07-13 09:39:11','2026-07-13 09:39:11',0),(84,2,2,'[测试] 转让二手显示器','毕业出显示器，有意私信联系我。',4,0,0,0,1,'2026-07-13 09:39:11','2026-07-13 09:39:11',0);
/*!40000 ALTER TABLE `forum_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_reply`
--

DROP TABLE IF EXISTS `forum_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '回复人',
  `parent_id` bigint DEFAULT '0' COMMENT '父回复ID(楼中楼)',
  `content` text NOT NULL,
  `like_count` int DEFAULT '0',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_reply_post` (`post_id`),
  KEY `idx_reply_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛回复';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_reply`
--

LOCK TABLES `forum_reply` WRITE;
/*!40000 ALTER TABLE `forum_reply` DISABLE KEYS */;
INSERT INTO `forum_reply` VALUES (1,82,2,0,'111',1,1,'2026-07-13 09:42:34',1),(2,82,2,0,'????????',1,0,'2026-07-13 09:45:05',0),(3,82,2,2,'?????',0,0,'2026-07-13 09:45:05',0),(4,82,2,0,'111',1,0,'2026-07-15 13:38:27',0),(5,82,2,0,'111',1,1,'2026-07-17 13:57:44',0),(6,82,2,5,'222',1,1,'2026-07-17 13:57:49',0),(7,82,2,0,'111',0,1,'2026-07-17 21:02:35',0),(8,82,2,7,'111',0,1,'2026-07-17 21:02:50',0);
/*!40000 ALTER TABLE `forum_reply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_report`
--

DROP TABLE IF EXISTS `forum_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `user_id` bigint NOT NULL COMMENT '举报人',
  `target_type` tinyint NOT NULL COMMENT '1帖子 2回复',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `reason` varchar(255) NOT NULL COMMENT '举报原因',
  `status` tinyint DEFAULT '0' COMMENT '0待处理 1已处理 2驳回',
  `handle_remark` varchar(255) DEFAULT NULL COMMENT '处理说明',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论坛举报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_report`
--

LOCK TABLES `forum_report` WRITE;
/*!40000 ALTER TABLE `forum_report` DISABLE KEYS */;
INSERT INTO `forum_report` VALUES (1,3,2,2,'无意义',1,'test','2026-07-14 11:44:27'),(2,2,2,3,'test',1,'复验：举报成立应扣诚信分','2026-07-14 12:56:32'),(3,2,2,4,'20',1,'111','2026-07-15 13:38:38');
/*!40000 ALTER TABLE `forum_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `integrity_record`
--

DROP TABLE IF EXISTS `integrity_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integrity_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `change_value` int NOT NULL COMMENT '变动分值(正负)',
  `score_after` int NOT NULL COMMENT '变动后分数',
  `event_type` tinyint NOT NULL COMMENT '1加分 2扣分',
  `reason` varchar(255) NOT NULL COMMENT '原因',
  `ref_type` varchar(50) DEFAULT NULL COMMENT '关联业务',
  `ref_id` bigint DEFAULT NULL COMMENT '关联ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人(管理员/system)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_integrity_user` (`user_id`),
  KEY `idx_integrity_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='诚信评定记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `integrity_record`
--

LOCK TABLES `integrity_record` WRITE;
/*!40000 ALTER TABLE `integrity_record` DISABLE KEYS */;
INSERT INTO `integrity_record` VALUES (4,2,15,100,1,'????????????','manual_reset',NULL,1,'2026-07-15 19:57:58');
/*!40000 ALTER TABLE `integrity_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `integrity_score`
--

DROP TABLE IF EXISTS `integrity_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `integrity_score` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `score` int NOT NULL DEFAULT '100' COMMENT '当前诚信分(0-100)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户当前诚信分';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `integrity_score`
--

LOCK TABLES `integrity_score` WRITE;
/*!40000 ALTER TABLE `integrity_score` DISABLE KEYS */;
INSERT INTO `integrity_score` VALUES (2,100,'2026-07-17 21:14:57');
/*!40000 ALTER TABLE `integrity_score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview_invitation`
--

DROP TABLE IF EXISTS `interview_invitation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_invitation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
  `job_id` bigint NOT NULL COMMENT '招聘ID',
  `org_id` bigint NOT NULL COMMENT '企业ID',
  `from_user_id` bigint NOT NULL COMMENT '企业发送人',
  `to_user_id` bigint NOT NULL COMMENT '受邀学员',
  `message_id` bigint DEFAULT NULL COMMENT '关联私信ID',
  `application_id` bigint DEFAULT NULL COMMENT '关联投递ID',
  `status` tinyint DEFAULT '0' COMMENT '0待回复 1已接受 2已拒绝',
  `invite_time` datetime DEFAULT NULL COMMENT '面试时间',
  `location` varchar(255) DEFAULT NULL COMMENT '面试地点/方式',
  `interview_mode` tinyint NOT NULL DEFAULT '0' COMMENT '0??? 1???',
  `room_id` varchar(64) DEFAULT NULL COMMENT 'TRTC ?????????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_invite_to` (`to_user_id`),
  KEY `idx_invite_job` (`job_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='面试邀请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_invitation`
--

LOCK TABLES `interview_invitation` WRITE;
/*!40000 ALTER TABLE `interview_invitation` DISABLE KEYS */;
INSERT INTO `interview_invitation` VALUES (8,127,2,3,2,20,12,1,'2026-07-20 10:00:00','平台视频面试',1,'interview-8','2026-07-17 21:06:31');
/*!40000 ALTER TABLE `interview_invitation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_application`
--

DROP TABLE IF EXISTS `job_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_application` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投递ID',
  `job_id` bigint NOT NULL COMMENT '招聘ID',
  `user_id` bigint NOT NULL COMMENT '求职者ID',
  `resume_id` bigint DEFAULT NULL COMMENT '使用的简历ID',
  `cover_message` text COMMENT '求职信',
  `status` tinyint DEFAULT '0' COMMENT '0已投递 1已查看 2面试中 3录用 4拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_user` (`job_id`,`user_id`),
  KEY `idx_apply_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简历投递';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_application`
--

LOCK TABLES `job_application` WRITE;
/*!40000 ALTER TABLE `job_application` DISABLE KEYS */;
INSERT INTO `job_application` VALUES (12,127,2,1,NULL,2,'2026-07-17 21:03:07','2026-07-17 21:03:07');
/*!40000 ALTER TABLE `job_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_match_record`
--

DROP TABLE IF EXISTS `job_match_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_match_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '学员ID',
  `job_id` bigint NOT NULL COMMENT '职位ID',
  `match_score` decimal(5,2) NOT NULL COMMENT '契合度0-100',
  `match_detail` json DEFAULT NULL COMMENT '匹配详情',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_match_user` (`user_id`),
  KEY `idx_match_job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='职位契合度推荐记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_match_record`
--

LOCK TABLES `job_match_record` WRITE;
/*!40000 ALTER TABLE `job_match_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `job_match_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_posting`
--

DROP TABLE IF EXISTS `job_posting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_posting` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '招聘ID',
  `org_id` bigint NOT NULL COMMENT '发布企业ID',
  `publisher_id` bigint NOT NULL COMMENT '发布人',
  `title` varchar(200) NOT NULL COMMENT '职位名称',
  `description` text COMMENT '职位描述',
  `requirements` text COMMENT '任职要求',
  `salary_range` varchar(50) DEFAULT NULL COMMENT '薪资范围',
  `location` varchar(100) DEFAULT NULL COMMENT '工作地点',
  `edu_requirement` varchar(50) DEFAULT NULL COMMENT '学历要求',
  `status` tinyint DEFAULT '1' COMMENT '0下架 1招聘中',
  `view_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_job_org` (`org_id`),
  KEY `idx_job_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招聘信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_posting`
--

LOCK TABLES `job_posting` WRITE;
/*!40000 ALTER TABLE `job_posting` DISABLE KEYS */;
INSERT INTO `job_posting` VALUES (65,2,3,'111','','','','','',1,0,'2026-07-13 13:33:14','2026-07-14 08:52:44',1),(84,2,3,'111','','','','','',1,0,'2026-07-15 13:43:00','2026-07-15 13:43:00',0),(127,2,3,'[测试] Java 后端开发工程师','负责学分银行平台后端模块开发，技术栈以 Java / Spring Boot 为主。','熟悉 Java、Spring Boot、MySQL；有微服务经验优先。','12K-20K','深圳','本科',1,0,'2026-07-17 19:39:14','2026-07-17 19:39:14',0),(128,2,3,'[测试] 前端开发工程师','负责 Vue3 前端页面开发，与 Java 团队协作。','熟悉 Vue3、TypeScript、Element Plus。','10K-18K','深圳','本科',1,0,'2026-07-17 19:39:14','2026-07-17 19:39:14',0);
/*!40000 ALTER TABLE `job_posting` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `job_tag`
--

DROP TABLE IF EXISTS `job_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `job_tag` (
  `job_id` bigint NOT NULL COMMENT '招聘ID',
  `tag_id` bigint NOT NULL COMMENT '技能标签ID',
  PRIMARY KEY (`job_id`,`tag_id`),
  KEY `idx_jt_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='招聘技能标签';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `job_tag`
--

LOCK TABLES `job_tag` WRITE;
/*!40000 ALTER TABLE `job_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `job_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_achievement`
--

DROP TABLE IF EXISTS `learning_achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成果ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '成果名称',
  `type` tinyint DEFAULT NULL COMMENT '1证书 2课程 3项目 4其他',
  `org_id` bigint DEFAULT NULL COMMENT '认证机构',
  `certificate_id` bigint DEFAULT NULL COMMENT '关联证书',
  `credit_value` decimal(10,2) DEFAULT NULL COMMENT '可兑换学分',
  `file_url` varchar(255) DEFAULT NULL COMMENT '附件',
  `tags` varchar(500) DEFAULT NULL COMMENT 'æ ‡ç­¾',
  `verify_status` tinyint DEFAULT '0' COMMENT '0待校验 1已通过 2未通过',
  `blockchain_hash` varchar(128) DEFAULT NULL COMMENT '存证哈希',
  `convert_status` tinyint NOT NULL DEFAULT '0' COMMENT 'è½¬æ¢çŠ¶æ€',
  `source_type` varchar(50) DEFAULT NULL COMMENT 'æ¥æºç±»åž‹',
  `source_id` bigint DEFAULT NULL COMMENT 'æ¥æºä¸šåŠ¡ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_achievement_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习成果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_achievement`
--

LOCK TABLES `learning_achievement` WRITE;
/*!40000 ALTER TABLE `learning_achievement` DISABLE KEYS */;
INSERT INTO `learning_achievement` VALUES (7,2,'[画像演示] Java 基础结业证书',1,2,NULL,10.00,NULL,NULL,1,NULL,0,NULL,NULL,'2026-07-10 22:13:07','2026-07-15 19:57:58',0),(8,2,'[画像演示] 个人博客项目（Spring Boot）',3,NULL,NULL,8.00,NULL,NULL,0,NULL,0,NULL,NULL,'2026-07-10 22:13:07','2026-07-10 22:13:07',0),(9,2,'区块链技术基础 课程合格证',1,2,1,8.00,'/certificates/LC202607111536230002FC5AA1.pdf',NULL,1,'2869a017c69461d2d530694d14a9e02e9af84dd73e2dc9a7b32ac65fdeae83fc',0,NULL,NULL,'2026-07-11 15:36:23','2026-07-11 15:36:23',0),(10,2,'数据结构与算法 C++ 实现 课程合格证',1,2,2,12.00,'/certificates/LC202607111651280002EB97B6.pdf',NULL,1,'4559d0e0522278cca48dcafab762279de6503157502a334fbc07ce6ea713d790',0,NULL,NULL,'2026-07-11 16:51:28','2026-07-11 16:51:28',0),(11,2,'【互认测试】企业实训营结业学分',5,2,NULL,15.00,NULL,NULL,1,'0xtest_recog_camp_001',0,'manual',NULL,'2026-07-15 19:57:58','2026-07-15 19:58:17',0),(12,2,'【互认测试】待校验成果（不可转换）',3,2,NULL,6.00,NULL,NULL,0,NULL,0,'manual',NULL,'2026-07-15 19:57:58','2026-07-15 19:58:17',0),(13,2,'【互认测试】跨机构课程互认学分',2,3,NULL,20.00,NULL,NULL,1,'0xtest_recog_course_002',1,'manual',NULL,'2026-07-15 19:58:17','2026-07-15 19:58:17',0),(14,2,'Java ???????',1,2,NULL,30.00,NULL,NULL,1,NULL,1,'certificate_claim',1,'2026-07-15 20:37:47','2026-07-15 20:37:47',0),(15,2,'Java 应用能力合格证',1,2,NULL,20.00,NULL,NULL,1,NULL,1,'certificate_claim',1,'2026-07-15 20:49:42','2026-07-15 20:49:42',0),(16,2,'计算机视觉项目实践 课程合格证',1,2,3,12.00,NULL,NULL,1,'036f85ab0a83ea6e58f6cb8083bafe5cce6a30e8650f4f1a38a37162a605ed8e',0,NULL,NULL,'2026-07-15 20:56:17','2026-07-15 20:56:17',0);
/*!40000 ALTER TABLE `learning_achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_archive`
--

DROP TABLE IF EXISTS `learning_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_archive` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '档案标题',
  `archive_type` tinyint DEFAULT '1' COMMENT '1课程 2活动 3成果 4其他',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程',
  `certificate_id` bigint DEFAULT NULL COMMENT '关联证书',
  `category` varchar(50) DEFAULT NULL COMMENT '类别',
  `description` text COMMENT '描述',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `credit_earned` decimal(10,2) DEFAULT '0.00' COMMENT '获得学分',
  `status` tinyint DEFAULT '1' COMMENT '0进行中 1已完成',
  `transferred_to_org_id` bigint DEFAULT NULL,
  `transferred_to_course_id` bigint DEFAULT NULL,
  `transferred_to_certificate_id` bigint DEFAULT NULL,
  `transfer_application_id` bigint DEFAULT NULL,
  `transfer_status` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_archive_user` (`user_id`),
  KEY `idx_archive_type` (`archive_type`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='终身学习档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_archive`
--

LOCK TABLES `learning_archive` WRITE;
/*!40000 ALTER TABLE `learning_archive` DISABLE KEYS */;
INSERT INTO `learning_archive` VALUES (12,2,'[画像演示] 参加 Java 技术沙龙',2,NULL,NULL,'活动实践','参与企业工程师分享，了解 Spring Boot 项目经验与学分认定流程。','2026-06-25','2026-06-25',5.00,1,NULL,NULL,NULL,NULL,0,'2026-07-10 22:13:07','2026-07-10 22:13:07',0),(13,2,'区块链技术基础',1,96,1,'课程学习','完成学习资源并通过区块链证书校验: LC202607111536230002FC5AA1','2026-07-11','2026-07-11',8.00,1,NULL,NULL,NULL,NULL,0,'2026-07-11 15:36:23','2026-07-11 15:36:23',0),(14,2,'数据结构与算法 C++ 实现',1,100,2,'课程学习','完成学习资源并通过区块链证书校验: LC202607111651280002EB97B6','2026-07-11','2026-07-11',12.00,1,NULL,NULL,NULL,NULL,0,'2026-07-11 16:51:28','2026-07-11 16:51:28',0),(15,2,'??????????',2,NULL,NULL,'????','????????????????????????????','2026-07-15','2026-07-15',15.00,1,NULL,NULL,NULL,NULL,0,'2026-07-15 19:57:58','2026-07-15 19:57:58',0),(16,2,'计算机视觉项目实践',1,107,3,'课程学习','完成学习资源并通过区块链证书校验: LC202607152056170002EAC8C3','2026-07-15','2026-07-15',12.00,1,NULL,NULL,NULL,NULL,0,'2026-07-15 20:56:17','2026-07-15 20:56:17',0),(17,2,'[演示] 高等数学与学分互认基础',1,214,NULL,'学分转换演示','演示：已完成 [演示] 高等数学与学分互认基础','2026-07-16','2026-07-16',4.00,1,NULL,NULL,NULL,NULL,0,'2026-07-16 10:34:47','2026-07-17 21:14:57',0),(18,2,'[演示] 数据结构跨校选修',1,215,NULL,'学分转换演示','演示：已完成 [演示] 数据结构跨校选修','2026-07-16','2026-07-16',3.50,1,NULL,NULL,NULL,NULL,0,'2026-07-16 10:34:47','2026-07-16 11:27:29',0),(19,2,'[演示] Java 企业级开发实训',1,216,NULL,'学分转换演示','演示：已完成 [演示] Java 企业级开发实训','2026-07-16','2026-07-16',4.00,1,NULL,NULL,NULL,NULL,0,'2026-07-16 10:34:47','2026-07-16 11:27:29',0),(20,2,'[演示] Python 数据分析工作坊',1,217,NULL,'学分转换演示','演示：已完成 [演示] Python 数据分析工作坊','2026-07-16','2026-07-16',2.50,1,NULL,NULL,NULL,NULL,0,'2026-07-16 10:34:47','2026-07-16 11:27:29',0);
/*!40000 ALTER TABLE `learning_archive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_certificate`
--

DROP TABLE IF EXISTS `learning_certificate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '证书ID',
  `cert_no` varchar(64) NOT NULL COMMENT '证书唯一编号',
  `user_id` bigint NOT NULL COMMENT '学员ID',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `title` varchar(200) NOT NULL COMMENT '证书名称',
  `qr_content` varchar(500) NOT NULL COMMENT '二维码内容(校验URL或JSON)',
  `qr_image_url` varchar(255) DEFAULT NULL COMMENT '二维码图片',
  `file_url` varchar(255) DEFAULT NULL COMMENT '证书PDF下载地址',
  `blockchain_hash` varchar(128) DEFAULT NULL COMMENT '区块链存证哈希',
  `verify_status` tinyint DEFAULT '0' COMMENT '0待校验 1已通过 2未通过',
  `issued_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cert_no` (`cert_no`),
  KEY `idx_cert_user` (`user_id`),
  KEY `idx_cert_course` (`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学习证书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_certificate`
--

LOCK TABLES `learning_certificate` WRITE;
/*!40000 ALTER TABLE `learning_certificate` DISABLE KEYS */;
INSERT INTO `learning_certificate` VALUES (1,'LC202607111536230002FC5AA1',2,96,'区块链技术基础 课程合格证','/api/learning/certificates/verify?certNo=LC202607111536230002FC5AA1&hash=2869a017c69461d2d530694d14a9e02e9af84dd73e2dc9a7b32ac65fdeae83fc',NULL,'/certificates/LC202607111536230002FC5AA1.pdf','2869a017c69461d2d530694d14a9e02e9af84dd73e2dc9a7b32ac65fdeae83fc',1,'2026-07-11 15:36:24'),(2,'LC202607111651280002EB97B6',2,100,'数据结构与算法 C++ 实现 课程合格证','/api/learning/certificates/verify?certNo=LC202607111651280002EB97B6&hash=4559d0e0522278cca48dcafab762279de6503157502a334fbc07ce6ea713d790',NULL,'/certificates/LC202607111651280002EB97B6.pdf','4559d0e0522278cca48dcafab762279de6503157502a334fbc07ce6ea713d790',1,'2026-07-11 16:51:29'),(3,'LC202607152056170002EAC8C3',2,107,'计算机视觉项目实践 课程合格证','/api/learning/certificates/verify?certNo=LC202607152056170002EAC8C3&hash=036f85ab0a83ea6e58f6cb8083bafe5cce6a30e8650f4f1a38a37162a605ed8e',NULL,NULL,'036f85ab0a83ea6e58f6cb8083bafe5cce6a30e8650f4f1a38a37162a605ed8e',1,'2026-07-15 20:56:18');
/*!40000 ALTER TABLE `learning_certificate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_stat_daily`
--

DROP TABLE IF EXISTS `learning_stat_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_stat_daily` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `study_minutes` int DEFAULT '0' COMMENT '学习时长(分钟)',
  `courses_completed` int DEFAULT '0' COMMENT '完成课程数',
  `credit_earned` decimal(10,2) DEFAULT '0.00' COMMENT '当日获得学分',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`stat_date`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日学习统计(图表)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_stat_daily`
--

LOCK TABLES `learning_stat_daily` WRITE;
/*!40000 ALTER TABLE `learning_stat_daily` DISABLE KEYS */;
INSERT INTO `learning_stat_daily` VALUES (1,2,'2026-07-15',0,1,0.00);
/*!40000 ALTER TABLE `learning_stat_daily` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mall_category`
--

DROP TABLE IF EXISTS `mall_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mall_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_mall_cat_parent` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城分类';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mall_category`
--

LOCK TABLES `mall_category` WRITE;
/*!40000 ALTER TABLE `mall_category` DISABLE KEYS */;
INSERT INTO `mall_category` VALUES (1,'课程资源',0,1,1,'2026-07-07 21:31:38'),(2,'学习用品',0,2,1,'2026-07-07 21:31:38'),(3,'虚拟商品',0,3,1,'2026-07-07 21:31:38'),(4,'兑换专区',0,4,1,'2026-07-07 21:31:38'),(5,'虚拟权益',0,50,1,'2026-07-11 15:27:32'),(6,'技术服务',0,60,1,'2026-07-11 15:27:32');
/*!40000 ALTER TABLE `mall_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mall_order`
--

DROP TABLE IF EXISTS `mall_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mall_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '买家ID',
  `total_credit` decimal(12,2) DEFAULT '0.00' COMMENT '学分总额',
  `total_money` decimal(12,2) DEFAULT '0.00' COMMENT '现金总额',
  `pay_method` tinyint DEFAULT '1' COMMENT '1学分 2模拟支付 3混合',
  `pay_status` tinyint DEFAULT '0' COMMENT '0待支付 1已支付 2已取消 3已退款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_status` (`pay_status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城订单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mall_order`
--

LOCK TABLES `mall_order` WRITE;
/*!40000 ALTER TABLE `mall_order` DISABLE KEYS */;
INSERT INTO `mall_order` VALUES (1,'MO2026071115335621B7DA8B',2,10.00,0.00,1,1,'2026-07-11 15:34:00','','2026-07-11 15:33:56','2026-07-11 15:33:56'),(2,'MO202607112003525E2E7BCE',1,90.00,0.00,1,0,NULL,'商品详情页立即兑换：护眼阅读台灯','2026-07-11 20:03:52','2026-07-11 20:03:52'),(3,'MO20260711200354BF4C12A2',1,90.00,0.00,1,0,NULL,'商品详情页立即兑换：护眼阅读台灯','2026-07-11 20:03:54','2026-07-11 20:03:54'),(4,'MO202607131004087932EA15',2,6.00,0.00,1,1,'2026-07-13 10:04:09','商品详情页立即兑换：Git 常用命令速查表','2026-07-13 10:04:08','2026-07-13 10:04:08'),(5,'MO20260714083239EEC993AC',2,45.00,0.00,1,1,'2026-07-14 08:32:40','商品详情页立即兑换：静音无线鼠标','2026-07-14 08:32:39','2026-07-14 08:32:39'),(6,'MO20260715133611831B5072',2,46.00,0.00,1,0,NULL,'','2026-07-15 13:36:11','2026-07-15 13:36:11');
/*!40000 ALTER TABLE `mall_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mall_order_item`
--

DROP TABLE IF EXISTS `mall_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mall_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品快照名',
  `quantity` int NOT NULL DEFAULT '1',
  `price_credit` decimal(10,2) DEFAULT '0.00',
  `price_money` decimal(10,2) DEFAULT '0.00',
  `redemption_code` varchar(64) DEFAULT NULL COMMENT '???????????????',
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mall_order_item`
--

LOCK TABLES `mall_order_item` WRITE;
/*!40000 ALTER TABLE `mall_order_item` DISABLE KEYS */;
INSERT INTO `mall_order_item` VALUES (1,1,65,'Linux 运维命令手册',1,10.00,0.00,'CB-00D6E84FCDBF'),(2,2,90,'护眼阅读台灯',1,90.00,0.00,NULL),(3,3,90,'护眼阅读台灯',1,90.00,0.00,NULL),(4,4,61,'Git 常用命令速查表',1,6.00,0.00,'CB-94FFE9C534DE'),(5,5,106,'静音无线鼠标',1,45.00,0.00,NULL),(6,6,63,'Java 核心技术电子书兑换券',1,18.00,0.00,NULL),(7,6,66,'MySQL 性能优化案例集',1,22.00,0.00,NULL),(8,6,61,'Git 常用命令速查表',1,6.00,0.00,NULL);
/*!40000 ALTER TABLE `mall_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mall_product`
--

DROP TABLE IF EXISTS `mall_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mall_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `org_id` bigint DEFAULT NULL COMMENT '????ID',
  `publisher_id` bigint DEFAULT NULL COMMENT '???ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '描述',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面',
  `product_type` tinyint DEFAULT '1' COMMENT '1实物 2虚拟 3课程 4服务',
  `ref_course_id` bigint DEFAULT NULL COMMENT '关联课程(可选)',
  `price_credit` decimal(10,2) DEFAULT '0.00' COMMENT '学分价格',
  `price_money` decimal(10,2) DEFAULT '0.00' COMMENT '现金价格',
  `stock` int DEFAULT '9999' COMMENT '库存',
  `status` tinyint DEFAULT '1' COMMENT '0下架 1上架',
  `approval_status` tinyint NOT NULL DEFAULT '1' COMMENT '0?? 1?? 2??',
  `review_remark` varchar(255) DEFAULT NULL COMMENT '????',
  `reviewed_by` bigint DEFAULT NULL COMMENT '???',
  `reviewed_at` datetime DEFAULT NULL COMMENT '????',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_product_cat` (`category_id`),
  KEY `idx_product_type` (`product_type`)
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商城商品';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mall_product`
--

LOCK TABLES `mall_product` WRITE;
/*!40000 ALTER TABLE `mall_product` DISABLE KEYS */;
INSERT INTO `mall_product` VALUES (58,2,NULL,NULL,'A5 网格学习笔记本','适合记录代码思路、算法草稿和课程重点的网格笔记本。','https://picsum.photos/seed/product-notebook/640/480',1,NULL,12.00,0.00,200,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(59,5,NULL,NULL,'AI 学习助手使用额度','兑换五十次课程答疑、知识总结和学习计划生成额度。','https://picsum.photos/seed/product-ai-assistant/640/480',2,NULL,25.00,0.00,500,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(60,1,NULL,NULL,'C++ 算法刷题课程月卡','兑换一个月 C++ 数据结构与算法刷题课程权限。','https://picsum.photos/seed/product-cpp-course/640/480',3,100,35.00,0.00,90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(61,3,NULL,NULL,'Git 常用命令速查表','覆盖分支、合并、回退、标签和远程协作的电子速查表。','https://picsum.photos/seed/product-git-cheatsheet/640/480',2,NULL,6.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-13 10:54:11',0),(62,6,NULL,NULL,'GitHub 项目代码评审','导师对一个学习项目的目录、代码质量和文档进行评审。','https://picsum.photos/seed/product-code-review/640/480',4,NULL,60.00,0.00,35,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(63,1,NULL,NULL,'Java 核心技术电子书兑换券','兑换一本 Java 核心技术方向正版电子书的电子券。','https://picsum.photos/seed/product-java-book/640/480',2,NULL,18.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(64,3,NULL,NULL,'JetBrains IDE 快捷键手册','IntelliJ IDEA、WebStorm 和 PyCharm 常用快捷键电子手册。','https://picsum.photos/seed/product-ide-guide/640/480',2,NULL,8.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(65,3,NULL,NULL,'Linux 运维命令手册','包含文件、网络、进程、日志和服务管理的常用命令。','https://picsum.photos/seed/product-linux-guide/640/480',2,NULL,10.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 16:11:59',0),(66,1,NULL,NULL,'MySQL 性能优化案例集','包含执行计划、索引设计和慢查询优化案例的电子资料。','https://picsum.photos/seed/product-mysql-cases/640/480',2,NULL,22.00,0.00,120,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(67,1,NULL,NULL,'Python 数据分析训练营名额','包含数据清洗、可视化和结业项目的线上训练营名额。','https://picsum.photos/seed/product-python-camp/640/480',3,74,55.00,0.00,60,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(68,1,NULL,NULL,'Spring Boot 项目实战课兑换码','兑换 Spring Boot 企业项目实战在线课程学习资格。','https://picsum.photos/seed/product-spring-course/640/480',3,85,45.00,0.00,80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(69,2,NULL,NULL,'Type-C 多功能转接器','提供 HDMI、USB 和读卡接口，方便课程演示与实验。','https://picsum.photos/seed/product-usb-hub/640/480',1,NULL,75.00,0.00,50,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(70,1,NULL,NULL,'Vue 3 前端实战课程包','包含 Vue 3、Pinia、路由和组件设计的课程包。','https://picsum.photos/seed/product-vue-course/640/480',3,92,40.00,0.00,70,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(71,5,NULL,NULL,'云端开发环境 20 小时','兑换浏览器云端开发环境二十小时使用额度。','https://picsum.photos/seed/product-cloud-ide/640/480',2,NULL,35.00,0.00,300,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(72,1,NULL,NULL,'人工智能入门视频课兑换码','兑换机器学习与生成式人工智能入门视频课程。','https://picsum.photos/seed/product-ai-course/640/480',3,102,48.00,0.00,75,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(73,2,NULL,NULL,'便携式笔记本电脑支架','可折叠铝合金支架，改善长时间在线学习姿势。','https://picsum.photos/seed/product-laptop-stand/640/480',1,NULL,60.00,0.00,60,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(74,4,NULL,NULL,'共享自习室两小时券','兑换合作自习室两小时独立座位使用时间。','https://picsum.photos/seed/product-study-room/640/480',2,NULL,25.00,0.00,120,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(75,3,NULL,NULL,'前端 UI 组件设计素材包','包含后台管理、表单、图表和移动端界面设计素材。','https://picsum.photos/seed/product-ui-kit/640/480',2,NULL,18.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(76,6,NULL,NULL,'前端页面体验评审','从交互、响应式、可访问性和性能角度评审一个前端页面。','https://picsum.photos/seed/product-frontend-review/640/480',4,NULL,55.00,0.00,30,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(77,1,NULL,NULL,'区块链技术学习资料包','包含区块链基础、智能合约和联盟链实践资料。','https://picsum.photos/seed/product-blockchain-pack/640/480',2,NULL,28.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(78,2,NULL,NULL,'双头荧光标记笔套装','六色双头标记笔，用于教材重点和学习计划标注。','https://picsum.photos/seed/product-highlighter/640/480',1,NULL,10.00,0.00,180,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(79,6,NULL,NULL,'后端接口设计评审','评审 REST 接口命名、响应结构、鉴权和异常处理设计。','https://picsum.photos/seed/product-api-review/640/480',4,NULL,65.00,0.00,30,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(80,4,NULL,NULL,'咖啡饮品兑换券','可在校园合作咖啡店兑换指定中杯饮品一杯。','https://picsum.photos/seed/product-coffee/640/480',2,NULL,20.00,0.00,150,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(81,4,NULL,NULL,'图书馆逾期费抵扣券','用于抵扣一次不超过十元的图书逾期费用。','https://picsum.photos/seed/product-library-coupon/640/480',2,NULL,18.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(82,5,NULL,NULL,'在线简历制作工具月卡','使用专业模板制作、导出和维护技术简历。','https://picsum.photos/seed/product-resume-tool/640/480',2,NULL,20.00,0.00,400,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(83,5,NULL,NULL,'在线绘图工具专业版月卡','用于流程图、架构图和思维导图制作的月度权益。','https://picsum.photos/seed/product-diagram-tool/640/480',2,NULL,22.00,0.00,300,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(84,5,NULL,NULL,'在线题库会员月卡','一个月在线编程题库会员权益，支持题解和学习统计。','https://picsum.photos/seed/product-question-bank/640/480',2,NULL,30.00,0.00,500,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(85,4,NULL,NULL,'学习主题徽章套装','包含编程、阅读和终身学习主题的三枚金属徽章。','https://picsum.photos/seed/product-badges/640/480',1,NULL,28.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(86,6,NULL,NULL,'学习路径规划咨询','根据基础和目标岗位制定三个月学习路径与阶段目标。','https://picsum.photos/seed/product-learning-plan/640/480',4,NULL,50.00,0.00,45,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(87,5,NULL,NULL,'开发者社区高级会员月卡','获得技术专栏、直播回放和问答优先回复权益。','https://picsum.photos/seed/product-community-member/640/480',2,NULL,32.00,0.00,250,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(88,6,NULL,NULL,'技术简历一对一诊断','由企业导师进行一次三十分钟技术简历结构与内容诊断。','https://picsum.photos/seed/product-resume-review/640/480',4,NULL,45.00,0.00,40,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(89,3,NULL,NULL,'技术面试题库电子版','覆盖 Java、数据库、前端和计算机基础的面试练习题。','https://picsum.photos/seed/product-interview-bank/640/480',2,NULL,20.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(90,2,NULL,NULL,'护眼阅读台灯','三档色温与亮度调节，适合夜间阅读和编程学习。','https://picsum.photos/seed/product-lamp/640/480',1,NULL,90.00,0.00,40,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(91,3,NULL,NULL,'数据库设计文档模板','提供数据字典、ER 图说明和表结构评审模板。','https://picsum.photos/seed/product-database-template/640/480',2,NULL,10.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(92,6,NULL,NULL,'数据库设计评审','对一个项目的数据模型、字段、索引和查询方案进行评审。','https://picsum.photos/seed/product-database-review/640/480',4,NULL,65.00,0.00,25,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(93,2,NULL,NULL,'数据线收纳包','分区收纳充电器、移动硬盘、数据线和转接头。','https://picsum.photos/seed/product-cable-bag/640/480',1,NULL,20.00,0.00,130,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(94,4,NULL,NULL,'校园书店 20 元代金券','可在校园合作书店购买教材和学习用品时抵扣。','https://picsum.photos/seed/product-bookstore/640/480',2,NULL,35.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(95,4,NULL,NULL,'校园打印额度 50 页','兑换校内合作打印点黑白打印额度五十页。','https://picsum.photos/seed/product-printing/640/480',2,NULL,15.00,0.00,200,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(96,4,NULL,NULL,'校园文创帆布袋','印有校园学习主题图案的环保帆布袋。','https://picsum.photos/seed/product-canvas-bag/640/480',1,NULL,40.00,0.00,80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(97,2,NULL,NULL,'桌面番茄钟计时器','用于专注学习和休息管理的实体倒计时器。','https://picsum.photos/seed/product-timer/640/480',1,NULL,30.00,0.00,100,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(98,6,NULL,NULL,'模拟技术面试一次','完成一次四十五分钟岗位模拟面试并获得书面反馈。','https://picsum.photos/seed/product-mock-interview/640/480',4,NULL,70.00,0.00,30,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(99,5,NULL,NULL,'电子图书馆会员月卡','一个月技术电子书和期刊阅读权限。','https://picsum.photos/seed/product-library-member/640/480',2,NULL,28.00,0.00,400,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(100,2,NULL,NULL,'程序员主题鼠标垫','大尺寸防滑鼠标垫，适合宿舍和办公室学习桌面。','https://picsum.photos/seed/product-mousepad/640/480',1,NULL,25.00,0.00,120,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(101,3,NULL,NULL,'程序员简历模板包','提供后端、前端、测试和数据岗位的可编辑简历模板。','https://picsum.photos/seed/product-resume-template/640/480',2,NULL,12.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(102,6,NULL,NULL,'职业方向咨询','结合技能、项目和求职目标进行一次职业方向咨询。','https://picsum.photos/seed/product-career-consulting/640/480',4,NULL,55.00,0.00,40,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(103,5,NULL,NULL,'英语技术阅读训练月卡','提供一个月技术英语词汇和文档阅读训练内容。','https://picsum.photos/seed/product-tech-english/640/480',2,NULL,24.00,0.00,300,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(104,1,NULL,NULL,'软件测试与质量保障课程包','覆盖单元测试、接口测试和持续集成的在线课程包。','https://picsum.photos/seed/product-test-course/640/480',3,87,38.00,0.00,80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(105,4,NULL,NULL,'运动场馆单次体验券','兑换校内合作羽毛球馆或健身房单次体验。','https://picsum.photos/seed/product-sport-pass/640/480',2,NULL,30.00,0.00,90,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(106,2,NULL,NULL,'静音无线鼠标','适合图书馆和宿舍使用的便携静音无线鼠标。','https://picsum.photos/seed/product-mouse/640/480',1,NULL,45.00,0.00,80,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-14 13:00:44',0),(107,3,NULL,NULL,'项目需求文档模板包','包含需求说明、接口清单、测试用例和验收记录模板。','https://picsum.photos/seed/product-requirement-template/640/480',2,NULL,10.00,0.00,9999,1,1,NULL,NULL,NULL,'2026-07-11 15:27:32','2026-07-11 15:27:32',0),(160,1,2,3,'111',NULL,NULL,1,NULL,10.00,0.00,99,1,1,NULL,1,'2026-07-13 13:50:13','2026-07-13 13:45:57','2026-07-14 08:52:44',1);
/*!40000 ALTER TABLE `mall_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_credit_txn`
--

DROP TABLE IF EXISTS `org_credit_txn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_credit_txn` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `org_id` bigint NOT NULL DEFAULT '0',
  `subject_id` bigint NOT NULL,
  `txn_type` tinyint NOT NULL COMMENT '1?? 2???? 3???? 4?????? 5?????? 6????',
  `amount` decimal(12,2) NOT NULL COMMENT '????',
  `balance_after` decimal(12,2) NOT NULL,
  `peer_org_id` bigint DEFAULT NULL COMMENT '????',
  `peer_subject_id` bigint DEFAULT NULL COMMENT '????',
  `rate` decimal(8,4) DEFAULT NULL COMMENT '??/????',
  `biz_type` varchar(50) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `ref_type` varchar(50) DEFAULT NULL,
  `ref_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_oct_user` (`user_id`,`create_time`),
  KEY `idx_oct_org` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_credit_txn`
--

LOCK TABLES `org_credit_txn` WRITE;
/*!40000 ALTER TABLE `org_credit_txn` DISABLE KEYS */;
INSERT INTO `org_credit_txn` VALUES (1,2,2,1,2,-5.00,40.00,3,1,1.0000,'recognize','机构学分互认转出',NULL,NULL,'2026-07-15 20:37:47'),(2,2,3,1,3,5.00,17.00,2,1,1.0000,'recognize','机构学分互认转入',NULL,NULL,'2026-07-15 20:37:47'),(3,2,2,1,4,-2.00,38.00,2,2,0.7000,'subject_convert','科目换算转出',NULL,NULL,'2026-07-15 20:37:47'),(4,2,2,2,5,1.40,1.40,2,1,0.7000,'subject_convert','科目换算转入',NULL,NULL,'2026-07-15 20:37:47'),(5,2,2,1,6,-30.00,8.00,NULL,NULL,NULL,'claim_certificate','领取：Java ???????','certificate_rule',1,'2026-07-15 20:37:47'),(6,2,2,1,4,-5.00,3.00,2,2,0.7000,'subject_convert','科目换算转出',NULL,NULL,'2026-07-15 20:42:22'),(7,2,2,2,5,3.50,4.90,2,1,0.7000,'subject_convert','科目换算转入',NULL,NULL,'2026-07-15 20:42:22'),(8,2,2,1,4,-3.00,0.00,2,2,0.7000,'subject_convert','科目换算转出',NULL,NULL,'2026-07-15 20:42:54'),(9,2,2,2,5,2.10,7.00,2,1,0.7000,'subject_convert','科目换算转入',NULL,NULL,'2026-07-15 20:42:54'),(10,2,2,1,6,-20.00,20.00,NULL,NULL,NULL,'claim_certificate','领取：Java 应用能力合格证','certificate_rule',1,'2026-07-15 20:49:42'),(11,2,2,1,6,-4.20,15.80,NULL,NULL,NULL,'course_cert_claim','学分兑换课程证书: 计算机视觉项目实践','course',107,'2026-07-15 20:56:17');
/*!40000 ALTER TABLE `org_credit_txn` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_credit_wallet`
--

DROP TABLE IF EXISTS `org_credit_wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_credit_wallet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `org_id` bigint NOT NULL DEFAULT '0' COMMENT '0=??????',
  `subject_id` bigint NOT NULL,
  `balance` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total_earned` decimal(12,2) NOT NULL DEFAULT '0.00',
  `total_spent` decimal(12,2) NOT NULL DEFAULT '0.00',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_org_subject` (`user_id`,`org_id`,`subject_id`),
  KEY `idx_ocw_user` (`user_id`),
  KEY `idx_ocw_org` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_credit_wallet`
--

LOCK TABLES `org_credit_wallet` WRITE;
/*!40000 ALTER TABLE `org_credit_wallet` DISABLE KEYS */;
INSERT INTO `org_credit_wallet` VALUES (1,2,2,1,15.80,45.00,24.20,'2026-07-15 20:26:26','2026-07-15 20:56:17'),(3,2,3,1,25.00,25.00,0.00,'2026-07-15 20:26:26','2026-07-15 20:48:00'),(4,2,7,3,15.00,15.00,0.00,'2026-07-15 20:26:26','2026-07-15 20:48:00'),(5,2,2,2,15.00,15.00,0.00,'2026-07-15 20:37:47','2026-07-15 21:07:04'),(14,2,2,4,20.00,20.00,0.00,'2026-07-15 21:07:04','2026-07-15 21:07:04'),(16,2,2,3,15.00,15.00,0.00,'2026-07-15 21:07:04','2026-07-15 21:07:04');
/*!40000 ALTER TABLE `org_credit_wallet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_material`
--

DROP TABLE IF EXISTS `org_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资料ID',
  `org_id` bigint NOT NULL COMMENT '企业ID',
  `publisher_id` bigint NOT NULL COMMENT '发布人',
  `title` varchar(200) NOT NULL COMMENT '资料标题',
  `description` text COMMENT '描述',
  `file_url` varchar(255) DEFAULT NULL COMMENT '文件地址',
  `material_type` tinyint DEFAULT '1' COMMENT '1文档 2视频 3其他',
  `status` tinyint DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_material_org` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='企业发布的学习资料';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_material`
--

LOCK TABLES `org_material` WRITE;
/*!40000 ALTER TABLE `org_material` DISABLE KEYS */;
INSERT INTO `org_material` VALUES (125,2,3,'[测试] Java 编码规范手册','企业内部 Java 开发规范与 Code Review 清单，供学员参考学习。','/files/materials/java-coding-guide.pdf',1,1,'2026-07-17 19:39:14',0),(126,2,3,'[测试] Spring Boot 项目模板','可快速启动的 Java 微服务项目骨架与部署说明。','/files/materials/springboot-template.zip',1,1,'2026-07-17 19:39:14',0);
/*!40000 ALTER TABLE `org_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_record`
--

DROP TABLE IF EXISTS `payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `pay_no` varchar(64) NOT NULL COMMENT '支付流水号',
  `amount_credit` decimal(12,2) DEFAULT '0.00',
  `amount_money` decimal(12,2) DEFAULT '0.00',
  `pay_channel` varchar(30) DEFAULT 'mock' COMMENT 'mock/simulated',
  `pay_status` tinyint DEFAULT '1' COMMENT '1成功 0失败',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pay_no` (`pay_no`),
  KEY `idx_pay_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付记录(模拟)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_record`
--

LOCK TABLES `payment_record` WRITE;
/*!40000 ALTER TABLE `payment_record` DISABLE KEYS */;
INSERT INTO `payment_record` VALUES (1,1,2,'PAY20260711153400AAB11095',10.00,0.00,'mock',1,'2026-07-11 15:34:00'),(2,4,2,'PAY202607131004090C00EB35',6.00,0.00,'mock',1,'2026-07-13 10:04:09'),(3,5,2,'PAY2026071408323933AD4687',45.00,0.00,'mock',1,'2026-07-14 08:32:40');
/*!40000 ALTER TABLE `payment_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `policy_news`
--

DROP TABLE IF EXISTS `policy_news`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy_news` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '政策资讯ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '正文',
  `source` varchar(100) DEFAULT NULL COMMENT '来源',
  `author` varchar(50) DEFAULT NULL COMMENT '作者/发布单位',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面',
  `status` tinyint DEFAULT '1' COMMENT '0草稿 1已发布',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_policy_time` (`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=127 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='政策类资讯(与招聘/活动分开)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `policy_news`
--

LOCK TABLES `policy_news` WRITE;
/*!40000 ALTER TABLE `policy_news` DISABLE KEYS */;
INSERT INTO `policy_news` VALUES (125,'[测试] 关于 Java 类课程学分认定指引','为保障 Java 等编程类学习成果认定规范，平台发布课程学分映射标准说明……','学分银行管委会','教务处',NULL,1,'2026-07-01 10:00:00','2026-07-17 19:39:14','2026-07-17 19:39:14',0),(126,'[测试] 2026 年暑期实践活动通知','鼓励学员参与企业实践活动，含编程马拉松、技术沙龙等。','平台公告','运营中心',NULL,1,'2026-06-20 09:00:00','2026-07-17 19:39:14','2026-07-17 19:39:14',0);
/*!40000 ALTER TABLE `policy_news` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `search_log`
--

DROP TABLE IF EXISTS `search_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `search_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID(可空)',
  `keyword` varchar(200) NOT NULL COMMENT '搜索词',
  `search_type` varchar(30) DEFAULT 'global' COMMENT 'global/course/job/forum',
  `result_count` int DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_search_keyword` (`keyword`),
  KEY `idx_search_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='搜索日志(智能搜索)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `search_log`
--

LOCK TABLES `search_log` WRITE;
/*!40000 ALTER TABLE `search_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `search_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_organization`
--

DROP TABLE IF EXISTS `sys_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_organization` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '机构/企业ID',
  `name` varchar(100) NOT NULL COMMENT '机构名称',
  `code` varchar(50) NOT NULL COMMENT '机构编码',
  `type` tinyint NOT NULL DEFAULT '3' COMMENT '类型: 1高校 2培训机构 3企业 4其他',
  `logo` varchar(255) DEFAULT NULL COMMENT 'Logo URL',
  `intro` text COMMENT '企业/机构简介',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `website` varchar(255) DEFAULT NULL COMMENT '官网',
  `join_status` tinyint DEFAULT '1' COMMENT '加盟状态: 0待审核 1已加盟 2已退出',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0停用 1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_org_type` (`type`),
  KEY `idx_org_status` (`join_status`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='加盟机构/企业表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_organization`
--

LOCK TABLES `sys_organization` WRITE;
/*!40000 ALTER TABLE `sys_organization` DISABLE KEYS */;
INSERT INTO `sys_organization` VALUES (1,'示例大学','ORG001',1,NULL,'示例高校加盟单位','张主任','13800000001',NULL,NULL,NULL,1,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(2,'示例科技集团','ORG002',3,NULL,'示例企业，发布招聘与活动','李经理','13800000002',NULL,NULL,NULL,1,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(3,'示例培训机构','ORG003',2,NULL,'职业技能培训机构','王老师','13800000003',NULL,NULL,NULL,1,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(4,'中南大学','ENT_1783921590454',3,NULL,NULL,'邵卓凡',NULL,NULL,NULL,NULL,1,1,'2026-07-13 13:46:30','2026-07-14 08:13:29',1),(5,'南中大学','ENT_1783922162319',3,NULL,NULL,'222',NULL,NULL,NULL,NULL,1,1,'2026-07-13 13:56:02','2026-07-13 13:56:02',0),(6,'星河职业学院','ORG004',1,NULL,'应用型职业院校，Java/C 互认试点','赵院长','13800000004',NULL,NULL,NULL,1,1,'2026-07-15 20:26:25','2026-07-15 20:27:13',0),(7,'云启软件培训','ORG005',2,NULL,'软件技能培训机构','钱老师','13800000005',NULL,NULL,NULL,1,1,'2026-07-15 20:26:25','2026-07-15 20:27:13',0),(8,'江城开放大学','ORG006',1,NULL,'开放教育与学分银行试点高校','孙主任','13800000006',NULL,NULL,NULL,1,1,'2026-07-15 20:26:25','2026-07-15 20:27:13',0);
/*!40000 ALTER TABLE `sys_organization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_tag`
--

DROP TABLE IF EXISTS `sys_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名: Java/C++等',
  `category` varchar(30) DEFAULT 'skill' COMMENT '分类: skill/course/job/general',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=341 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='统一标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tag`
--

LOCK TABLES `sys_tag` WRITE;
/*!40000 ALTER TABLE `sys_tag` DISABLE KEYS */;
INSERT INTO `sys_tag` VALUES (1,'Java','skill','2026-07-07 21:31:38'),(2,'C++','skill','2026-07-07 21:31:38'),(3,'Python','skill','2026-07-07 21:31:38'),(4,'前端开发','skill','2026-07-07 21:31:38'),(5,'数据库','skill','2026-07-07 21:31:38'),(6,'Spring Boot','skill','2026-07-07 21:31:38'),(7,'人工智能','skill','2026-07-07 21:31:38'),(8,'区块链','skill','2026-07-07 21:31:38');
/*!40000 ALTER TABLE `sys_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(BCrypt)',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色: 0学员 1企业用户 2系统管理员',
  `org_id` bigint DEFAULT NULL COMMENT '所属企业/机构ID(企业用户必填)',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0禁用 1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_org` (`org_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu','系统管理员',NULL,NULL,NULL,2,NULL,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(2,'student1','$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu','测试学员',NULL,NULL,NULL,0,NULL,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(3,'enterprise1','$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu','企业管理员',NULL,NULL,NULL,1,2,1,'2026-07-07 21:31:38','2026-07-07 21:31:38',0),(4,'中南大学','$2a$10$sKMn1ql4mCFmFw./69F6v.CskluOBF0qMsO1xv38YO5qkep6k57mq','邵卓凡',NULL,NULL,NULL,1,4,1,'2026-07-13 13:46:30','2026-07-14 08:13:29',1),(5,'111','$2a$10$7/7j/TKBythcWWbLHOILsetu8q7Qhslqf6la5b3Fe.G/bcu6SbMvu','222',NULL,NULL,NULL,1,5,1,'2026-07-13 13:56:02','2026-07-14 08:52:44',1);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_course`
--

DROP TABLE IF EXISTS `user_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学习记录ID',
  `user_id` bigint NOT NULL COMMENT '学员ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `progress` tinyint DEFAULT '0' COMMENT '进度0-100',
  `watched_seconds` int NOT NULL DEFAULT '0' COMMENT '????????????',
  `max_watched_position_seconds` int NOT NULL DEFAULT '0' COMMENT '?????????????????????',
  `last_position_seconds` int NOT NULL DEFAULT '0' COMMENT '????????????',
  `status` tinyint DEFAULT '0' COMMENT '0学习中 1已完成 2已退课',
  `paid_credit` decimal(10,2) DEFAULT '0.00' COMMENT '消耗学分',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`,`course_id`),
  KEY `idx_uc_course` (`course_id`),
  KEY `idx_uc_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户课程学习记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_course`
--

LOCK TABLES `user_course` WRITE;
/*!40000 ALTER TABLE `user_course` DISABLE KEYS */;
INSERT INTO `user_course` VALUES (1,2,40,100,0,0,0,1,20.00,'2026-05-31 15:48:50','2026-06-30 15:48:50'),(2,2,41,65,0,0,0,0,35.00,'2026-06-20 15:48:50',NULL),(3,2,42,15,0,0,0,0,15.00,'2026-07-05 15:48:50',NULL),(4,2,49,100,0,0,0,1,20.00,'2026-05-31 19:58:07','2026-06-30 19:58:07'),(5,2,50,65,0,0,0,0,35.00,'2026-06-20 19:58:07',NULL),(6,2,51,15,0,0,0,0,15.00,'2026-07-05 19:58:07',NULL),(7,2,52,100,0,0,0,1,20.00,'2026-05-31 20:05:25','2026-06-30 20:05:25'),(8,2,53,65,0,0,0,0,35.00,'2026-06-20 20:05:25',NULL),(9,2,54,15,0,0,0,0,15.00,'2026-07-05 20:05:25',NULL),(13,2,96,100,77,88,76,1,0.00,'2026-07-11 15:34:13','2026-07-11 15:36:24'),(14,2,64,0,0,0,0,0,0.00,'2026-07-11 15:34:36',NULL),(15,2,107,100,36,908,36,1,0.00,'2026-07-11 16:22:43','2026-07-15 20:56:18'),(16,2,106,0,0,0,0,0,0.00,'2026-07-11 16:30:09',NULL),(17,2,100,100,321,322,321,1,0.00,'2026-07-11 16:42:14','2026-07-11 16:51:29'),(18,2,85,0,0,0,0,0,0.00,'2026-07-11 17:51:05',NULL),(19,1,107,0,0,0,0,0,0.00,'2026-07-11 20:05:53',NULL),(20,3,107,0,0,0,0,0,0.00,'2026-07-11 21:03:56',NULL),(21,3,105,0,0,0,0,0,0.00,'2026-07-11 21:05:27',NULL),(22,2,97,0,0,0,0,0,0.00,'2026-07-11 21:45:27',NULL),(23,2,105,0,0,0,0,0,0.00,'2026-07-14 11:24:59',NULL),(24,2,104,0,0,0,0,0,0.00,'2026-07-15 20:01:59',NULL),(25,2,223,1,26,26,26,0,0.00,'2026-07-16 14:08:13',NULL),(26,2,218,0,9,9,9,0,0.00,'2026-07-17 08:31:03',NULL),(27,2,221,0,0,0,0,0,0.00,'2026-07-17 14:22:59',NULL);
/*!40000 ALTER TABLE `user_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_episode_progress`
--

DROP TABLE IF EXISTS `user_episode_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_episode_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `episode_id` bigint NOT NULL,
  `progress` tinyint NOT NULL DEFAULT '0',
  `watched_seconds` int NOT NULL DEFAULT '0',
  `max_watched_position_seconds` int NOT NULL DEFAULT '0',
  `last_position_seconds` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0??? 1???',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_episode` (`user_id`,`episode_id`),
  KEY `idx_user_episode_course` (`user_id`,`course_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_episode_progress`
--

LOCK TABLES `user_episode_progress` WRITE;
/*!40000 ALTER TABLE `user_episode_progress` DISABLE KEYS */;
INSERT INTO `user_episode_progress` VALUES (1,2,107,50,3,36,36,36,0,'2026-07-11 21:11:21','2026-07-11 21:11:21'),(2,2,106,49,0,0,0,0,0,'2026-07-11 21:41:46','2026-07-11 21:41:46'),(3,2,64,7,0,0,0,0,0,'2026-07-16 08:04:04','2026-07-16 08:04:04'),(4,2,223,123,1,26,26,26,0,'2026-07-17 13:56:13','2026-07-17 13:56:13'),(5,2,218,118,0,9,9,9,0,'2026-07-17 14:19:53','2026-07-17 14:19:53');
/*!40000 ALTER TABLE `user_episode_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_learning_checkin`
--

DROP TABLE IF EXISTS `user_learning_checkin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_learning_checkin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `checkin_date` date NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course_checkin` (`user_id`,`course_id`,`checkin_date`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_learning_checkin`
--

LOCK TABLES `user_learning_checkin` WRITE;
/*!40000 ALTER TABLE `user_learning_checkin` DISABLE KEYS */;
INSERT INTO `user_learning_checkin` VALUES (1,2,107,'2026-07-14','2026-07-14 13:16:09'),(2,2,107,'2026-07-15','2026-07-15 13:37:51');
/*!40000 ALTER TABLE `user_learning_checkin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_learning_profile`
--

DROP TABLE IF EXISTS `user_learning_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_learning_profile` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_job` varchar(100) DEFAULT NULL COMMENT '心仪职位',
  `summary` text COMMENT '学习画像摘要',
  `profile_json` json DEFAULT NULL COMMENT '画像详情(技能、偏好等)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI学习画像';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_learning_profile`
--

LOCK TABLES `user_learning_profile` WRITE;
/*!40000 ALTER TABLE `user_learning_profile` DISABLE KEYS */;
INSERT INTO `user_learning_profile` VALUES (1,'AI算法工程师','系统管理员角色，信用分100分，学习诚信度高。当前仅报名了一门《计算机视觉项目实践》课程，尚未开始学习，处于探索阶段。无任何已完成的课程、成就或论坛互动，学习行为记录几乎空白。建议尽快启动课程，明确学习路径。','{\"gaps\": [\"尚未开始任何课程学习\", \"无课程完成记录或成就\", \"未参与社区互动或知识分享\", \"无目标技能标签设定\"], \"stage\": \"入门\", \"skills\": [\"Python\", \"人工智能\"], \"summary\": \"系统管理员角色，信用分100分，学习诚信度高。当前仅报名了一门《计算机视觉项目实践》课程，尚未开始学习，处于探索阶段。无任何已完成的课程、成就或论坛互动，学习行为记录几乎空白。建议尽快启动课程，明确学习路径。\", \"strengths\": [\"高诚信度，学习态度端正\", \"已有明确的课程选择方向（计算机视觉）\"], \"targetJob\": \"AI算法工程师\", \"suggestions\": [\"立即开始《计算机视觉项目实践》课程学习\", \"设定1-2个短期学习目标并完成\", \"参与论坛讨论，积累学习信用\"]}','2026-07-14 08:42:23');
/*!40000 ALTER TABLE `user_learning_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_learning_reminder`
--

DROP TABLE IF EXISTS `user_learning_reminder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_learning_reminder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `enabled` tinyint NOT NULL DEFAULT '1',
  `interval_minutes` int NOT NULL DEFAULT '30',
  `last_remind_at` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course_reminder` (`user_id`,`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_learning_reminder`
--

LOCK TABLES `user_learning_reminder` WRITE;
/*!40000 ALTER TABLE `user_learning_reminder` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_learning_reminder` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_message`
--

DROP TABLE IF EXISTS `user_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_user_id` bigint NOT NULL COMMENT '发送方',
  `to_user_id` bigint NOT NULL COMMENT '接收方',
  `msg_type` tinyint DEFAULT '1' COMMENT '1普通私信 2面试邀请 3活动邀请 4系统通知',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `ref_type` varchar(50) DEFAULT NULL COMMENT '关联: job/activity',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `read_status` tinyint DEFAULT '0' COMMENT '0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_msg_to` (`to_user_id`,`read_status`),
  KEY `idx_msg_from` (`from_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户私信/通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_message`
--

LOCK TABLES `user_message` WRITE;
/*!40000 ALTER TABLE `user_message` DISABLE KEYS */;
INSERT INTO `user_message` VALUES (2,3,2,1,'1','666',NULL,NULL,1,'2026-07-13 09:56:42'),(3,2,3,1,'2','333',NULL,NULL,1,'2026-07-13 09:58:55'),(5,3,2,1,'投递结果：[测试] Java 后端开发工程师','示例科技集团 暂未录用您对「[测试] Java 后端开发工程师」的投递。\n\n请在「投递记录」查看详情。','application',3,1,'2026-07-13 11:13:44'),(10,3,2,1,'录用通知：[测试] 前端开发工程师','恭喜！示例科技集团 已录用您对「[测试] 前端开发工程师」的投递。\n\n请在「投递记录」查看详情。','application',8,1,'2026-07-14 14:03:18'),(12,3,2,1,'录用通知：[测试] Java 后端开发工程师','恭喜！示例科技集团 已录用您对「[测试] Java 后端开发工程师」的投递。\n\n请在「投递记录」查看详情。','application',9,1,'2026-07-14 14:17:19'),(14,3,2,1,'录用通知：[测试] 前端开发工程师','恭喜！示例科技集团 已录用您对「[测试] 前端开发工程师」的投递。\n\n请在「投递记录」查看详情。','application',10,1,'2026-07-14 14:22:41'),(15,3,2,3,'活动邀请：[测试] Java 技术沙龙：Spring Boot 实践','您好，示例科技集团 邀请您参加活动「[测试] Java 技术沙龙：Spring Boot 实践」。\n\n开始时间：2026-08-15 14:00\n结束时间：2026-08-15 17:00\n活动地点：示例科技集团报告厅\n参与奖励：5.00 秩点\n\ntest\n\n您也可在企业详情页主动报名。若收到本邀请，请在「活动邀请」页面接受或拒绝；接受后将自动完成报名。','activity',1,0,'2026-07-15 11:26:38'),(16,2,3,1,'1','test',NULL,NULL,1,'2026-07-15 11:27:40'),(17,1,2,4,'test','111','system',NULL,1,'2026-07-15 11:28:18'),(18,1,3,4,'test','111','system',NULL,1,'2026-07-15 11:28:18'),(19,2,3,1,'无标题','1',NULL,NULL,1,'2026-07-15 13:41:22'),(20,3,2,2,'面试邀请：[测试] Java 后端开发工程师','您好，示例科技集团 邀请您参加「[测试] Java 后端开发工程师」的面试。\n\n面试时间：2026-07-20 10:00\n面试方式：视频面试\n面试地点/方式：平台视频面试\n\n接受邀请后，可在「面试邀请」页面点击「进入面试」加入平台视频房间。\n\n请在「面试邀请」页面回复是否参加。','interview',8,0,'2026-07-17 21:06:31'),(21,1,2,4,'1','1','system',NULL,1,'2026-07-17 21:08:15'),(22,1,3,4,'1','1','system',NULL,0,'2026-07-17 21:08:15');
/*!40000 ALTER TABLE `user_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_resume`
--

DROP TABLE IF EXISTS `user_resume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_resume` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) DEFAULT '默认简历' COMMENT '简历名称',
  `content_json` json DEFAULT NULL COMMENT '结构化简历(JSON)',
  `file_url` varchar(255) DEFAULT NULL COMMENT '导出文件URL',
  `is_default` tinyint DEFAULT '1' COMMENT '是否默认',
  `version` int DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resume_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户简历';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_resume`
--

LOCK TABLES `user_resume` WRITE;
/*!40000 ALTER TABLE `user_resume` DISABLE KEYS */;
INSERT INTO `user_resume` VALUES (1,2,'默认简历','{\"email\": \"\", \"phone\": \"\", \"skills\": \"\", \"projects\": \"\", \"realName\": \"测试学员\", \"education\": \"\", \"selfIntro\": \"\", \"workExperience\": \"\"}',NULL,1,1,'2026-07-10 18:59:18','2026-07-17 21:14:57'),(2,3,'默认简历','{\"email\": \"\", \"phone\": \"\", \"skills\": \"\", \"projects\": \"\", \"realName\": \"企业管理员\", \"education\": \"\", \"selfIntro\": \"\", \"workExperience\": \"\"}',NULL,1,1,'2026-07-13 11:13:49','2026-07-13 11:13:49');
/*!40000 ALTER TABLE `user_resume` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_target_tag`
--

DROP TABLE IF EXISTS `user_target_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_target_tag` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_id` bigint NOT NULL COMMENT '目标技能标签',
  `source` varchar(30) DEFAULT 'manual' COMMENT 'manual/job/ai',
  PRIMARY KEY (`user_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户目标技能(心仪职位)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_target_tag`
--

LOCK TABLES `user_target_tag` WRITE;
/*!40000 ALTER TABLE `user_target_tag` DISABLE KEYS */;
INSERT INTO `user_target_tag` VALUES (2,1,'manual'),(2,5,'manual'),(2,6,'manual');
/*!40000 ALTER TABLE `user_target_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'credit_bank'
--

--
-- Dumping routines for database 'credit_bank'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-17 21:41:41
