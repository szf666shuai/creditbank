-- ============================================================
-- 终身学习学分银行平台 - 完整数据库初始化脚本
-- 字符集: utf8mb4 | 引擎: InnoDB
-- 维护分支: db
-- ============================================================

CREATE DATABASE IF NOT EXISTS credit_bank
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE credit_bank;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===================== 1. 系统与用户 =====================

CREATE TABLE IF NOT EXISTS sys_organization (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '机构/企业ID',
    name            VARCHAR(100) NOT NULL COMMENT '机构名称',
    code            VARCHAR(50)  NOT NULL UNIQUE COMMENT '机构编码',
    type            TINYINT NOT NULL DEFAULT 3 COMMENT '类型: 1高校 2培训机构 3企业 4其他',
    logo            VARCHAR(255) COMMENT 'Logo URL',
    intro           TEXT COMMENT '企业/机构简介',
    contact         VARCHAR(50)  COMMENT '联系人',
    phone           VARCHAR(20)  COMMENT '联系电话',
    email           VARCHAR(100) COMMENT '联系邮箱',
    address         VARCHAR(255) COMMENT '地址',
    website         VARCHAR(255) COMMENT '官网',
    join_status     TINYINT DEFAULT 1 COMMENT '加盟状态: 0待审核 1已加盟 2已退出',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0停用 1正常',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_org_type (type),
    INDEX idx_org_status (join_status, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加盟机构/企业表';

CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    real_name       VARCHAR(50)  COMMENT '真实姓名',
    phone           VARCHAR(20)  COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar          VARCHAR(255) COMMENT '头像URL',
    role            TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0学员 1企业用户 2系统管理员',
    org_id          BIGINT COMMENT '所属企业/机构ID(企业用户必填)',
    status          TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_user_role (role),
    INDEX idx_user_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 统一标签库（课程、招聘、AI推荐共用）
CREATE TABLE IF NOT EXISTS sys_tag (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    name            VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名: Java/C++等',
    category        VARCHAR(30) DEFAULT 'skill' COMMENT '分类: skill/course/job/general',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一标签表';

-- ===================== 2. 学分经济系统 =====================

CREATE TABLE IF NOT EXISTS credit_account (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id         BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    balance         DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '当前学分余额',
    total_earned    DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '累计获取',
    total_spent     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '累计消耗',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学分账户表';

CREATE TABLE IF NOT EXISTS credit_rule (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_code       VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name       VARCHAR(100) NOT NULL COMMENT '规则名称',
    amount          DECIMAL(10,2) NOT NULL COMMENT '学分变动值(正为获取负为消耗)',
    biz_type        VARCHAR(50) NOT NULL COMMENT '业务: post/comment/like/course_buy/course_complete等',
    description     VARCHAR(255) COMMENT '说明',
    enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学分规则配置表';

CREATE TABLE IF NOT EXISTS credit_transaction (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    type            TINYINT NOT NULL COMMENT '类型: 1获取 2转换 3增长 4消耗',
    amount          DECIMAL(12,2) NOT NULL COMMENT '变动学分(正负)',
    balance_after   DECIMAL(12,2) COMMENT '变动后余额',
    biz_type        VARCHAR(50) COMMENT '业务类型',
    source          VARCHAR(200) COMMENT '来源/用途说明',
    ref_type        VARCHAR(50) COMMENT '关联业务表类型',
    ref_id          BIGINT COMMENT '关联业务ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_credit_user (user_id),
    INDEX idx_credit_biz (biz_type, ref_type, ref_id),
    INDEX idx_credit_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学分流水表';

-- ===================== 3. 诚信评定 =====================

CREATE TABLE IF NOT EXISTS integrity_score (
    user_id         BIGINT PRIMARY KEY COMMENT '用户ID',
    score           INT NOT NULL DEFAULT 100 COMMENT '当前诚信分(0-100)',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户当前诚信分';

CREATE TABLE IF NOT EXISTS integrity_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    change_value    INT NOT NULL COMMENT '变动分值(正负)',
    score_after     INT NOT NULL COMMENT '变动后分数',
    event_type      TINYINT NOT NULL COMMENT '1加分 2扣分',
    reason          VARCHAR(255) NOT NULL COMMENT '原因',
    ref_type        VARCHAR(50) COMMENT '关联业务',
    ref_id          BIGINT COMMENT '关联ID',
    operator_id     BIGINT COMMENT '操作人(管理员/system)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_integrity_user (user_id),
    INDEX idx_integrity_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诚信评定记录表';

-- ===================== 4. 学习资源 / 课程 =====================

CREATE TABLE IF NOT EXISTS course (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '课程ID',
    org_id          BIGINT COMMENT '发布机构/企业ID',
    publisher_id    BIGINT COMMENT '发布人用户ID',
    title           VARCHAR(200) NOT NULL COMMENT '课程标题',
    description     TEXT COMMENT '课程描述',
    cover_url       VARCHAR(255) COMMENT '封面',
    price_credit    DECIMAL(10,2) DEFAULT 0 COMMENT '学分定价',
    price_money     DECIMAL(10,2) DEFAULT 0 COMMENT '现金定价(模拟)',
    duration_hours  DECIMAL(6,1) COMMENT '学时',
    credit_reward   DECIMAL(10,2) DEFAULT 0 COMMENT '完成奖励学分',
    status          TINYINT DEFAULT 1 COMMENT '0下架 1上架',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_course_org (org_id),
    INDEX idx_course_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习资源/课程表';

CREATE TABLE IF NOT EXISTS course_tag (
    course_id       BIGINT NOT NULL COMMENT '课程ID',
    tag_id          BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (course_id, tag_id),
    INDEX idx_ct_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程标签关联';

CREATE TABLE IF NOT EXISTS user_course (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学习记录ID',
    user_id         BIGINT NOT NULL COMMENT '学员ID',
    course_id       BIGINT NOT NULL COMMENT '课程ID',
    progress        TINYINT DEFAULT 0 COMMENT '进度0-100',
    status          TINYINT DEFAULT 0 COMMENT '0学习中 1已完成 2已退课',
    paid_credit     DECIMAL(10,2) DEFAULT 0 COMMENT '消耗学分',
    start_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time   DATETIME COMMENT '完成时间',
    UNIQUE KEY uk_user_course (user_id, course_id),
    INDEX idx_uc_course (course_id),
    INDEX idx_uc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户课程学习记录';

CREATE TABLE IF NOT EXISTS learning_certificate (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '证书ID',
    cert_no         VARCHAR(64) NOT NULL UNIQUE COMMENT '证书唯一编号',
    user_id         BIGINT NOT NULL COMMENT '学员ID',
    course_id       BIGINT COMMENT '关联课程ID',
    title           VARCHAR(200) NOT NULL COMMENT '证书名称',
    qr_content      VARCHAR(500) NOT NULL COMMENT '二维码内容(校验URL或JSON)',
    qr_image_url    VARCHAR(255) COMMENT '二维码图片',
    file_url        VARCHAR(255) COMMENT '证书PDF下载地址',
    blockchain_hash VARCHAR(128) COMMENT '区块链存证哈希',
    verify_status   TINYINT DEFAULT 0 COMMENT '0待校验 1已通过 2未通过',
    issued_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cert_user (user_id),
    INDEX idx_cert_course (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习证书表';

CREATE TABLE IF NOT EXISTS learning_archive (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '档案ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    title           VARCHAR(200) NOT NULL COMMENT '档案标题',
    archive_type    TINYINT DEFAULT 1 COMMENT '1课程 2活动 3成果 4其他',
    course_id       BIGINT COMMENT '关联课程',
    certificate_id  BIGINT COMMENT '关联证书',
    category        VARCHAR(50) COMMENT '类别',
    description     TEXT COMMENT '描述',
    start_date      DATE COMMENT '开始日期',
    end_date        DATE COMMENT '结束日期',
    credit_earned   DECIMAL(10,2) DEFAULT 0 COMMENT '获得学分',
    status          TINYINT DEFAULT 1 COMMENT '0进行中 1已完成',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_archive_user (user_id),
    INDEX idx_archive_type (archive_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='终身学习档案';

CREATE TABLE IF NOT EXISTS learning_achievement (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成果ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    title           VARCHAR(200) NOT NULL COMMENT '成果名称',
    type            TINYINT COMMENT '1证书 2课程 3项目 4其他',
    org_id          BIGINT COMMENT '认证机构',
    certificate_id  BIGINT COMMENT '关联证书',
    credit_value    DECIMAL(10,2) COMMENT '可兑换学分',
    file_url        VARCHAR(255) COMMENT '附件',
    verify_status   TINYINT DEFAULT 0 COMMENT '0待校验 1已通过 2未通过',
    blockchain_hash VARCHAR(128) COMMENT '存证哈希',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_achievement_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习成果表';

-- 学习进程统计（成长折线/柱状图）
CREATE TABLE IF NOT EXISTS learning_stat_daily (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    stat_date       DATE NOT NULL COMMENT '统计日期',
    study_minutes   INT DEFAULT 0 COMMENT '学习时长(分钟)',
    courses_completed INT DEFAULT 0 COMMENT '完成课程数',
    credit_earned   DECIMAL(10,2) DEFAULT 0 COMMENT '当日获得学分',
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日学习统计(图表)';

-- ===================== 5. 积分商城 =====================

CREATE TABLE IF NOT EXISTS mall_category (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name            VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id       BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_mall_cat_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城分类';

CREATE TABLE IF NOT EXISTS mall_product (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    category_id     BIGINT NOT NULL COMMENT '分类ID',
    name            VARCHAR(200) NOT NULL COMMENT '商品名称',
    description     TEXT COMMENT '描述',
    cover_url       VARCHAR(255) COMMENT '封面',
    product_type    TINYINT DEFAULT 1 COMMENT '1实物 2虚拟 3课程 4服务',
    ref_course_id   BIGINT COMMENT '关联课程(可选)',
    price_credit    DECIMAL(10,2) DEFAULT 0 COMMENT '学分价格',
    price_money     DECIMAL(10,2) DEFAULT 0 COMMENT '现金价格',
    stock           INT DEFAULT 9999 COMMENT '库存',
    status          TINYINT DEFAULT 1 COMMENT '0下架 1上架',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_product_cat (category_id),
    INDEX idx_product_type (product_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城商品';

CREATE TABLE IF NOT EXISTS mall_order (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no        VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id         BIGINT NOT NULL COMMENT '买家ID',
    total_credit    DECIMAL(12,2) DEFAULT 0 COMMENT '学分总额',
    total_money     DECIMAL(12,2) DEFAULT 0 COMMENT '现金总额',
    pay_method      TINYINT DEFAULT 1 COMMENT '1学分 2模拟支付 3混合',
    pay_status      TINYINT DEFAULT 0 COMMENT '0待支付 1已支付 2已取消 3已退款',
    pay_time        DATETIME COMMENT '支付时间',
    remark          VARCHAR(255) COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_user (user_id),
    INDEX idx_order_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城订单';

CREATE TABLE IF NOT EXISTS mall_order_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    product_id      BIGINT NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(200) COMMENT '商品快照名',
    quantity        INT NOT NULL DEFAULT 1,
    price_credit    DECIMAL(10,2) DEFAULT 0,
    price_money     DECIMAL(10,2) DEFAULT 0,
    INDEX idx_order_item_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE IF NOT EXISTS payment_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付记录ID',
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    pay_no          VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号',
    amount_credit   DECIMAL(12,2) DEFAULT 0,
    amount_money    DECIMAL(12,2) DEFAULT 0,
    pay_channel     VARCHAR(30) DEFAULT 'mock' COMMENT 'mock/simulated',
    pay_status      TINYINT DEFAULT 1 COMMENT '1成功 0失败',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pay_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录(模拟)';

-- ===================== 6. 论坛 =====================

CREATE TABLE IF NOT EXISTS forum_board (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '板块ID',
    name            VARCHAR(100) NOT NULL COMMENT '板块名称',
    description     VARCHAR(255) COMMENT '描述',
    icon            VARCHAR(50) COMMENT '图标',
    sort_order      INT DEFAULT 0,
    status          TINYINT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛板块';

CREATE TABLE IF NOT EXISTS forum_post (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    board_id        BIGINT NOT NULL COMMENT '板块ID',
    user_id         BIGINT NOT NULL COMMENT '发帖人',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    content         TEXT NOT NULL COMMENT '内容',
    view_count      INT DEFAULT 0,
    reply_count     INT DEFAULT 0,
    like_count      INT DEFAULT 0,
    is_top          TINYINT DEFAULT 0 COMMENT '是否置顶',
    status          TINYINT DEFAULT 1 COMMENT '0隐藏 1正常 2审核中',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_post_board (board_id),
    INDEX idx_post_user (user_id),
    INDEX idx_post_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子';

CREATE TABLE IF NOT EXISTS forum_reply (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回复ID',
    post_id         BIGINT NOT NULL COMMENT '帖子ID',
    user_id         BIGINT NOT NULL COMMENT '回复人',
    parent_id       BIGINT DEFAULT 0 COMMENT '父回复ID(楼中楼)',
    content         TEXT NOT NULL,
    like_count      INT DEFAULT 0,
    status          TINYINT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_reply_post (post_id),
    INDEX idx_reply_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复';

CREATE TABLE IF NOT EXISTS forum_like (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    target_type     TINYINT NOT NULL COMMENT '1帖子 2回复',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_like (user_id, target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录';

CREATE TABLE IF NOT EXISTS forum_report (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '举报ID',
    user_id         BIGINT NOT NULL COMMENT '举报人',
    target_type     TINYINT NOT NULL COMMENT '1帖子 2回复',
    target_id       BIGINT NOT NULL COMMENT '目标ID',
    reason          VARCHAR(255) NOT NULL COMMENT '举报原因',
    status          TINYINT DEFAULT 0 COMMENT '0待处理 1已处理 2驳回',
    handle_remark   VARCHAR(255) COMMENT '处理说明',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛举报';

-- ===================== 7. 招聘 / 活动 / 政策资讯 =====================

CREATE TABLE IF NOT EXISTS job_posting (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '招聘ID',
    org_id          BIGINT NOT NULL COMMENT '发布企业ID',
    publisher_id    BIGINT NOT NULL COMMENT '发布人',
    title           VARCHAR(200) NOT NULL COMMENT '职位名称',
    description     TEXT COMMENT '职位描述',
    requirements    TEXT COMMENT '任职要求',
    salary_range    VARCHAR(50) COMMENT '薪资范围',
    location        VARCHAR(100) COMMENT '工作地点',
    edu_requirement VARCHAR(50) COMMENT '学历要求',
    status          TINYINT DEFAULT 1 COMMENT '0下架 1招聘中',
    view_count      INT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_job_org (org_id),
    INDEX idx_job_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘信息';

CREATE TABLE IF NOT EXISTS job_tag (
    job_id          BIGINT NOT NULL COMMENT '招聘ID',
    tag_id          BIGINT NOT NULL COMMENT '技能标签ID',
    PRIMARY KEY (job_id, tag_id),
    INDEX idx_jt_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘技能标签';

CREATE TABLE IF NOT EXISTS job_application (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投递ID',
    job_id          BIGINT NOT NULL COMMENT '招聘ID',
    user_id         BIGINT NOT NULL COMMENT '求职者ID',
    resume_id       BIGINT COMMENT '使用的简历ID',
    cover_message   TEXT COMMENT '求职信',
    status          TINYINT DEFAULT 0 COMMENT '0已投递 1已查看 2面试中 3录用 4拒绝',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_job_user (job_id, user_id),
    INDEX idx_apply_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历投递';

CREATE TABLE IF NOT EXISTS activity (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    org_id          BIGINT NOT NULL COMMENT '主办企业/机构',
    publisher_id    BIGINT NOT NULL COMMENT '发布人',
    title           VARCHAR(200) NOT NULL COMMENT '活动名称',
    description     TEXT COMMENT '活动详情',
    location        VARCHAR(255) COMMENT '地点',
    start_time      DATETIME COMMENT '开始时间',
    end_time        DATETIME COMMENT '结束时间',
    max_participants INT COMMENT '人数上限',
    credit_reward   DECIMAL(10,2) DEFAULT 0 COMMENT '参与奖励学分',
    status          TINYINT DEFAULT 1 COMMENT '0取消 1报名中 2进行中 3已结束',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_activity_org (org_id),
    INDEX idx_activity_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动信息';

CREATE TABLE IF NOT EXISTS activity_registration (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    activity_id     BIGINT NOT NULL COMMENT '活动ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    status          TINYINT DEFAULT 0 COMMENT '0已报名 1已签到 2已取消 3已拒绝邀请',
    check_in_time   DATETIME COMMENT '签到时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_reg_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名';

CREATE TABLE IF NOT EXISTS policy_news (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '政策资讯ID',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    content         TEXT NOT NULL COMMENT '正文',
    source          VARCHAR(100) COMMENT '来源',
    author          VARCHAR(50) COMMENT '作者/发布单位',
    cover_url       VARCHAR(255) COMMENT '封面',
    status          TINYINT DEFAULT 1 COMMENT '0草稿 1已发布',
    publish_time    DATETIME COMMENT '发布时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_policy_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策类资讯(与招聘/活动分开)';

CREATE TABLE IF NOT EXISTS org_material (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资料ID',
    org_id          BIGINT NOT NULL COMMENT '企业ID',
    publisher_id    BIGINT NOT NULL COMMENT '发布人',
    title           VARCHAR(200) NOT NULL COMMENT '资料标题',
    description     TEXT COMMENT '描述',
    file_url        VARCHAR(255) COMMENT '文件地址',
    material_type   TINYINT DEFAULT 1 COMMENT '1文档 2视频 3其他',
    status          TINYINT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    INDEX idx_material_org (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业发布的学习资料';

-- ===================== 8. 私信 / 邀请 =====================

CREATE TABLE IF NOT EXISTS user_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    from_user_id    BIGINT NOT NULL COMMENT '发送方',
    to_user_id      BIGINT NOT NULL COMMENT '接收方',
    msg_type        TINYINT DEFAULT 1 COMMENT '1普通私信 2面试邀请 3活动邀请 4系统通知',
    title           VARCHAR(200) COMMENT '标题',
    content         TEXT NOT NULL COMMENT '内容',
    ref_type        VARCHAR(50) COMMENT '关联: job/activity',
    ref_id          BIGINT COMMENT '关联业务ID',
    read_status     TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_msg_to (to_user_id, read_status),
    INDEX idx_msg_from (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私信/通知';

CREATE TABLE IF NOT EXISTS interview_invitation (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请ID',
    job_id          BIGINT NOT NULL COMMENT '招聘ID',
    org_id          BIGINT NOT NULL COMMENT '企业ID',
    from_user_id    BIGINT NOT NULL COMMENT '企业发送人',
    to_user_id      BIGINT NOT NULL COMMENT '受邀学员',
    message_id      BIGINT COMMENT '关联私信ID',
    application_id  BIGINT COMMENT '关联投递ID',
    status          TINYINT DEFAULT 0 COMMENT '0待回复 1已接受 2已拒绝',
    invite_time     DATETIME COMMENT '面试时间',
    location        VARCHAR(255) COMMENT '面试地点/方式',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_invite_to (to_user_id),
    INDEX idx_invite_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试邀请';

CREATE TABLE IF NOT EXISTS activity_invitation (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请ID',
    activity_id     BIGINT NOT NULL COMMENT '活动ID',
    from_user_id    BIGINT NOT NULL COMMENT '企业发送人',
    to_user_id      BIGINT NOT NULL COMMENT '受邀学员',
    message_id      BIGINT COMMENT '关联私信',
    status          TINYINT DEFAULT 0 COMMENT '0待回复 1已接受 2已拒绝',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_act_invite_to (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动参与邀请';

-- ===================== 9. AI 助手 / 学习画像 =====================

CREATE TABLE IF NOT EXISTS user_resume (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    title           VARCHAR(100) DEFAULT '默认简历' COMMENT '简历名称',
    content_json    JSON COMMENT '结构化简历(JSON)',
    file_url        VARCHAR(255) COMMENT '导出文件URL',
    is_default      TINYINT DEFAULT 1 COMMENT '是否默认',
    version         INT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resume_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户简历';

CREATE TABLE IF NOT EXISTS user_learning_profile (
    user_id         BIGINT PRIMARY KEY COMMENT '用户ID',
    target_job      VARCHAR(100) COMMENT '心仪职位',
    summary         TEXT COMMENT '学习画像摘要',
    profile_json    JSON COMMENT '画像详情(技能、偏好等)',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI学习画像';

CREATE TABLE IF NOT EXISTS user_target_tag (
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    tag_id          BIGINT NOT NULL COMMENT '目标技能标签',
    source          VARCHAR(30) DEFAULT 'manual' COMMENT 'manual/job/ai',
    PRIMARY KEY (user_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户目标技能(心仪职位)';

CREATE TABLE IF NOT EXISTS job_match_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT NOT NULL COMMENT '学员ID',
    job_id          BIGINT NOT NULL COMMENT '职位ID',
    match_score     DECIMAL(5,2) NOT NULL COMMENT '契合度0-100',
    match_detail    JSON COMMENT '匹配详情',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_match_user (user_id),
    INDEX idx_match_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位契合度推荐记录';

CREATE TABLE IF NOT EXISTS search_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT COMMENT '用户ID(可空)',
    keyword         VARCHAR(200) NOT NULL COMMENT '搜索词',
    search_type     VARCHAR(30) DEFAULT 'global' COMMENT 'global/course/job/forum',
    result_count    INT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_search_keyword (keyword),
    INDEX idx_search_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索日志(智能搜索)';

CREATE TABLE IF NOT EXISTS ai_recommend_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT NOT NULL,
    recommend_type  VARCHAR(50) NOT NULL COMMENT 'course/job/learning_path',
    input_context   JSON COMMENT '输入上下文',
    output_result   JSON COMMENT '推荐结果',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_rec_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐记录';

-- ===================== 初始数据 =====================

INSERT INTO sys_organization (name, code, type, intro, contact, phone, join_status) VALUES
('示例大学', 'ORG001', 1, '示例高校加盟单位', '张主任', '13800000001', 1),
('示例科技集团', 'ORG002', 3, '示例企业，发布招聘与活动', '李经理', '13800000002', 1),
('示例培训机构', 'ORG003', 2, '职业技能培训机构', '王老师', '13800000003', 1);

INSERT INTO sys_user (username, password, real_name, role, org_id) VALUES
('admin', '$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu', '系统管理员', 2, NULL),
('student1', '$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu', '测试学员', 0, NULL),
('enterprise1', '$2a$10$jEdydp234.XnKIUAjnYbyOmR/4O2gPJRfsy5eL3qkfcV18SWaLEyu', '企业管理员', 1, 2);

INSERT INTO sys_tag (name, category) VALUES
('Java', 'skill'), ('C++', 'skill'), ('Python', 'skill'),
('前端开发', 'skill'), ('数据库', 'skill'), ('Spring Boot', 'skill'),
('人工智能', 'skill'), ('区块链', 'skill');

INSERT INTO credit_rule (rule_code, rule_name, amount, biz_type, description) VALUES
('POST_CREATE', '发帖奖励', 2.00, 'post', '发布论坛帖子'),
('REPLY_CREATE', '回复奖励', 1.00, 'reply', '回复帖子'),
('POST_LIKE', '被点赞奖励', 0.50, 'like_received', '帖子/回复被点赞'),
('COURSE_COMPLETE', '完成课程', 10.00, 'course_complete', '完成一门课程'),
('ACTIVITY_CHECKIN', '活动签到', 5.00, 'activity_checkin', '活动签到奖励'),
('REGISTER_BONUS', '新用户注册奖励', 20.00, 'register', '学员注册成功一次性奖励'),
('DAILY_CHECKIN', '每日签到', 2.00, 'daily_checkin', '每日签到奖励'),
('CHECKIN_STREAK_7', '连续签到7天', 5.00, 'checkin_streak', '连续签到满7天额外奖励'),
('ACHIEVE_VERIFY', '成果校验通过', 15.00, 'achieve_verify', '学习成果区块链校验通过奖励');

INSERT INTO forum_board (name, description, sort_order) VALUES
('校园频道', '学习交流、课程讨论', 1),
('校园集市', '二手、互助、生活', 2),
('求职经验', '面试、简历、职场', 3),
('政策解读', '学分银行政策讨论', 4);

INSERT INTO mall_category (name, parent_id, sort_order) VALUES
('课程资源', 0, 1),
('学习用品', 0, 2),
('虚拟商品', 0, 3),
('兑换专区', 0, 4);

INSERT INTO integrity_score (user_id, score) VALUES
(2, 100);

INSERT INTO credit_account (user_id, balance, total_earned) VALUES
(2, 50.00, 50.00);

SET FOREIGN_KEY_CHECKS = 1;
