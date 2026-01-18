CREATE TABLE `mg_course`
(
    `id`                    binary(16)   NOT NULL COMMENT '课程ID',
    `course_name`           varchar(100) NOT NULL COMMENT '课程名称',
    `teacher_id`            binary(16)   NOT NULL COMMENT '授课教师ID',
    `description`           text         DEFAULT NULL COMMENT '课程描述',
    `cover_image_url`       varchar(512) DEFAULT NULL COMMENT '课程封面图片URL',
    `semester`              varchar(20)  DEFAULT NULL COMMENT '开设学期 (例如: 2025秋季)',
    `location`              varchar(100) DEFAULT NULL COMMENT '上课地点',
    `course_type`           tinyint(1)   DEFAULT 0 COMMENT '课程类型 (0=必修, 1=选修)',
    `status`                tinyint(1)   DEFAULT 0 COMMENT '课程状态 (0=正常, 1=停课)',
    `create_time`           datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`            tinyint(1)   DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_teacher_id` (`teacher_id`) -- 教师ID索引
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程信息表';

CREATE TABLE `mg_course_student`
(
    `student_id`  binary(16) NOT NULL COMMENT '学生ID',
    `course_id`   binary(16) NOT NULL COMMENT '课程ID',
    `grade`       decimal(7, 2) DEFAULT NULL COMMENT '成绩',
    `status`      tinyint(1)    DEFAULT 0 COMMENT '选课状态 (0=在读, 1=已退课, 2=已完成)',
    `create_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`student_id`, `course_id`), -- 使用学生ID和课程ID作为联合主键
    KEY `idx_student_id` (`student_id`),     -- 学生ID索引
    KEY `idx_course_id` (`course_id`)        -- 课程ID索引
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学生选课关联表';

CREATE TABLE `mg_course_assistant_teacher`
(
    `course_id`           binary(16)   NOT NULL COMMENT '课程ID',
    `assistant_teacher_id` binary(16)  NOT NULL COMMENT '辅助教师ID',
    `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_course_id` (`course_id`),
    KEY `idx_assistant_teacher_id` (`assistant_teacher_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程-辅助教师关联表';