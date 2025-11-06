/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : sapientiacloud_edupivot

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 16/10/2025 22:53:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mg_course
-- ----------------------------
DROP TABLE IF EXISTS `mg_course`;
CREATE TABLE `mg_course`
(
    `id`                    binary(16)                                                    NOT NULL COMMENT '课程ID',
    `course_name`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
    `teacher_id`            binary(16)                                                    NULL DEFAULT NULL COMMENT '授课教师ID',
    `assistant_teacher_ids` json                                                          NULL COMMENT '辅助教学教师ID列表 (JSON array)',
    `description`           text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '课程描述',
    `cover_image_url`       varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程封面图片URL',
    `semester`              varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL COMMENT '开设学期 (例如: 2025秋季)',
    `location`              varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上课地点',
    `course_type`           tinyint(1)                                                    NULL DEFAULT 0 COMMENT '课程类型 (0=必修, 1=选修)',
    `status`                tinyint(1)                                                    NULL DEFAULT 0 COMMENT '课程状态 (0=正常, 1=停课)',
    `create_time`           datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`            tinyint(1)                                                    NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_teacher_id` (`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '课程信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mg_course
-- ----------------------------
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E01, '高等数学', 0x336F943E20CD47098455DCC1DDBB65B9, '[
  \"0198086c-fcd1-7a09-b3c3-537de3493332\",
  \"019831a2-19da-7a9c-a878-3926484143bf\",
  \"01983258-89ce-7ac0-9645-06d6a39318a3\",
  \"01983258-8fb7-7c0d-add5-23313e77aa92\"
]', '高等数学是理工科学生必修的基础课程，主要学习微积分、线性代数等数学基础知识。',
        'http://127.0.0.1:31589/sapientiacloud-edupivot/course-covers/image_1757937858613.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20250915%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250915T120419Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=cac4abd7df4ce53fd101ae58747e9cc84cb432514913a291d4f2caf0cf42a649',
        '2024春季学期', '教学楼A101', 0, 0, '2024-01-15 09:00:00', '2025-09-21 16:45:00', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E02, '数据结构与算法', 0x019832FE2FEE7D4C885ECCA9FF295F41, '[
  \"0198086c-fcd1-7a09-b3c3-537de3493332\",
  \"019831a2-19da-7a9c-a878-3926484143bf\",
  \"01983258-89ce-7ac0-9645-06d6a39318a3\",
  \"01983258-8fb7-7c0d-add5-23313e77aa92\",
  \"01983258-95d8-703a-b430-530c1f74796a\",
  \"01983258-9e67-7df4-9313-b29c89ba5789\",
  \"01983258-a460-7641-877a-408382ed30b7\",
  \"01983258-a9ac-7846-8c5e-de44d0b55579\",
  \"01983258-fec2-77dc-be9f-4adaead542b8\",
  \"019832fe-1d54-7dff-b951-89f76527518b\",
  \"01983309-c398-703b-9bd3-13a9a28fbf2b\",
  \"019832fe-2fee-7d4c-885e-cca9ff295f41\",
  \"01983305-7e72-7cc6-95dc-f9d45e09519f\",
  \"336f943e-20cd-4709-8455-dcc1ddbb65b9\"
]', '学习计算机科学中的基础数据结构（如数组、链表、栈、队列、树、图）和常用算法（如排序、搜索、动态规划等）。',
        'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼B201', 0, 0, '2024-01-16 10:30:00', '2025-09-24 20:51:57', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E03, '人工智能导论', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '介绍人工智能的基本概念、发展历史、主要技术领域和应用场景，包括机器学习、深度学习、自然语言处理等。',
        'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼C301', 1, 0, '2024-01-17 14:20:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E04, '软件工程', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习软件开发生命周期、需求分析、系统设计、编码实现、测试维护等软件工程的核心概念和方法。',
        'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼A205', 0, 0, '2024-01-18 11:15:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E05, '数据库系统原理', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习关系数据库的基本理论、SQL语言、数据库设计、事务处理、并发控制等数据库系统的核心知识。',
        'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼B102', 0, 0, '2024-01-19 16:45:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E06, 'Web前端开发', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习HTML、CSS、JavaScript等前端技术，掌握响应式设计、前端框架（如Vue.js、React）等现代前端开发技能。',
        'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼C205', 1, 0, '2024-01-20 13:30:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E07, '计算机网络', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习计算机网络的基本概念、协议体系、网络设备、网络安全等网络技术的核心内容。',
        'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼A301', 0, 0, '2024-01-21 09:45:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E08, '移动应用开发', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习Android和iOS平台的应用开发技术，包括UI设计、数据存储、网络通信、性能优化等移动开发技能。',
        'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼B301', 1, 1, '2024-01-22 15:20:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E09, '操作系统', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习操作系统的基本概念、进程管理、内存管理、文件系统、设备管理等操作系统核心功能。',
        'https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼A401', 0, 0, '2024-01-23 10:10:00', '2025-09-06 11:29:47', 0);
INSERT INTO `mg_course`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E10, '机器学习', 0x336F943E20CD47098455DCC1DDBB65B9, NULL,
        '学习机器学习的基本算法、模型训练、特征工程、模型评估等机器学习技术的核心内容。',
        'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=400&h=300&fit=crop', '2024春季学期',
        '计算机楼C401', 1, 0, '2024-01-24 14:50:00', '2025-09-08 19:57:08', 0);

-- ----------------------------
-- Table structure for mg_course_student
-- ----------------------------
DROP TABLE IF EXISTS `mg_course_student`;
CREATE TABLE `mg_course_student`
(
    `student_id`  binary(16)    NOT NULL COMMENT '学生ID',
    `course_id`   binary(16)    NOT NULL COMMENT '课程ID',
    `grade`       decimal(5, 2) NULL DEFAULT NULL COMMENT '成绩',
    `status`      tinyint(1)    NULL DEFAULT 0 COMMENT '选课状态 (0=在读, 1=已退课, 2=已完成)',
    `create_time` datetime      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`student_id`, `course_id`) USING BTREE,
    INDEX `idx_student_id` (`student_id` ASC) USING BTREE,
    INDEX `idx_course_id` (`course_id` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '学生选课关联表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mg_course_student
-- ----------------------------
INSERT INTO `mg_course_student`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 0x78D44B4ABECD4F659461F2DCDDA03E01, 98.00, 0, '2025-10-07 11:33:35',
        '2025-10-07 03:37:10');
INSERT INTO `mg_course_student`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 0x78D44B4ABECD4F659461F2DCDDA03E02, 87.50, 0, '2025-09-04 20:40:00',
        '2025-09-21 08:37:16');
INSERT INTO `mg_course_student`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 0x78D44B4ABECD4F659461F2DCDDA03E04, 89.00, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');
INSERT INTO `mg_course_student`
VALUES (0x01992CB6D9A778419E2B9DCC1C9EA0A7, 0x78D44B4ABECD4F659461F2DCDDA03E01, 91.25, 0, '2025-09-09 20:40:00',
        '2025-10-06 09:58:59');
INSERT INTO `mg_course_student`
VALUES (0x01992CB6D9A778419E2B9DCC1C9EA0A7, 0x78D44B4ABECD4F659461F2DCDDA03E05, 86.75, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');
INSERT INTO `mg_course_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 0x78D44B4ABECD4F659461F2DCDDA03E01, 56.74, 0, '2025-10-07 11:21:41',
        '2025-10-07 03:42:52');
INSERT INTO `mg_course_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 0x78D44B4ABECD4F659461F2DCDDA03E03, 88.00, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');
INSERT INTO `mg_course_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 0x78D44B4ABECD4F659461F2DCDDA03E10, 93.50, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');
INSERT INTO `mg_course_student`
VALUES (0x4EFCEABC8CD64169A1F24962A5318358, 0x78D44B4ABECD4F659461F2DCDDA03E02, 92.00, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');
INSERT INTO `mg_course_student`
VALUES (0x78D44B4ABECD4F659461F2DCDDA03E0C, 0x78D44B4ABECD4F659461F2DCDDA03E03, 90.25, 0, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00');

-- ----------------------------
-- Table structure for mg_student
-- ----------------------------
DROP TABLE IF EXISTS `mg_student`;
CREATE TABLE `mg_student`
(
    `id`              binary(16)                                                    NOT NULL COMMENT '学生ID',
    `student_code`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '学号',
    `real_name`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '学生真实姓名',
    `birth_date`      date                                                          NULL DEFAULT NULL COMMENT '出生日期',
    `admission_year`  int                                                           NULL DEFAULT NULL COMMENT '入学年份',
    `major`           varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
    `academic_status` tinyint(1)                                                    NULL DEFAULT 0 COMMENT '学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)',
    `description`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '自我描述',
    `sys_user_id`     binary(16)                                                    NULL DEFAULT NULL COMMENT '系统用户ID',
    `create_time`     datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      tinyint(1)                                                    NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '学生信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mg_student
-- ----------------------------
INSERT INTO `mg_student`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 'S202300501415', 'Lesli Waelchi', '2003-03-15', 2023, '计算机科学与技术', 0,
        '热爱编程，喜欢学习新技术，希望在软件开发领域有所建树。', 0x01983258AFB079B3847B0ABF6A991C88,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB6D9A778419E2B9DCC1C9EA0A7, 'S202300501416', 'Rudolph Smith', '2004-07-22', 2023, '软件工程', 0,
        '对软件工程充满热情，喜欢团队合作，希望成为一名优秀的软件工程师。', 0x01992CB6D9A778419E2B9DCC1C9EA0A7,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 'S202300501417', 'Terrence Cronin', '2003-11-08', 2023,
        '数据科学与大数据技术', 0, '对数据科学和大数据技术很感兴趣，希望在这个领域深入学习。',
        0x01992CB7735D78579C8CEDBE566AD0D2, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D3, 'S202300501418', 'Alice Johnson', '2003-05-12', 2023, '人工智能', 0,
        '对人工智能和机器学习充满兴趣，希望在这个前沿领域有所贡献。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00',
        0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D4, 'S202300501419', 'Bob Wilson', '2004-09-30', 2023, '网络工程', 0,
        '热爱网络技术，对网络安全和网络架构设计很感兴趣。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D5, 'S202300501420', 'Carol Davis', '2003-12-18', 2023, '信息安全', 0,
        '对信息安全领域很感兴趣，希望成为一名网络安全专家。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D6, 'S202300501421', 'David Brown', '2004-04-25', 2023, '物联网工程', 0,
        '对物联网技术充满热情，希望在这个新兴领域有所发展。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D7, 'S202300501422', 'Emma Miller', '2003-08-14', 2023, '数字媒体技术', 0,
        '热爱数字媒体和游戏开发，希望在这个创意领域有所成就。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D8, 'S202300501423', 'Frank Garcia', '2004-01-20', 2023, '电子信息工程', 0,
        '对电子技术和通信工程很感兴趣，希望在这个领域深入学习。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D9, 'S202300501424', 'Grace Lee', '2003-06-03', 2023, '自动化', 0,
        '对自动化技术和智能控制很感兴趣，希望在这个领域有所发展。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00',
        0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0DA, 'S202300501425', 'Henry Martinez', '2004-10-17', 2023,
        '机械设计制造及其自动化', 0, '热爱机械设计和制造技术，希望成为一名优秀的机械工程师。', NULL,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0DB, 'S202300501426', 'Ivy Anderson', '2003-02-28', 2023, '材料科学与工程', 0,
        '对材料科学很感兴趣，希望在新材料研发领域有所贡献。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x01992CB7735D78579C8CEDBE566AD0DC, 'S202300501427', 'Jack Taylor', '2004-11-11', 2023, '土木工程', 0,
        '对土木工程和建筑结构很感兴趣，希望在这个领域深入学习。', NULL, '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_student`
VALUES (0x249F4441A88642BFA8839719EC9FA13D, '20230051414', 'zs', '2025-09-10', 2023, 'jsj', 0, 'sdaa',
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 23:06:14', '2025-09-11 15:07:28', 1);
INSERT INTO `mg_student`
VALUES (0x317D7686FECB42958DA84DE76AB1054A, '202300501414', '赵盛', '2025-09-08', 2023, '计算机科学与技术', 0, '1Z',
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 21:55:37', '2025-09-11 13:56:33', 1);
INSERT INTO `mg_student`
VALUES (0x718C8AC57C6C4A51AAFEB40B0B63AD70, '203213213211', '3123232', '2025-09-03', 2023, '1331', 0, '13213',
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 22:05:59', '2025-09-11 15:03:37', 1);
INSERT INTO `mg_student`
VALUES (0x89DD2BE92E554B63B21F8223CB241E4D, 'asd', 'sad', '2025-09-09', 2023, 'jsjk', 0, NULL,
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 21:59:26', '2025-09-11 14:04:03', 1);
INSERT INTO `mg_student`
VALUES (0xAB901E3D9D2045E49AA1B399B4E7479C, '2023005103123221', 'zs', '2025-08-31', 2023, 'sds', 0, 'dadfsa d',
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 22:04:30', '2025-09-11 14:05:35', 1);
INSERT INTO `mg_student`
VALUES (0xB3DBF5E8B68E479ABA8A524D3A7CF38A, '202300501414', 'zs', '2025-09-02', 2023, 'jsj', 0, NULL,
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 23:04:16', '2025-09-11 15:05:46', 1);
INSERT INTO `mg_student`
VALUES (0xB7990849B7A643CA9D2F04A30949B375, '202300501414', 'zs', '2025-09-03', 2023, 'sdsd', 0, NULL,
        0x0197EE62BE087C57B1FF42B1FA3C8B3F, '2025-09-11 23:07:55', '2025-09-16 18:47:56', 0);
INSERT INTO `mg_student`
VALUES (0xCB5F555F86BA4777A9F29A9E863958C0, '2023005014111', 'sss', NULL, 2023, '8888', 1, '',
        0x01983258847B7546B6D0FE06FE3B66FF, '2025-09-05 13:39:54', '2025-09-09 12:45:33', 0);

-- ----------------------------
-- Table structure for mg_teacher
-- ----------------------------
DROP TABLE IF EXISTS `mg_teacher`;
CREATE TABLE `mg_teacher`
(
    `id`             binary(16)                                                    NOT NULL COMMENT '教师ID',
    `teacher_code`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '教师工号',
    `real_name`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '教师真实姓名',
    `birth_date`     date                                                          NULL DEFAULT NULL COMMENT '出生日期',
    `department`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门/学院',
    `education`      tinyint(1)                                                    NULL DEFAULT 1 COMMENT '学历 (0=专科, 1=本科, 2=硕士, 3=博士)',
    `specialization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业特长/研究方向',
    `description`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         NULL COMMENT '自我描述',
    `sys_user_id`    binary(16)                                                    NULL DEFAULT NULL COMMENT '系统用户ID',
    `create_time`    datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`     tinyint(1)                                                    NULL DEFAULT NULL COMMENT '逻辑删除标记 (0=未删除, 1=已删除)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '教师信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mg_teacher
-- ----------------------------
INSERT INTO `mg_teacher`
VALUES (0x0198086CFCD17A09B3C3537DE3493332, 'T20230051442', 'Jimmie Stehr', '1985-03-15', '计算机学院', 3,
        '软件工程、系统架构', '专注于软件工程和系统架构设计，具有丰富的项目开发经验。', 0x0198319FAC4577FC80C79106DA57CAFF,
        '2025-09-09 20:40:00', '2025-09-10 10:26:38', 0);
INSERT INTO `mg_teacher`
VALUES (0x019831A219DA7A9CA8783926484143BF, 'T20230051443', 'Rudolf O\'Conner', '1982-07-22', '数学学院', 3,
        '高等数学、线性代数', '数学博士，专长于高等数学和线性代数教学，发表多篇学术论文。',
        0x019831A219DA7A9CA8783926484143BF, '2025-09-09 20:40:00', '2025-09-10 00:11:42', 0);
INSERT INTO `mg_teacher`
VALUES (0x0198325889CE7AC0964506D6A39318A3, 'T20230051444', 'Judi Sipes', '1988-11-08', '物理学院', 2,
        '理论物理、量子力学', '物理学硕士，擅长理论物理和量子力学教学，深受学生喜爱。', 0x0198325889CE7AC0964506D6A39318A3,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x019832588FB77C0DADD523313E77AA92, 'T20230051445', 'Valeria Sipes', '1983-05-12', '化学学院', 3,
        '有机化学、分析化学', '化学博士，在有机化学和分析化学领域有深入研究。', 0x019832588FB77C0DADD523313E77AA92,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x0198325895D8703AB430530C1F74796A, 'T20230051446', 'Joesph Harris', '1987-09-30', '计算机学院', 2,
        '数据库系统、算法设计', '计算机科学硕士，专长于数据库系统和算法设计。', 0x0198325895D8703AB430530C1F74796A,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x019832589E677DF49313B29C89BA5789, 'T20230051447', 'Jeffrey Toy Sr.', '1984-12-18', '电子工程学院', 3,
        '电路设计、信号处理', '电子工程博士，在电路设计和信号处理方面有丰富经验。', 0x019832589E677DF49313B29C89BA5789,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x01983258A4607641877A408382ED30B7, 'T20230051448', 'Dong Bode', '1986-04-25', '机械工程学院', 2,
        '机械设计、材料力学', '机械工程硕士，专长于机械设计和材料力学。', 0x01983258A4607641877A408382ED30B7,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x01983258A9AC78468C5EDE44D0B55579, 'T20230051449', 'Maisie Funk', '1989-08-14', '外语学院', 2, '英语教学、翻译',
        '英语硕士，专长于英语教学和翻译，具有海外留学经历。', 0x01983258A9AC78468C5EDE44D0B55579, '2025-09-09 20:40:00',
        '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x01983258FEC277DCBE9F4ADAEAD542B8, 'T20230051450', 'Shenna Zemlak', '1981-01-20', '经济管理学院', 3,
        '宏观经济学、金融学', '经济学博士，在宏观经济学和金融学领域有深入研究。', 0x01983258FEC277DCBE9F4ADAEAD542B8,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x019832FE1D547DFFB95189F76527518B, 'T20230051451', 'Winifred Beer IV', '1985-06-03', '艺术学院', 2,
        '视觉设计、艺术史', '艺术硕士，专长于视觉设计和艺术史教学。', 0x019832FE1D547DFFB95189F76527518B,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x019832FE2FEE7D4C885ECCA9FF295F41, 'T20230051452', 'Jerilyn Bergnaum', '1983-10-17', '体育学院', 2,
        '体育教育、运动训练', '体育教育硕士，专长于体育教育和运动训练。', 0x019832FE2FEE7D4C885ECCA9FF295F41,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x019833057E727CC695DCF9D45E09519F, 'T20230051453', 'Marty Armstrong', '1987-02-28', '生物学院', 3,
        '分子生物学、遗传学', '生物学博士，在分子生物学和遗传学领域有深入研究。', 0x019833057E727CC695DCF9D45E09519F,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x01983309C398703B9BD313A9A28FBF2B, 'T20230051454', 'Dr. Kevin Pfeffer', '1980-11-11', '医学院', 3,
        '内科学、临床医学', '医学博士，专长于内科学和临床医学教学。', 0x01983309C398703B9BD313A9A28FBF2B,
        '2025-09-09 20:40:00', '2025-09-09 20:40:00', 0);
INSERT INTO `mg_teacher`
VALUES (0x01984F1405607DDFA38BCADE91CB6755, 'T20230051455', 'Thomasine Botsford', '1986-07-05', '法学院', 3,
        '民法学、刑法学', '法学博士，专长于民法学和刑法学教学。', 0x01984F1405607DDFA38BCADE91CB6755,
        '2025-09-09 20:40:00', '2025-09-10 02:38:08', 1);
INSERT INTO `mg_teacher`
VALUES (0x336F943E20CD47098455DCC1DDBB65B9, '20230051441', '陈平安', '2025-08-03', '计算机学院', 1, '全栈架构师', NULL,
        0x0198086CFCD17A09B3C3537DE3493332, '2025-08-28 15:55:45', '2025-09-11 23:00:58', 0);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`              binary(16)                                                    NULL DEFAULT NULL,
    `parent_id`       binary(16)                                                    NULL DEFAULT NULL,
    `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL,
    `permission_key`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `sort`            int                                                           NULL DEFAULT NULL,
    `create_time`     datetime                                                      NULL DEFAULT NULL,
    `update_time`     datetime                                                      NULL DEFAULT NULL,
    `is_deleted`      tinyint(1)                                                    NULL DEFAULT 0
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission`
VALUES (0x01980D4FE11075B29BA4876FDFB7F66E, NULL, '用户权限', 'system:user:query', 0, '2025-07-15 16:59:56',
        '2025-07-23 23:21:46', 1);
INSERT INTO `sys_permission`
VALUES (0x0198179991207D8BA7AA2583A8CB40C6, NULL, 'add', 'add', 0, '2025-07-17 16:56:37', '2025-07-17 16:56:37', 1);
INSERT INTO `sys_permission`
VALUES (0x01981799AC6A78F597994ECE44E64DF4, NULL, 'ass', 'ass', 0, '2025-07-17 16:56:44', '2025-07-17 16:56:44', 1);
INSERT INTO `sys_permission`
VALUES (0x01983337DB407A19BD4000BD2000FAC3, 0x01980D4FE11075B29BA4876FDFB7F66E, '用户详情查询', 'system:user:info', 0,
        '2025-07-23 01:39:15', '2025-07-23 01:39:15', 1);
INSERT INTO `sys_permission`
VALUES (0x0198403004CA7B81B1E155A65D4C8A02, NULL, '用户权限', 'system:user:all', 1, '2025-07-25 14:05:46',
        '2025-07-27 20:32:41', 0);
INSERT INTO `sys_permission`
VALUES (0x01984032644870CCA0D25035379DD2C2, 0x0198403004CA7B81B1E155A65D4C8A02, '用户查询权限', 'system:user:query', 1,
        '2025-07-25 14:08:21', '2025-07-27 19:33:21', 0);
INSERT INTO `sys_permission`
VALUES (0x019840332EBF7B6BA5CDAE83C535996A, 0x0198403004CA7B81B1E155A65D4C8A02, '用户添加权限', 'system:user:add', 1,
        '2025-07-25 14:09:13', '2025-07-25 14:10:52', 0);
INSERT INTO `sys_permission`
VALUES (0x01984033E7E370078504F1374069CD69, 0x0198403004CA7B81B1E155A65D4C8A02, '用户更新权限', 'system:user:edit', 1,
        '2025-07-25 14:10:00', '2025-07-25 14:14:57', 0);
INSERT INTO `sys_permission`
VALUES (0x01984034865371B980C72F6CE99CCAEE, 0x01984032644870CCA0D25035379DD2C2, '用户删除权限', 'system:role:delete', 1,
        '2025-07-25 14:10:41', '2025-07-25 14:10:59', 1);
INSERT INTO `sys_permission`
VALUES (0x0198403562AB76FFA9155FCDC614F6C7, 0x0198403004CA7B81B1E155A65D4C8A02, '用户删除权限', 'system:user:delete', 1,
        '2025-07-25 14:11:37', '2025-07-25 14:11:37', 0);
INSERT INTO `sys_permission`
VALUES (0x01984035D4E27A889617BBCC99B5EFDA, NULL, '角色权限', 'system:role:all', 2, '2025-07-25 14:12:06',
        '2025-07-25 14:12:06', 0);
INSERT INTO `sys_permission`
VALUES (0x019840367C4C7B13BB584ACFBC14DCB4, 0x01984035D4E27A889617BBCC99B5EFDA, '角色查询权限', 'system:role:query', 2,
        '2025-07-25 14:12:49', '2025-07-25 14:12:49', 0);
INSERT INTO `sys_permission`
VALUES (0x0198403744007D47B02AD08D2B0EB217, 0x01984035D4E27A889617BBCC99B5EFDA, '角色添加权限', 'system:role:add', 2,
        '2025-07-25 14:13:40', '2025-07-25 14:13:40', 0);
INSERT INTO `sys_permission`
VALUES (0x01984037DF6B7B04B19463A22B7B5CB9, 0x01984035D4E27A889617BBCC99B5EFDA, '角色更新权限', 'system:role:edit', 2,
        '2025-07-25 14:14:20', '2025-07-27 13:22:09', 0);
INSERT INTO `sys_permission`
VALUES (0x01984038EF7D7664A1942B778A39D136, 0x01984035D4E27A889617BBCC99B5EFDA, '角色删除权限', 'system:role:delete', 2,
        '2025-07-25 14:15:30', '2025-07-25 14:15:30', 0);
INSERT INTO `sys_permission`
VALUES (0x01984039A9F87DD880AD77F493C16112, NULL, '权限权限', 'system:permission:all', 3, '2025-07-25 14:16:18',
        '2025-07-25 14:16:18', 0);
INSERT INTO `sys_permission`
VALUES (0x0198403B4FF376609FE48242DC1DC670, 0x01984039A9F87DD880AD77F493C16112, '权限查询权限',
        'system:permission:query', 3, '2025-07-25 14:18:06', '2025-07-25 14:18:06', 0);
INSERT INTO `sys_permission`
VALUES (0x0198403BA0827921A16C1399984A1FD6, 0x01984039A9F87DD880AD77F493C16112, '权限添加权限', 'system:permission:add',
        3, '2025-07-25 14:18:26', '2025-07-25 14:18:26', 0);
INSERT INTO `sys_permission`
VALUES (0x0198403BF4AC7CD9870B327A5F5B3E9F, 0x01984039A9F87DD880AD77F493C16112, '权限更新权限',
        'system:permission:edit', 3, '2025-07-25 14:18:48', '2025-07-25 14:18:48', 0);
INSERT INTO `sys_permission`
VALUES (0x0198403C57C8716985AEB998BB1A2898, 0x01984039A9F87DD880AD77F493C16112, '权限删除权限',
        'system:permission:delete', 3, '2025-07-25 14:19:13', '2025-07-25 14:19:13', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A4C6E5B75498A80B17688106F23, NULL, '教师权限', 'manage:teacher:all', 4, '2025-09-09 01:07:56',
        '2025-09-09 01:25:11', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A5CCE85716ABC4A67A3C741ECF0, 0x01992A4C6E5B75498A80B17688106F23, '教师查询权限', 'manage:teacher:query',
        4, '2025-09-09 01:25:49', '2025-09-09 01:28:55', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A5D5C177AFBA3C096D90498DEC4, 0x01992A4C6E5B75498A80B17688106F23, '教师添加权限', 'manage:teacher:add', 4,
        '2025-09-09 01:26:26', '2025-09-09 01:29:02', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A5DAFAA78ACA3A5CBD5D62F0835, 0x01992A4C6E5B75498A80B17688106F23, '教师更新权限', 'manage:teacher:edit',
        4, '2025-09-09 01:26:47', '2025-09-09 01:29:07', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A5E0F417AC38957AC114454225D, 0x01992A4C6E5B75498A80B17688106F23, '教师删除权限', 'manage:teacher:delete',
        4, '2025-09-09 01:27:11', '2025-09-09 01:29:14', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A5F1A117F83808649E83302C0E5, NULL, '学生权限', 'manage:student:all', 5, '2025-09-09 01:28:20',
        '2025-09-09 01:28:20', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A605E28750E928270E337144037, 0x01992A5F1A117F83808649E83302C0E5, '学生查询权限', 'manage:student:query',
        5, '2025-09-09 01:29:43', '2025-09-09 01:29:43', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A6099BB7824872BEBF536A9B48D, 0x01992A5F1A117F83808649E83302C0E5, '学生添加权限', 'manage:student:add', 5,
        '2025-09-09 01:29:58', '2025-09-09 01:29:58', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A60E2D1724C838E4F7114BFD0F8, 0x01992A5F1A117F83808649E83302C0E5, '学生更新权限', 'manage:student:edit',
        5, '2025-09-09 01:30:17', '2025-09-09 01:30:17', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A6139FF76B6BDAEE75FF2EB9BD2, 0x01992A5F1A117F83808649E83302C0E5, '学生删除权限', 'manage:student:delete',
        5, '2025-09-09 01:30:39', '2025-09-09 01:30:39', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A61D9EB73D3994B86D2C26138F0, NULL, '课程权限', 'manage:course:all', 6, '2025-09-09 01:31:20',
        '2025-09-09 01:31:20', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A62156178D5A14D8996812A0B54, 0x01992A61D9EB73D3994B86D2C26138F0, '课程查询权限', 'manage:course:query',
        6, '2025-09-09 01:31:35', '2025-09-09 01:31:35', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A6266477C03B0524F0C095453E6, 0x01992A61D9EB73D3994B86D2C26138F0, '课程添加权限', 'manage:course:add', 6,
        '2025-09-09 01:31:56', '2025-09-09 01:31:56', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A62AAD571A5B619F42EB6BFA08E, 0x01992A61D9EB73D3994B86D2C26138F0, '课程更新权限', 'manage:course:edit', 6,
        '2025-09-09 01:32:13', '2025-09-09 01:32:13', 0);
INSERT INTO `sys_permission`
VALUES (0x01992A62E78974389796659E022429B2, 0x01992A61D9EB73D3994B86D2C26138F0, '课程删除权限', 'manage:course:delete',
        6, '2025-09-09 01:32:29', '2025-09-09 01:32:29', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          binary(16)                                                    NULL DEFAULT NULL,
    `role_name`   varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL DEFAULT NULL,
    `role_key`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `sort`        int                                                           NULL DEFAULT NULL,
    `status`      tinyint(1)                                                    NULL DEFAULT NULL,
    `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `create_time` datetime                                                      NULL DEFAULT NULL,
    `update_time` datetime                                                      NULL DEFAULT NULL,
    `is_deleted`  tinyint(1)                                                    NULL DEFAULT NULL
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (0x01980D1F114073D58CF57D7FB7C2B53B, '超级管理员', 'ADMIN', 1, 0, '超级管理员角色', '2025-07-15 16:06:37',
        '2025-07-15 16:44:33', 0);
INSERT INTO `sys_role`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, '教师', 'TEACHER', 2, 0, '人民教师', '2025-07-30 09:56:53',
        '2025-07-30 09:57:02', 0);
INSERT INTO `sys_role`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, '学生', 'STUDENT', 3, 0, '学生', '2025-07-30 09:57:45',
        '2025-07-30 09:57:45', 0);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
    `role_id`       binary(16) NULL DEFAULT NULL,
    `permission_id` binary(16) NULL DEFAULT NULL
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01984032644870CCA0D25035379DD2C2);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x019840367C4C7B13BB584ACFBC14DCB4);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x0198403B4FF376609FE48242DC1DC670);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A5CCE85716ABC4A67A3C741ECF0);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A605E28750E928270E337144037);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A62156178D5A14D8996812A0B54);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A5F1A117F83808649E83302C0E5);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A6099BB7824872BEBF536A9B48D);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A60E2D1724C838E4F7114BFD0F8);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A6139FF76B6BDAEE75FF2EB9BD2);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A61D9EB73D3994B86D2C26138F0);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A6266477C03B0524F0C095453E6);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A62AAD571A5B619F42EB6BFA08E);
INSERT INTO `sys_role_permission`
VALUES (0x0198590BF85F7B2FA5FE4CC0994F72D5, 0x01992A62E78974389796659E022429B2);
INSERT INTO `sys_role_permission`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, 0x01992A62156178D5A14D8996812A0B54);
INSERT INTO `sys_role_permission`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, 0x01992A605E28750E928270E337144037);
INSERT INTO `sys_role_permission`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, 0x01992A5CCE85716ABC4A67A3C741ECF0);
INSERT INTO `sys_role_permission`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, 0x0198403B4FF376609FE48242DC1DC670);
INSERT INTO `sys_role_permission`
VALUES (0x0198590CC13A7706AB89025C6BD7AAFC, 0x01984032644870CCA0D25035379DD2C2);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`              binary(16)                                                    NULL DEFAULT NULL,
    `username`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `password`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `nick_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `email`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `mobile`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `gender`          tinyint(1)                                                    NULL DEFAULT NULL,
    `avatar`          varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
    `status`          tinyint(1)                                                    NULL DEFAULT NULL,
    `last_login_time` datetime                                                      NULL DEFAULT NULL,
    `create_time`     datetime                                                      NULL DEFAULT NULL,
    `update_time`     datetime                                                      NULL DEFAULT NULL,
    `is_deleted`      tinyint(1)                                                    NULL DEFAULT NULL
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES (0x0197EE62BE087C57B1FF42B1FA3C8B3F, 'admin', '$2a$10$R3cselz6LLpoog028pfkU.gNHcLMURQRV8QqOKwoqPEmq4DviwWr.',
        'Admin', 'admin@qq.com', '18039801656', 0,
        'http://127.0.0.1:31589/sapientiacloud-edupivot/avatar/avatar_1758019623800.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20250916%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250916T104703Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=383aef8f0cc16436b3d77699df795365da4f0586c206e4990a8f5ebed85d6752',
        0, '2025-10-15 20:11:07', '2025-07-09 16:52:18', '2025-10-15 20:11:08', 0);
INSERT INTO `sys_user`
VALUES (0x0198086CFCD17A09B3C3537DE3493332, 'zhaosheng', '$2a$10$2s582mhreZPnfeFDG/cKzeElwyTsF0QZ958I.2CPTwmFX7wdtjXd.',
        'Test', '654011721@qq.com', '18839932908', 1,
        'http://127.0.0.1:31589/sapientiacloud-edupivot/avatar/avatar_1758092352851.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20250917%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250917T065913Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=6cdc036bde7f1ebadb49e07fd446f31d45cd8c2fcedbb9a2c54a88e2060315ac',
        0, '2025-09-17 14:58:55', '2025-07-14 18:13:37', '2025-09-17 14:59:15', 0);
INSERT INTO `sys_user`
VALUES (0x0198319FAC4577FC80C79106DA57CAFF, 'EhebNABf', '$2a$10$z7AJnOGubTi0kjpHcsZok.lR6vGf2ZSM.H6h/iNBh3FL2qszK1AWS',
        'Miss Jimmie Stehr', '', '', 0, '', 0, NULL, '2025-07-22 18:13:25', '2025-07-26 00:14:21', 0);
INSERT INTO `sys_user`
VALUES (0x019831A219DA7A9CA8783926484143BF, '4LwvhdNY', '$2a$10$MHiyvZa330AJffnrgRh6IekaZSM84TErtWxeOAKwy7wRIqtgO7q6S',
        'Rudolf O\'Conner', '', '', 0, '', 0, NULL, '2025-07-22 18:16:04', '2025-07-22 18:16:04', 0);
INSERT INTO `sys_user`
VALUES (0x01983258847B7546B6D0FE06FE3B66FF, 'vLBVA8D2', '$2a$10$70qz0blbg2LfjxUEs5pMiOZfZNFV2qB3GrJRzP7x8lrWD1o32uMTG',
        'Marcella Stehr', '', '', 0, '', 0, '2025-09-05 13:39:00', '2025-07-22 21:35:19', '2025-09-05 13:39:00', 0);
INSERT INTO `sys_user`
VALUES (0x0198325889CE7AC0964506D6A39318A3, 'OB5snUUc', '$2a$10$fAnOGXG9F6rb.I/wiPblO.aI0TTQWm02EmBZivSWwwN59afwl2eu2',
        'Judi Sipes', '', '', 0, '', 0, NULL, '2025-07-22 21:35:20', '2025-07-22 21:35:20', 0);
INSERT INTO `sys_user`
VALUES (0x019832588FB77C0DADD523313E77AA92, '38symVIN', '$2a$10$ViXFdkHjLTA0Y/DISBusYO9PpwklXhrOmdsw5fbj8o5UQ9hTX4QPm',
        'Valeria Sipes', '', '', 0, '', 0, NULL, '2025-07-22 21:35:22', '2025-07-22 21:35:22', 0);
INSERT INTO `sys_user`
VALUES (0x0198325895D8703AB430530C1F74796A, 'W93i3G4D', '$2a$10$PfLDKhUE/4rEXJknyMPZbuQGjJY/aiXkn78SxHjkzjKP5NFjQsPgS',
        'Joesph Harris', '', '', 0, '', 0, NULL, '2025-07-22 21:35:23', '2025-07-22 21:35:23', 0);
INSERT INTO `sys_user`
VALUES (0x019832589E677DF49313B29C89BA5789, 'MVKKLITc', '$2a$10$Ya4loVD8bNaxej6tQCmhZukTaub1VGaT1j9Ka.7kHwrhm87VNFMge',
        'Jeffrey Toy Sr.', '', '', 0, '', 0, NULL, '2025-07-22 21:35:25', '2025-07-22 21:35:25', 0);
INSERT INTO `sys_user`
VALUES (0x01983258A4607641877A408382ED30B7, 'ROLgrrtO', '$2a$10$kTUxQ73xL5vst6XS5kbZQOejlnhm9XCqxAQaAesE0qHbIKjl7GASi',
        'Dong Bode', '', '', 0, '', 0, NULL, '2025-07-22 21:35:27', '2025-07-22 21:35:27', 0);
INSERT INTO `sys_user`
VALUES (0x01983258A9AC78468C5EDE44D0B55579, 'sj7HPuTJ', '$2a$10$XixuVa2zi92bONaQamZ.wulE6uKr9FhXk3tIVq5xWzZFeQiZtNoI.',
        'Maisie Funk', '', '', 0, '', 0, NULL, '2025-07-22 21:35:28', '2025-07-22 21:35:28', 0);
INSERT INTO `sys_user`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 'D0zRtuSU', '$2a$10$4fuCbb.vph8EkPxoovL2Letzy7dz7b6lEOCMv1FIpOhRAY9eUiov6',
        'Lesli Waelchi', '55@qq.com', '18039824444', 0, '', 0, '2025-10-07 11:32:40', '2025-07-22 21:35:30',
        '2025-10-07 11:32:41', 0);
INSERT INTO `sys_user`
VALUES (0x01983258FEC277DCBE9F4ADAEAD542B8, 'hptPg9QD', '$2a$10$BU.Bh4PXme4pQySgtcwOL.tvWOj4greW7NxkH4/yOb4/Q1J90F1wC',
        'Shenna Zemlak', '', '', 0, '', 0, NULL, '2025-07-22 21:35:50', '2025-07-22 21:35:50', 0);
INSERT INTO `sys_user`
VALUES (0x019832FE1D547DFFB95189F76527518B, 'GLuEoSC0', '$2a$10$t7SDho1WzVUK14x9d65rEetTAxH/OA53UZfKoL/1.sZAOC19L8DZq',
        'Winifred Beer IV', NULL, NULL, 0, NULL, 0, NULL, '2025-07-23 00:36:11', '2025-07-23 00:36:11', 0);
INSERT INTO `sys_user`
VALUES (0x019832FE2FEE7D4C885ECCA9FF295F41, 'HEi5uscz', '$2a$10$nKJzKTRPgaKf3.jY.prm4OckNCBvYKxHlPe5ydGHUzsQEQYGGUbE.',
        'Jerilyn Bergnaum', NULL, NULL, 0, NULL, 0, NULL, '2025-07-23 00:36:16', '2025-07-23 00:36:16', 0);
INSERT INTO `sys_user`
VALUES (0x019833057E727CC695DCF9D45E09519F, 'cgsrAU6C', '$2a$10$gTVQBro6oor5RtOaUb5/Ae9oNF7WalATfuNpYte8hhUMg6QceNIx.',
        'Marty Armstrong', NULL, NULL, 0, NULL, 0, NULL, '2025-07-23 00:44:15', '2025-07-23 00:44:15', 0);
INSERT INTO `sys_user`
VALUES (0x01983309C398703B9BD313A9A28FBF2B, 'I6wA2iyD', '$2a$10$CdfkWACYe.gKd4kTRUFud.ZcPNv6u1m10tEyba4W4veN813PiFTse',
        'Dr. Kevin Pfeffer', NULL, NULL, 0, NULL, 0, NULL, '2025-07-23 00:48:55', '2025-07-23 00:48:55', 0);
INSERT INTO `sys_user`
VALUES (0x01984F1405607DDFA38BCADE91CB6755, 'D0qQOFjF', '$2a$10$OuQL/f0AaJq.86a1ECr4f.RqtD7ZRJY4aQuu/78fag0vOTxJMoGf.',
        'Thomasine Botsford', NULL, NULL, 0, NULL, 0, '2025-10-07 11:31:03', '2025-07-28 11:29:29',
        '2025-10-07 11:31:03', 0);
INSERT INTO `sys_user`
VALUES (0x01992CB6D9A778419E2B9DCC1C9EA0A7, 'iRdU8XLh', '$2a$10$/yQRllia289yoyO5M2iWjuXhQFx93vqZlGOefH148aGeio7fdntxa',
        'Rudolph Smith', NULL, NULL, 0,
        'http://127.0.0.1:31589/sapientiacloud-edupivot/avatars/image_1757391802796.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20250909%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20250909T042322Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=c717a43659664876369081493e08c142628648d0afe94ad93442b85e2e81de11',
        0, '2025-10-07 11:19:55', '2025-09-09 12:23:25', '2025-10-07 11:19:56', 0);
INSERT INTO `sys_user`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 'yc0hKY14', '$2a$10$DMKDGN8rtQ4RrT/MElyYkerFygI0bK5e/Lf4e1MXBJdMBJc.kwDsS',
        'Terrence Cronin', NULL, NULL, 0, NULL, 0, '2025-10-08 22:15:35', '2025-09-09 12:24:04', '2025-10-08 22:15:35',
        0);
INSERT INTO `sys_user`
VALUES (0x01992E9F054C794EAB45B0CA4CA41C34, 'KoJZIxkh', '$2a$10$BPFTMXR8/DD1mo.Er6UQfe49VW6NjOZ7iZh4AU5jOnbkbFa1ROLla',
        'Layne Harris', NULL, NULL, 0, NULL, 0, NULL, '2025-09-09 21:16:38', '2025-09-09 21:16:38', 0);
INSERT INTO `sys_user`
VALUES (0x01992E9F1062772DB84BF7CDEB2D5734, 'XMxDvnZ5', '$2a$10$n0NSKT8YiV505pe4vzJp8eemJRgWDNomFiuQ09q2sabyO8UBBefKq',
        'Anamaria Greenfelder', NULL, NULL, 0, NULL, 0, NULL, '2025-09-09 21:16:40', '2025-09-09 21:16:40', 0);
INSERT INTO `sys_user`
VALUES (0x01992E9F8091738CA7BC757711E06C16, 'ykhwyEGB', '$2a$10$9wjpLSSAheC1xdZlZ.3o1uxXKqq4YzljPO5Ox/NNXNpYqHzuVx8Bq',
        'Cordell Schmitt', NULL, NULL, 0, NULL, 0, NULL, '2025-09-09 21:17:09', '2025-09-09 21:17:09', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `user_id` binary(16) NULL DEFAULT NULL,
    `role_id` binary(16) NULL DEFAULT NULL
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES (0x0197EE62BE087C57B1FF42B1FA3C8B3F, 0x01980D1F114073D58CF57D7FB7C2B53B);
INSERT INTO `sys_user_role`
VALUES (0x0198325889CE7AC0964506D6A39318A3, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x019832588FB77C0DADD523313E77AA92, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x0198325895D8703AB430530C1F74796A, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x019832589E677DF49313B29C89BA5789, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x01983258A4607641877A408382ED30B7, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x01983258A9AC78468C5EDE44D0B55579, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x01983258FEC277DCBE9F4ADAEAD542B8, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x019832FE1D547DFFB95189F76527518B, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x019832FE2FEE7D4C885ECCA9FF295F41, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x019833057E727CC695DCF9D45E09519F, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x01983309C398703B9BD313A9A28FBF2B, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x01983258847B7546B6D0FE06FE3B66FF, 0x0198590CC13A7706AB89025C6BD7AAFC);
INSERT INTO `sys_user_role`
VALUES (0x01983258AFB079B3847B0ABF6A991C88, 0x0198590CC13A7706AB89025C6BD7AAFC);
INSERT INTO `sys_user_role`
VALUES (0x01992CB6D9A778419E2B9DCC1C9EA0A7, 0x0198590CC13A7706AB89025C6BD7AAFC);
INSERT INTO `sys_user_role`
VALUES (0x01992CB7735D78579C8CEDBE566AD0D2, 0x0198590CC13A7706AB89025C6BD7AAFC);
INSERT INTO `sys_user_role`
VALUES (0x019831A219DA7A9CA8783926484143BF, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x0198319FAC4577FC80C79106DA57CAFF, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x0198086CFCD17A09B3C3537DE3493332, 0x0198590BF85F7B2FA5FE4CC0994F72D5);
INSERT INTO `sys_user_role`
VALUES (0x0197EE62BE087C57B1FF42B1FA3C8B3F, 0x0198590CC13A7706AB89025C6BD7AAFC);

SET FOREIGN_KEY_CHECKS = 1;
