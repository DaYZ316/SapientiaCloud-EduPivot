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