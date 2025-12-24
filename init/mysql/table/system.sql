-- ===============================================================
-- Table: sys_permission
-- ===============================================================
create table sys_permission
(
    id              binary(16)                           not null
        primary key,
    parent_id       binary(16)                           null,
    permission_name varchar(50)                          not null,
    permission_key  varchar(100)                         not null,
    sort            int        default 0                 null,
    create_time     datetime   default CURRENT_TIMESTAMP null,
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

-- ===============================================================
-- Table: sys_role
-- ===============================================================
create table sys_role
(
    id          binary(16)                           not null
        primary key,
    role_name   varchar(30)                          not null,
    role_key    varchar(100)                         not null,
    sort        int        default 0                 null,
    status      tinyint(1) default 0                 null,
    description varchar(500)                         null,
    create_time datetime   default CURRENT_TIMESTAMP null,
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted  tinyint(1) default 0                 null
);

-- ===============================================================
-- Table: sys_user
-- ===============================================================
create table sys_user
(
    id              binary(16)                           not null
        primary key,
    username        varchar(255)                         null,
    password        varchar(255)                         null,
    nick_name       varchar(255)                         null,
    email           varchar(255)                         null,
    mobile          varchar(255)                         null,
    gender          tinyint(1)                           null,
    avatar          varchar(255)                         null,
    status          tinyint(1) default 0                 null,
    github_id       varchar(100)                         null,
    wechat_id       varchar(100)                         null,
    last_login_time datetime                             null,
    create_time     datetime   default CURRENT_TIMESTAMP null,
    update_time     datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      tinyint(1) default 0                 null
);

-- ===============================================================
-- Table: sys_role_permission
-- ===============================================================
create table sys_role_permission
(
    role_id       binary(16) not null,
    permission_id binary(16) not null,
    primary key (role_id, permission_id)
);

create index idx_permission_id_on_role_permission
    on sys_role_permission (permission_id);

-- ===============================================================
-- Table: sys_user_role
-- ===============================================================
create table sys_user_role
(
    user_id binary(16) not null,
    role_id binary(16) not null,
    primary key (user_id, role_id)
);

create index idx_role_id_on_user_role
    on sys_user_role (role_id);

CREATE TABLE `sys_notification`
(
    `id`          binary(16)   NOT NULL COMMENT '通知ID',
    `user_id`     binary(16)   NOT NULL COMMENT '接收用户ID（sys_user.id）',
    `title`       varchar(200) NOT NULL COMMENT '通知标题',
    `content`     text         DEFAULT NULL COMMENT '通知内容（富文本HTML）',
    `attachment_urls` text     DEFAULT NULL COMMENT '附件URL列表（JSON字符串或逗号分隔）',
    `type`        tinyint(1)   DEFAULT 0 COMMENT '通知类型 (0=系统通知, 1=课程通知, 2=作业通知, 3=直播通知, 4=其他)',
    `status`      tinyint(1)   DEFAULT 0 COMMENT '阅读状态 (0=未读, 1=已读)',
    `read_time`   datetime     DEFAULT NULL COMMENT '阅读时间',
    `sender_id`   binary(16)   DEFAULT NULL COMMENT '发送者ID（sys_user.id，系统通知可为空）',
    `sender_name` varchar(100) DEFAULT NULL COMMENT '发送者名称',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`  tinyint(1)   DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统通知表';
