CREATE TABLE `mg_course`
(
    `id`                    binary(16)   NOT NULL COMMENT '课程ID',
    `course_name`           varchar(100) NOT NULL COMMENT '课程名称',
    `teacher_id`            binary(16)   NOT NULL COMMENT '授课教师ID',
    `assistant_teacher_ids` json         DEFAULT NULL COMMENT '辅助教学教师ID列表 (JSON array)',
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
    `grade`       decimal(5, 2) DEFAULT NULL COMMENT '成绩',
    `status`      tinyint(1)    DEFAULT 0 COMMENT '选课状态 (0=在读, 1=已退课, 2=已完成)',
    `create_time` datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`student_id`, `course_id`), -- 使用学生ID和课程ID作为联合主键
    KEY `idx_student_id` (`student_id`),     -- 学生ID索引
    KEY `idx_course_id` (`course_id`)        -- 课程ID索引
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='学生选课关联表';

CREATE TABLE `mg_course_chapter`
(
    `id`              binary(16)   NOT NULL COMMENT '章节ID',
    `course_id`       binary(16)   NOT NULL COMMENT '所属课程ID',
    `parent_id`       binary(16) DEFAULT NULL COMMENT '父章节ID (用于支持多级章节结构, NULL表示为一级章节)',
    `chapter_name`    varchar(150) NOT NULL COMMENT '章节名称',
    `chapter_content` text       DEFAULT NULL COMMENT '章节内容 (例如: 详细的文本、富文本标记等)',
    `sort`            int        DEFAULT 0 COMMENT '章节排序 (值越小越靠前)',
    `create_time`     datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1) DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`), -- 课程ID索引，加速查询特定课程下的所有章节
    KEY `idx_parent_id` (`parent_id`)  -- 父章节ID索引，加速查询子章节
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程章节信息表';

CREATE TABLE `mg_course_thread`
(
    `id`              binary(16)   NOT NULL COMMENT '主贴ID',
    `course_id`       binary(16)   NOT NULL COMMENT '所属课程ID',
    `user_id`         binary(16)   NOT NULL COMMENT '发帖用户ID',
    `title`           varchar(200) NOT NULL COMMENT '帖子标题',
    `content`         longtext     NOT NULL COMMENT '帖子内容 (使用longtext以支持富文本和长内容)',
    `is_pinned`       tinyint(1) DEFAULT 0 COMMENT '是否置顶 (1=是, 0=否)',
    `is_closed`       tinyint(1) DEFAULT 0 COMMENT '是否关闭/锁定 (1=是, 0=否, 关闭后无法回复)',
    `view_count`      int        DEFAULT 0 COMMENT '浏览次数',
    `reply_count`     int        DEFAULT 0 COMMENT '回复总数 (冗余字段, 提高查询性能)',
    `last_reply_time` datetime   DEFAULT NULL COMMENT '最后回复时间 (冗余字段, 用于排序)',
    `create_time`     datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1) DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_course_id_user_id` (`course_id`, `user_id`) -- 联合索引，便于查询某课程下的帖子或某用户的帖子
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程论坛主贴表';

CREATE TABLE `mg_thread_reply`
(
    `id`              binary(16) NOT NULL COMMENT '回复ID',
    `thread_id`       binary(16) NOT NULL COMMENT '所属主贴ID',
    `user_id`         binary(16) NOT NULL COMMENT '回复用户ID',
    `parent_reply_id` binary(16) DEFAULT NULL COMMENT '父回复ID (用于支持楼中楼回复)',
    `content`         text       NOT NULL COMMENT '回复内容',
    `create_time`     datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1) DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_thread_id` (`thread_id`),            -- 主贴ID索引，加速查询一个帖子下的所有回复
    KEY `idx_parent_reply_id` (`parent_reply_id`) -- 父回复ID索引，加速查询楼中楼
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='课程论坛回复表';