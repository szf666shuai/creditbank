-- 终身学习学分银行平台 - 数据库初始化脚本
-- 字符集: utf8mb4

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===================== 系统管理 =====================

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password    VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    real_name   VARCHAR(50)  COMMENT '真实姓名',
    phone       VARCHAR(20)  COMMENT '手机号',
    email       VARCHAR(100) COMMENT '邮箱',
    avatar      VARCHAR(255) COMMENT '头像URL',
    role        TINYINT DEFAULT 0 COMMENT '角色: 0学员 1机构管理员 2系统管理员',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    org_id      BIGINT COMMENT '所属机构ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_organization (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '机构ID',
    name        VARCHAR(100) NOT NULL COMMENT '机构名称',
    code        VARCHAR(50)  NOT NULL UNIQUE COMMENT '机构编码',
    type        TINYINT COMMENT '机构类型: 1高校 2培训机构 3企业 4其他',
    contact     VARCHAR(50)  COMMENT '联系人',
    phone       VARCHAR(20)  COMMENT '联系电话',
    address     VARCHAR(255) COMMENT '地址',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0停用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加盟机构表';

-- ===================== 积分商城 =====================

CREATE TABLE IF NOT EXISTS credit_account (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id     BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    balance     DECIMAL(12,2) DEFAULT 0 COMMENT '当前积分余额',
    total_earned DECIMAL(12,2) DEFAULT 0 COMMENT '累计获取积分',
    total_spent  DECIMAL(12,2) DEFAULT 0 COMMENT '累计消耗积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分账户表';

CREATE TABLE IF NOT EXISTS credit_transaction (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    type        TINYINT NOT NULL COMMENT '类型: 1获取 2转换 3增长 4消耗',
    amount      DECIMAL(12,2) NOT NULL COMMENT '积分数量',
    balance_after DECIMAL(12,2) COMMENT '交易后余额',
    source      VARCHAR(100) COMMENT '来源/用途说明',
    ref_id      BIGINT COMMENT '关联业务ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';

-- ===================== 学习档案与成果 =====================

CREATE TABLE IF NOT EXISTS learning_archive (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '档案ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    title       VARCHAR(200) NOT NULL COMMENT '档案标题',
    category    VARCHAR(50)  COMMENT '学习类别',
    description TEXT COMMENT '描述',
    start_date  DATE COMMENT '开始日期',
    end_date    DATE COMMENT '结束日期',
    status      TINYINT DEFAULT 0 COMMENT '状态: 0进行中 1已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='终身学习档案表';

CREATE TABLE IF NOT EXISTS learning_achievement (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成果ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    title       VARCHAR(200) NOT NULL COMMENT '成果名称',
    type        TINYINT COMMENT '类型: 1证书 2课程 3项目 4其他',
    org_id      BIGINT COMMENT '颁发/认证机构',
    credit_value DECIMAL(10,2) COMMENT '可兑换积分值',
    file_url    VARCHAR(255) COMMENT '成果文件URL',
    verify_status TINYINT DEFAULT 0 COMMENT '校验状态: 0待校验 1已通过 2未通过',
    blockchain_hash VARCHAR(128) COMMENT '区块链存证哈希',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习成果表';

-- ===================== 论坛 =====================

CREATE TABLE IF NOT EXISTS forum_post (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    user_id     BIGINT NOT NULL COMMENT '发帖用户',
    title       VARCHAR(200) NOT NULL COMMENT '标题',
    content     TEXT NOT NULL COMMENT '内容',
    category    VARCHAR(50) COMMENT '分类',
    view_count  INT DEFAULT 0 COMMENT '浏览量',
    reply_count INT DEFAULT 0 COMMENT '回复数',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0隐藏 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- ===================== 报名与签到 =====================

CREATE TABLE IF NOT EXISTS activity (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    title       VARCHAR(200) NOT NULL COMMENT '活动名称',
    org_id      BIGINT COMMENT '主办机构',
    description TEXT COMMENT '活动描述',
    location    VARCHAR(255) COMMENT '地点',
    start_time  DATETIME COMMENT '开始时间',
    end_time    DATETIME COMMENT '结束时间',
    max_participants INT COMMENT '最大人数',
    credit_reward DECIMAL(10,2) COMMENT '签到奖励积分',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0取消 1报名中 2进行中 3已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

CREATE TABLE IF NOT EXISTS activity_registration (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    status      TINYINT DEFAULT 0 COMMENT '状态: 0已报名 1已签到 2已取消',
    check_in_time DATETIME COMMENT '签到时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_user (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

-- ===================== 招聘求职 =====================

CREATE TABLE IF NOT EXISTS job_posting (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '职位ID',
    org_id      BIGINT COMMENT '发布机构',
    title       VARCHAR(200) NOT NULL COMMENT '职位名称',
    description TEXT COMMENT '职位描述',
    requirements TEXT COMMENT '任职要求',
    salary_range VARCHAR(50) COMMENT '薪资范围',
    location    VARCHAR(100) COMMENT '工作地点',
    status      TINYINT DEFAULT 1 COMMENT '状态: 0下架 1招聘中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘职位表';

-- ===================== 诚信评定 =====================

CREATE TABLE IF NOT EXISTS integrity_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT NOT NULL COMMENT '用户ID',
    score       INT DEFAULT 100 COMMENT '诚信分数(0-100)',
    event_type  TINYINT COMMENT '事件类型: 1加分 2扣分',
    reason      VARCHAR(255) COMMENT '原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诚信评定记录表';

-- ===================== 初始数据 =====================

INSERT INTO sys_organization (name, code, type, contact, phone) VALUES
('示例大学', 'ORG001', 1, '张主任', '13800000001'),
('示例培训机构', 'ORG002', 2, '李经理', '13800000002');

INSERT INTO sys_user (username, password, real_name, role, org_id) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 2, NULL);

SET FOREIGN_KEY_CHECKS = 1;
