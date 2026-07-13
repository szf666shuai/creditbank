CREATE TABLE IF NOT EXISTS course_comment_like (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    comment_id  BIGINT NOT NULL COMMENT '评论ID',
    user_id     BIGINT NOT NULL COMMENT '点赞用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_course_comment_like (comment_id, user_id),
    INDEX idx_course_comment_like_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评论点赞';
