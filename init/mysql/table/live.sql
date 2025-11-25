-- d:\Develop\code\SapientiaCloud-EduPivot\mini\SapientiaCloud-EduPivot\init\mysql\table\live.sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mg_live_room
-- ----------------------------
DROP TABLE IF EXISTS `mg_live_room`;
CREATE TABLE `mg_live_room`  (
  `id` binary(16) NOT NULL COMMENT '房间ID（主键）',
  `room_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '房间名称',
  `creator_id` binary(16) NOT NULL COMMENT '创建者用户ID（关联sys_user.id）',
  `course_id` binary(16) NULL DEFAULT NULL COMMENT '课程ID（关联mg_course.id，可选）',
  `classroom_id` binary(16) NULL DEFAULT NULL COMMENT '课堂记录ID（关联mg_course_record.id，推荐）',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '房间状态 (0=未开始,1=直播中,2=已结束)',
  `lk_room_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit房间名',
  `lk_room_sid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit房间SID',
  `lk_node_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit节点ID（多节点可用）',
  `start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `expected_end_time` datetime NULL DEFAULT NULL COMMENT '预计结束时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '实际结束时间',
  `max_participants` int NULL DEFAULT 500 COMMENT '最大并发人数（默认500）',
  `recording_enabled` tinyint(1) NULL DEFAULT 0 COMMENT '是否开启录制 (0=否,1=是)',
  `egress_task_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录制/推流任务ID',
  `egress_status` tinyint(1) NULL DEFAULT 0 COMMENT '录制状态 (0=待启动,1=进行中,2=完成,3=失败)',
  `recording_asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '录制文件/回放地址',
  `stream_output_urls` json NULL COMMENT 'RTMP推流目的地集合（JSON）',
  `metadata` json NULL COMMENT '房间元数据（公告、分组信息等）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除,1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lk_room_name`(`lk_room_name` ASC) USING BTREE,
  INDEX `idx_classroom_status`(`classroom_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '直播房间表（房间核心信息与LiveKit映射）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for mg_live_room_user
-- ----------------------------
DROP TABLE IF EXISTS `mg_live_room_user`;
CREATE TABLE `mg_live_room_user`  (
  `id` binary(16) NOT NULL COMMENT '关联ID（主键）',
  `live_room_id` binary(16) NOT NULL COMMENT '房间ID（关联mg_live_room.id）',
  `user_id` binary(16) NOT NULL COMMENT '用户ID（关联sys_user.id）',
  `role` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户角色 (0=学生,1=老师,2=助教)',
  `join_time` datetime NULL DEFAULT NULL COMMENT '进入时间',
  `leave_time` datetime NULL DEFAULT NULL COMMENT '离开时间',
  `lk_identity` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit客户端identity',
  `lk_participant_sid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'LiveKit参与者SID',
  `token_jti` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加入房间时的Token标识（审计）',
  `join_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '加入时IP',
  `client_platform` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端平台（web/ios/android）',
  `kicked_by` binary(16) NULL DEFAULT NULL COMMENT '执行移除的用户ID（管理员）',
  `kicked_at` datetime NULL DEFAULT NULL COMMENT '移除时间',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标记 (0=未删除,1=已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_room_user_join`(`live_room_id` ASC, `user_id` ASC, `join_time` ASC) USING BTREE,
  INDEX `idx_room_role`(`live_room_id` ASC, `role` ASC) USING BTREE,
  INDEX `idx_participant_sid`(`lk_participant_sid` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房间-用户关联表（进入与离开会话记录，含LiveKit映射）' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;