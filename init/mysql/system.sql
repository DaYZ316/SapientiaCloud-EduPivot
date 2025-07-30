-- ===============================================================
-- Table: sys_permission
-- ===============================================================
create table sys_permission
(
    id              binary(16)                         not null
        primary key,
    parent_id       binary(16)                         null,
    permission_name varchar(50)                        not null,
    permission_key  varchar(100)                       not null,
    sort            int      default 0                 null,
    create_time     datetime default CURRENT_TIMESTAMP null,
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      int      default 0                 null
);

-- ===============================================================
-- Table: sys_role
-- ===============================================================
create table sys_role
(
    id          binary(16)                         not null
        primary key,
    role_name   varchar(30)                        not null,
    role_key    varchar(100)                       not null,
    sort        int      default 0                 null,
    status      int      default 0                 null,
    description varchar(500)                       null,
    create_time datetime default CURRENT_TIMESTAMP null,
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted  int      default 0                 null
);

-- ===============================================================
-- Table: sys_user
-- ===============================================================
create table sys_user
(
    id              binary(16)                         not null
        primary key,
    username        varchar(255)                       null,
    password        varchar(255)                       null,
    nick_name       varchar(255)                       null,
    email           varchar(255)                       null,
    mobile          varchar(255)                       null,
    gender          int                                null,
    avatar          varchar(255)                       null,
    status          int      default 0                 null,
    last_login_time datetime                           null,
    create_time     datetime default CURRENT_TIMESTAMP null,
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      int      default 0                 null
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


CREATE TABLE `student`
(
    `id`              binary(16)  NOT NULL COMMENT '学生ID',
    `student_code`    varchar(20) NOT NULL COMMENT '学号',
    `real_name`       varchar(50) NOT NULL COMMENT '学生真实姓名',
    `birth_date`      date         DEFAULT NULL COMMENT '出生日期',
    `admission_year`  int(4)       DEFAULT NULL COMMENT '入学年份',
    `major`           varchar(100) DEFAULT NULL COMMENT '专业',
    `academic_status` tinyint(1)   DEFAULT 0 COMMENT '学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)',
    `status`          tinyint(1)   DEFAULT 0 COMMENT '账号状态 (0=正常, 1=停用)',
    `description`     text         DEFAULT NULL COMMENT '自我描述',
    `sys_user_id`     binary(16)   DEFAULT NULL COMMENT '系统用户ID',
    `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1)   DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学生信息表';



CREATE TABLE `teacher`
(
    `id`             binary(16)  NOT NULL COMMENT '教师ID',
    `teacher_code`   varchar(20) NOT NULL COMMENT '教师工号',
    `real_name`      varchar(50) NOT NULL COMMENT '教师真实姓名',
    `birth_date`     date         DEFAULT NULL COMMENT '出生日期',
    `department`     varchar(100) DEFAULT NULL COMMENT '所属部门/学院',
    `education`      tinyint(1)   DEFAULT 1 COMMENT '学历 (0=专科, 1=本科, 2=硕士, 3=博士)',
    `specialization` varchar(200) DEFAULT NULL COMMENT '专业特长/研究方向',
    `status`         tinyint(1)   DEFAULT 0 COMMENT '账号状态 (0=正常, 1=停用)',
    `description`    text         DEFAULT NULL COMMENT '自我描述',
    `sys_user_id`    binary(16)   DEFAULT NULL COMMENT '系统用户ID',
    `create_time`    datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint(1)   DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='教师信息表';