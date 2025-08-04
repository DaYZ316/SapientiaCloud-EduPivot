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