/*
 Navicat Premium Dump SQL

 Source Server         : 117.72.194.197
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : 117.72.194.197:3306
 Source Schema         : sapientiacloud_edupivot

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 22/04/2026 20:18:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mg_classroom_question
-- ----------------------------
DROP TABLE IF EXISTS `mg_classroom_question`;
CREATE TABLE `mg_classroom_question`  (
  `id` binary(16) NOT NULL COMMENT '主键ID（唯一标识一条课堂-题目关联记录）',
  `classroom_id` binary(16) NOT NULL COMMENT '关联课堂ID（对应mg_course_record.id）',
  `course_id` binary(16) NOT NULL COMMENT '关联课程ID（对应mg_course.id）',
  `question_id` binary(16) NOT NULL COMMENT '关联题目ID（关联题库中题目的的唯一标识）',
  `question_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '题目标题',
  `publish_order` int NULL DEFAULT 0 COMMENT '题目发布顺序（控制课堂内题目展示的先后顺序）',
  `score` float NULL DEFAULT 0 COMMENT '题目分值值（支持小数）',
  `is_required` tinyint(1) NULL DEFAULT 0 COMMENT '是否必答 (0=选答, 1=必答)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '题目可作答开始时间（为空则默认随课堂开始）',
  `end_time` datetime NULL DEFAULT NULL COMMENT '题目作答截止时间（为空则默认随课堂结束）',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态 (0=待作答, 1=待批阅, 2=已批阅)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_classroom_question`(`classroom_id` ASC, `question_id` ASC, `is_deleted` ASC) USING BTREE COMMENT '确保同一课堂内题目不重复',
  INDEX `idx_classroom_id`(`classroom_id` ASC) USING BTREE COMMENT '快速查询某课堂的所有题目',
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE COMMENT '快速查询某课程的所有题目',
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE COMMENT '查询题目关联的课堂'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课堂-题目关联表（记录课堂发布的题目及配置信息）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_course
-- ----------------------------
DROP TABLE IF EXISTS `mg_course`;
CREATE TABLE `mg_course`  (
  `id` binary(16) NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `teacher_id` binary(16) NULL DEFAULT NULL COMMENT '授课教师ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程描述',
  `cover_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程封面图片URL',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '开设学期 (例如: 2025秋季)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
  `course_type` tinyint(1) NULL DEFAULT 0 COMMENT '课程类型 (0=必修, 1=选修)',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '课程状态 (0=正常, 1=停课)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_public` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否公开 (0=仅课程成员, 1=公开)',
  `is_deleted` tinyint(1) NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_course_assistant_teacher
-- ----------------------------
DROP TABLE IF EXISTS `mg_course_assistant_teacher`;
CREATE TABLE `mg_course_assistant_teacher`  (
  `course_id` binary(16) NOT NULL COMMENT '课程ID',
  `assistant_teacher_id` binary(16) NOT NULL COMMENT '辅助教师ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_assistant_teacher_id`(`assistant_teacher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程-辅助教师关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_course_record
-- ----------------------------
DROP TABLE IF EXISTS `mg_course_record`;
CREATE TABLE `mg_course_record`  (
  `id` binary(16) NOT NULL COMMENT '课程记录ID',
  `course_id` binary(16) NOT NULL COMMENT '关联课程ID',
  `teacher_id` binary(16) NOT NULL COMMENT '授课教师系统用户ID',
  `course_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `course_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程内容简介',
  `classroom_type` int NULL DEFAULT NULL COMMENT '教室类型 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)',
  `layout_rows` int NULL DEFAULT NULL COMMENT '行数 (仅传统布局或对齐布局使用)',
  `layout_columns` int NULL DEFAULT NULL COMMENT '列数 (仅传统布局或对齐布局使用)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '课程开始时间',
  `over_time` datetime NULL DEFAULT NULL COMMENT '课程结束时间',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '课程状态 (0=未开始, 1=进行中, 2=已结束, 3=已取消)',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `live_room_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '直播房间名',
  `live_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '直播状态 (0未开始, 1直播中, 2已结束, 3已暂停)',
  `live_lk_room_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit 房间名',
  `live_lk_room_sid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit 房间SID',
  `live_lk_node_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit 节点ID',
  `live_start_time` datetime NULL DEFAULT NULL COMMENT '直播实际开始时间',
  `live_expected_end_time` datetime NULL DEFAULT NULL COMMENT '直播预计结束时间',
  `live_end_time` datetime NULL DEFAULT NULL COMMENT '直播实际结束时间',
  `live_max_participants` int NULL DEFAULT 500 COMMENT '直播最大并发人数',
  `live_recording_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启录制',
  `live_egress_task_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录制任务ID',
  `live_egress_status` tinyint(1) NULL DEFAULT 0 COMMENT '录制状态(0待启动,1进行中,2完成,3失败)',
  `live_recording_asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录制文件地址',
  `live_stream_output_urls` json NULL COMMENT 'RTMP 推流集合(JSON)',
  `live_metadata` json NULL COMMENT '直播扩展元数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_live_lk_room_name`(`live_lk_room_name` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_live_status`(`live_status` ASC) USING BTREE,
  CONSTRAINT `fk_course_record_course` FOREIGN KEY (`course_id`) REFERENCES `mg_course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程教学记录表（含直播参数与教室布局）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_course_record_student
-- ----------------------------
DROP TABLE IF EXISTS `mg_course_record_student`;
CREATE TABLE `mg_course_record_student`  (
  `id` binary(16) NOT NULL COMMENT '主键ID',
  `record_id` binary(16) NOT NULL COMMENT '课堂记录ID（mg_course_record.id）',
  `course_id` binary(16) NOT NULL COMMENT '课程ID（mg_course.id）',
  `student_id` binary(16) NULL DEFAULT NULL COMMENT '学生ID（mg_student.id）',
  `teacher_id` binary(16) NULL DEFAULT NULL COMMENT '教师ID（mg_teacher.id）',
  `seat_index` int NULL DEFAULT NULL COMMENT '座位编号(从0开始)',
  `location_x` float NULL DEFAULT NULL COMMENT '3D坐标X',
  `location_y` float NULL DEFAULT NULL COMMENT '3D坐标Y',
  `location_z` float NULL DEFAULT NULL COMMENT '3D坐标Z',
  `rotation_y` float NULL DEFAULT NULL COMMENT '朝向角度(弧度制)',
  `seat_status` tinyint(1) NULL DEFAULT NULL COMMENT '座位状态(normal/marked/reserved/occupied)',
  `attendance_status` tinyint(1) NULL DEFAULT 0 COMMENT '出勤状态(0未签到,1已签到,2缺席)',
  `participation_score` float NULL DEFAULT NULL COMMENT '课堂互动得分',
  `live_join_time` datetime NULL DEFAULT NULL COMMENT '最近一次进入直播时间',
  `live_leave_time` datetime NULL DEFAULT NULL COMMENT '最近一次离开直播时间',
  `live_join_count` int NULL DEFAULT 0 COMMENT '进入直播次数',
  `live_lk_identity` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次 LiveKit identity',
  `live_lk_participant_sid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次 LiveKit participant SID',
  `live_token_jti` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次 token JTI',
  `live_join_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次加入IP',
  `live_client_platform` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次客户端平台',
  `live_kicked_at` datetime NULL DEFAULT NULL COMMENT '最近一次被移除时间',
  `live_remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最近一次备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_record_id`(`record_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_student_course` FOREIGN KEY (`course_id`) REFERENCES `mg_course` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_student_record` FOREIGN KEY (`record_id`) REFERENCES `mg_course_record` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_student_student` FOREIGN KEY (`student_id`) REFERENCES `mg_student` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_record_student_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `mg_teacher` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_teacher_student_exclusive` CHECK (((`student_id` is not null) and (`teacher_id` is null)) or ((`teacher_id` is not null) and (`student_id` is null)))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课堂学生参与表（含座位、出勤与直播参与信息）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_course_student
-- ----------------------------
DROP TABLE IF EXISTS `mg_course_student`;
CREATE TABLE `mg_course_student`  (
  `student_id` binary(16) NOT NULL COMMENT '学生ID',
  `course_id` binary(16) NOT NULL COMMENT '课程ID',
  `grade` decimal(7, 2) NULL DEFAULT NULL COMMENT '成绩',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '选课状态 (0=在读, 1=已退课, 2=已完成)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`student_id`, `course_id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生选课关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_student
-- ----------------------------
DROP TABLE IF EXISTS `mg_student`;
CREATE TABLE `mg_student`  (
  `id` binary(16) NOT NULL COMMENT '学生ID',
  `student_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生真实姓名',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `admission_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `academic_status` tinyint(1) NULL DEFAULT 0 COMMENT '学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自我描述',
  `sys_user_id` binary(16) NULL DEFAULT NULL COMMENT '系统用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_teacher
-- ----------------------------
DROP TABLE IF EXISTS `mg_teacher`;
CREATE TABLE `mg_teacher`  (
  `id` binary(16) NOT NULL COMMENT '教师ID',
  `teacher_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师工号',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师真实姓名',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门/学院',
  `education` tinyint(1) NULL DEFAULT 1 COMMENT '学历 (0=专科, 1=本科, 2=硕士, 3=博士)',
  `specialization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业特长/研究方向',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '自我描述',
  `sys_user_id` binary(16) NULL DEFAULT NULL COMMENT '系统用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notification_msg
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification_msg`;
CREATE TABLE `sys_notification_msg`  (
  `id` binary(16) NOT NULL COMMENT '通知ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容（富文本HTML）',
  `attachment_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '附件URL列表（JSON字符串或逗号分隔）',
  `type` tinyint(1) NULL DEFAULT 0 COMMENT '通知类型 (0=系统通知, 1=课程通知, 2=作业通知, 3=直播通知, 4=其他)',
  `sender_id` binary(16) NULL DEFAULT NULL COMMENT '发送者ID（sys_user.id，系统通知可为空）',
  `sender_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知正文表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_notification_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_notification_user`;
CREATE TABLE `sys_notification_user`  (
  `id` binary(16) NOT NULL COMMENT '收件记录ID',
  `notification_id` binary(16) NOT NULL COMMENT '通知ID（sys_notification_body.id）',
  `user_id` binary(16) NOT NULL COMMENT '接收用户ID（sys_user.id）',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '阅读状态 (0=未读, 1=已读)',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入箱时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_notification_user`(`notification_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_status_time`(`user_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_notification_id`(`notification_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统通知收件箱表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` binary(16) NULL DEFAULT NULL,
  `parent_id` binary(16) NULL DEFAULT NULL,
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `permission_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NULL DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` binary(16) NULL DEFAULT NULL,
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` binary(16) NULL DEFAULT NULL,
  `permission_id` binary(16) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` binary(16) NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `nick_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `mobile` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `gender` tinyint(1) NULL DEFAULT NULL,
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint(1) NULL DEFAULT NULL,
  `last_login_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `is_deleted` tinyint(1) NULL DEFAULT NULL,
  `github_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'GitHub用户ID',
  `wechat_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  INDEX `idx_github_id`(`github_id` ASC) USING BTREE,
  INDEX `idx_wechat_id`(`wechat_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` binary(16) NULL DEFAULT NULL,
  `role_id` binary(16) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for v_mg_course
-- ----------------------------
DROP VIEW IF EXISTS `v_mg_course`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_mg_course` AS select bin_to_uuid(`mg_course`.`id`) AS `id`,`mg_course`.`course_name` AS `course_name`,bin_to_uuid(`mg_course`.`teacher_id`) AS `teacher_id`,`mg_course`.`assistant_teacher_ids` AS `assistant_teacher_ids`,`mg_course`.`description` AS `description`,`mg_course`.`cover_image_url` AS `cover_image_url`,`mg_course`.`semester` AS `semester`,`mg_course`.`location` AS `location`,`mg_course`.`course_type` AS `course_type`,`mg_course`.`status` AS `status`,`mg_course`.`create_time` AS `create_time`,`mg_course`.`update_time` AS `update_time`,`mg_course`.`is_deleted` AS `is_deleted` from `mg_course`;

-- ----------------------------
-- View structure for v_mg_course_record_student
-- ----------------------------
DROP VIEW IF EXISTS `v_mg_course_record_student`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_mg_course_record_student` AS select bin_to_uuid(`mg_course_record_student`.`record_id`) AS `record_id`,bin_to_uuid(`mg_course_record_student`.`student_id`) AS `student_id`,bin_to_uuid(`mg_course_record_student`.`course_id`) AS `course_id`,`mg_course_record_student`.`seat_index` AS `seat_index`,`mg_course_record_student`.`location_x` AS `location_x`,`mg_course_record_student`.`location_y` AS `location_y`,`mg_course_record_student`.`location_z` AS `location_z`,`mg_course_record_student`.`rotation_y` AS `rotation_y`,`mg_course_record_student`.`seat_status` AS `seat_status`,`mg_course_record_student`.`attendance_status` AS `attendance_status`,`mg_course_record_student`.`participation_score` AS `participation_score`,`mg_course_record_student`.`create_time` AS `create_time`,`mg_course_record_student`.`update_time` AS `update_time` from `mg_course_record_student`;

-- ----------------------------
-- View structure for v_mg_course_student
-- ----------------------------
DROP VIEW IF EXISTS `v_mg_course_student`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_mg_course_student` AS select bin_to_uuid(`mg_course_student`.`student_id`) AS `student_id`,bin_to_uuid(`mg_course_student`.`course_id`) AS `course_id`,`mg_course_student`.`grade` AS `grade`,`mg_course_student`.`status` AS `status`,`mg_course_student`.`create_time` AS `create_time`,`mg_course_student`.`update_time` AS `update_time` from `mg_course_student`;

-- ----------------------------
-- View structure for v_mg_student
-- ----------------------------
DROP VIEW IF EXISTS `v_mg_student`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_mg_student` AS select bin_to_uuid(`mg_student`.`id`) AS `id`,`mg_student`.`student_code` AS `student_code`,`mg_student`.`real_name` AS `real_name`,`mg_student`.`birth_date` AS `birth_date`,`mg_student`.`admission_year` AS `admission_year`,`mg_student`.`major` AS `major`,`mg_student`.`academic_status` AS `academic_status`,`mg_student`.`description` AS `description`,bin_to_uuid(`mg_student`.`sys_user_id`) AS `sys_user_id`,`mg_student`.`create_time` AS `create_time`,`mg_student`.`update_time` AS `update_time`,`mg_student`.`is_deleted` AS `is_deleted` from `mg_student`;

-- ----------------------------
-- View structure for v_mg_teacher
-- ----------------------------
DROP VIEW IF EXISTS `v_mg_teacher`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_mg_teacher` AS select bin_to_uuid(`mg_teacher`.`id`) AS `id`,`mg_teacher`.`teacher_code` AS `teacher_code`,`mg_teacher`.`real_name` AS `real_name`,`mg_teacher`.`birth_date` AS `birth_date`,`mg_teacher`.`department` AS `department`,`mg_teacher`.`education` AS `education`,`mg_teacher`.`specialization` AS `specialization`,`mg_teacher`.`description` AS `description`,bin_to_uuid(`mg_teacher`.`sys_user_id`) AS `sys_user_id`,`mg_teacher`.`create_time` AS `create_time`,`mg_teacher`.`update_time` AS `update_time`,`mg_teacher`.`is_deleted` AS `is_deleted` from `mg_teacher`;

-- ----------------------------
-- View structure for v_sys_permission
-- ----------------------------
DROP VIEW IF EXISTS `v_sys_permission`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_sys_permission` AS select bin_to_uuid(`sys_permission`.`id`) AS `id`,bin_to_uuid(`sys_permission`.`parent_id`) AS `parent_id`,`sys_permission`.`permission_name` AS `permission_name`,`sys_permission`.`permission_key` AS `permission_key`,`sys_permission`.`sort` AS `sort`,`sys_permission`.`create_time` AS `create_time`,`sys_permission`.`update_time` AS `update_time`,`sys_permission`.`is_deleted` AS `is_deleted` from `sys_permission`;

-- ----------------------------
-- View structure for v_sys_role
-- ----------------------------
DROP VIEW IF EXISTS `v_sys_role`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_sys_role` AS select bin_to_uuid(`sys_role`.`id`) AS `id`,`sys_role`.`role_name` AS `role_name`,`sys_role`.`role_key` AS `role_key`,`sys_role`.`sort` AS `sort`,`sys_role`.`status` AS `status`,`sys_role`.`description` AS `description`,`sys_role`.`create_time` AS `create_time`,`sys_role`.`update_time` AS `update_time`,`sys_role`.`is_deleted` AS `is_deleted` from `sys_role`;

-- ----------------------------
-- View structure for v_sys_role_permission
-- ----------------------------
DROP VIEW IF EXISTS `v_sys_role_permission`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_sys_role_permission` AS select bin_to_uuid(`sys_role_permission`.`role_id`) AS `role_id`,bin_to_uuid(`sys_role_permission`.`permission_id`) AS `permission_id` from `sys_role_permission`;

-- ----------------------------
-- View structure for v_sys_user
-- ----------------------------
DROP VIEW IF EXISTS `v_sys_user`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_sys_user` AS select bin_to_uuid(`sys_user`.`id`) AS `id`,`sys_user`.`username` AS `username`,`sys_user`.`password` AS `password`,`sys_user`.`nick_name` AS `nick_name`,`sys_user`.`email` AS `email`,`sys_user`.`mobile` AS `mobile`,`sys_user`.`gender` AS `gender`,`sys_user`.`avatar` AS `avatar`,`sys_user`.`status` AS `status`,`sys_user`.`last_login_time` AS `last_login_time`,`sys_user`.`create_time` AS `create_time`,`sys_user`.`update_time` AS `update_time`,`sys_user`.`is_deleted` AS `is_deleted` from `sys_user`;

-- ----------------------------
-- View structure for v_sys_user_role
-- ----------------------------
DROP VIEW IF EXISTS `v_sys_user_role`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_sys_user_role` AS select bin_to_uuid(`sys_user_role`.`user_id`) AS `user_id`,bin_to_uuid(`sys_user_role`.`role_id`) AS `role_id` from `sys_user_role`;

SET FOREIGN_KEY_CHECKS = 1;
