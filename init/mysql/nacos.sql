/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : nacos

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 16/10/2025 22:53:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_info
-- ----------------------------
DROP TABLE IF EXISTS `config_info`;
CREATE TABLE `config_info`
(
    `id`                 bigint                                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id`            varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
    `group_id`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    `content`            longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin     NOT NULL COMMENT 'content',
    `md5`                varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'md5',
    `gmt_create`         datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`       datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user`           text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL COMMENT 'source user',
    `src_ip`             varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'source ip',
    `app_name`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    `tenant_id`          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
    `c_desc`             varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    `c_use`              varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL,
    `effect`             varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL,
    `type`               varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL,
    `c_schema`           text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL,
    `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL COMMENT '秘钥',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_configinfo_datagrouptenant` (`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 16
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = 'config_info'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info
-- ----------------------------
INSERT INTO `config_info`
VALUES (1, 'application.yaml', 'DEFAULT_GROUP',
        'spring:\r\n  datasource:\r\n    driver-class-name: com.mysql.cj.jdbc.Driver\r\n    url: jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?rewriteBatchedStatements=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true\r\n    username: ${MYSQL_USERNAME}\r\n    password: ${MYSQL_PASSWORD}\r\n\r\n  data:\r\n    redis:\r\n      port: ${REDIS_PORT}\r\n      host: ${REDIS_HOST}\r\n      password: ${REDIS_PASSWORD}\r\n      timeout: 6000ms\r\n      lettuce:\r\n        pool:\r\n          max-active: 8\r\n          max-wait: -1ms\r\n          max-idle: 8\r\n          min-idle: 0\r\n\r\n  cache:\r\n    type: redis\r\n    redis:\r\n      time-to-live: 1800000\r\n      cache-null-values: true\r\n      use-key-prefix: true\r\n      key-prefix: SapientiaCloud-EduPivot\r\n    caffeine:\r\n      spec: maximumSize=500,expireAfterWrite=10m\r\n\r\nknife4j:\r\n  enable: true\r\n  basic:\r\n    enable: true\r\n    username: ${KNIFE4J_USERNAME}\r\n    password: ${KNIFE4J_PASSWORD}\r\n\r\nlogging:\r\n  level:\r\n    com.dayz.sapientiacloud_edupivot.*.mapper: DEBUG\r\n    org.springframework.http.server.reactive: DEBUG\r\n    org.springframework.web.reactive: DEBUG\r\n\r\n\r\nMYSQL_HOST: 127.0.0.1\r\nMYSQL_PORT: 3306\r\nMYSQL_USERNAME: root\r\nMYSQL_PASSWORD: zhaosheng123\r\nMYSQL_DATABASE: sapientiacloud_edupivot\r\nREDIS_HOST: 127.0.0.1\r\nREDIS_PORT: 6379\r\nREDIS_USERNAME:\r\nREDIS_PASSWORD: zhaosheng123\r\nKNIFE4J_USERNAME: admin\r\nKNIFE4J_PASSWORD: zhaosheng123',
        '905c5618e5bdda82660a1d9807000413', '2025-08-04 23:56:52', '2025-08-04 23:56:52', 'nacos', '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info`
VALUES (2, 'SapientiaCloud-EduPivot--auth.yaml', 'DEFAULT_GROUP',
        'feign:\r\n  client:\r\n    config:\r\n      default:\r\n        connectTimeout: 5000\r\n        readTimeout: 5000\r\n        loggerLevel: full\r\n\r\nspring:\r\n  jpa:\r\n    open-in-view: false',
        '0186b93d3bfbe0c2bdc55dd8cd270225', '2025-08-04 23:57:10', '2025-08-04 23:57:10', 'nacos', '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info`
VALUES (3, 'SapientiaCloud-EduPivot--system.yaml', 'DEFAULT_GROUP',
        'pagehelper:\r\n  helper-dialect: mysql\r\n  reasonable: true\r\n  support-methods-arguments: true\r\n  params: count=countSql\r\n\r\nmybatis-plus:\r\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.system.entity\r\n  mapper-locations: classpath:mapper/**/*.xml\r\n  configuration:\r\n    cache-enabled: false\r\n    map-underscore-to-camel-case: true\r\n  global-config:\r\n    db-config:\r\n      id-type: assign_id\r\n      update-strategy: not_null\r\n      logic-delete-field: deleted\r\n      logic-delete-value: 1\r\n      logic-not-delete-value: 0\r\n\r\nfeign:\r\n  client:\r\n    config:\r\n      default:\r\n        connectTimeout: 5000\r\n        readTimeout: 5000\r\n        loggerLevel: full\r\n  httpclient:\r\n    enabled: true\r\n\r\nspring:\r\n  jpa:\r\n    open-in-view: false\r\n    show-sql: true\r\n    hibernate:\r\n      ddl-auto: update',
        '63f71f1ecb3d3303b0f70ca818e225f8', '2025-08-05 00:14:29', '2025-08-05 00:14:29', 'nacos', '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info`
VALUES (4, 'SapientiaCloud-EduPivot--gateway.yaml', 'DEFAULT_GROUP',
        'spring:\n  cloud:\n    gateway:\n      routes:\n        - id: SapientiaCloud-EduPivot--system\n          uri: lb://SapientiaCloud-EduPivot--system\n          predicates:\n            - Path=/api/system/**\n          filters:\n            - StripPrefix=2\n        - id: SapientiaCloud-EduPivot--auth\n          uri: lb://SapientiaCloud-EduPivot--auth\n          predicates:\n            - Path=/api/auth/**\n          filters:\n            - StripPrefix=2\n        - id: SapientiaCloud-EduPivot--minIO\n          uri: lb://SapientiaCloud-EduPivot--minIO\n          predicates:\n            - Path=/api/minIO/**\n          filters:\n            - StripPrefix=2\n        - id: SapientiaCloud-EduPivot--teacher\n          uri: lb://SapientiaCloud-EduPivot--teacher\n          predicates:\n            - Path=/api/teacher/**\n          filters:\n            - StripPrefix=2\n        - id: SapientiaCloud-EduPivot--student\n          uri: lb://SapientiaCloud-EduPivot--student\n          predicates:\n            - Path=/api/student/**\n          filters:\n            - StripPrefix=2\n        - id: SapientiaCloud-EduPivot--course\n          uri: lb://SapientiaCloud-EduPivot--course\n          predicates:\n            - Path=/api/course/**\n          filters:\n            - StripPrefix=2\n\n  data:\n    redis:\n      host: ${REDIS_HOST}\n      port: ${REDIS_PORT}\n      password: ${REDIS_PASSWORD}\n      timeout: 6000ms\n      lettuce:\n        pool:\n          max-active: 8\n          max-wait: -1ms\n          max-idle: 8\n          min-idle: 0\n\nknife4j:\n  gateway:\n    enabled: true\n    strategy: discover\n    discover:\n      enabled: true\n      version: openapi3\n\ncache:\n  caffeine:\n    spec: maximumSize=500,expireAfterWrite=10m\n\nlogging:\n  level:\n    org.springframework.cloud.gateway: DEBUG\n\nREDIS_HOST: 127.0.0.1\nREDIS_PORT: 6379\nREDIS_USERNAME:\nREDIS_PASSWORD: zhaosheng123',
        '638fc3ca884122e94e44d910742b4916', '2025-08-05 00:22:43', '2025-08-05 00:27:49', 'nacos', '172.18.0.1', '', '',
        '', '', '', 'yaml', '', '');
INSERT INTO `config_info`
VALUES (5, 'SapientiaCloud-EduPivot--minIO.yaml', 'DEFAULT_GROUP',
        'minio:\r\n  config:\r\n    ip: ${MINIO_IP}\r\n    port: ${MINIO_PORT}\r\n    accessKey: ${MINIO_USERNAME}\r\n    secretKey: ${MINIO_PASSWORD}\r\n    bucketName: ${MINIO_BUCKET_NAME}\r\n\r\nspring:\r\n  servlet:\r\n    multipart:\r\n      enabled: true\r\n      max-file-size: 50MB\r\n      max-request-size: 100MB\r\n      file-size-threshold: 2MB\r\n\r\nlogging:\r\n  level:\r\n    com.dayz.sapientiacloud_edupivot.minio: DEBUG\r\n\r\n\r\nMINIO_IP: 127.0.0.1\r\nMINIO_PORT: 31589\r\nMINIO_USERNAME: root\r\nMINIO_PASSWORD: zhaosheng123\r\nMINIO_BUCKET_NAME: sapientiacloud-edupivot',
        'f531610c2d758e6c11923eebb6559354', '2025-08-05 00:23:12', '2025-08-05 00:23:12', NULL, '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info`
VALUES (6, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      host: ${MONGODB_HOST}\n      port: ${MONGODB_PORT}\n      database: ${MONGODB_DATABASE}\n      username: ${MONGODB_USERNAME}\n      password: ${MONGODB_PASSWORD}\n      authentication-database: ${MONGODB_AUTH_DATABASE}\n      type-mapper: none\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng123\nMONGODB_AUTH_DATABASE: admin',
        'c1ceed1b81e1ca5300d1f905614e27b7', '2025-08-05 00:23:35', '2025-09-27 11:54:34', 'nacos', '172.18.0.1', '', '',
        '', '', '', 'yaml', '', '');
INSERT INTO `config_info`
VALUES (7, 'SapientiaCloud-EduPivot--student.yaml', 'DEFAULT_GROUP',
        'pagehelper:\r\n  helper-dialect: mysql\r\n  reasonable: true\r\n  support-methods-arguments: true\r\n  params: count=countSql\r\n\r\nmybatis-plus:\r\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.student.entity\r\n  mapper-locations: classpath:mapper/**/*.xml\r\n  configuration:\r\n    cache-enabled: false\r\n    map-underscore-to-camel-case: true\r\n  global-config:\r\n    db-config:\r\n      id-type: assign_id\r\n      update-strategy: not_null\r\n      logic-delete-field: deleted\r\n      logic-delete-value: 1\r\n      logic-not-delete-value: 0\r\n\r\nfeign:\r\n  client:\r\n    config:\r\n      default:\r\n        connectTimeout: 5000\r\n        readTimeout: 5000\r\n        loggerLevel: full\r\n  httpclient:\r\n    enabled: true\r\n\r\nspring:\r\n  jpa:\r\n    open-in-view: false\r\n    show-sql: true\r\n    hibernate:\r\n      ddl-auto: update',
        'ca924e9ee68d99638b7090d450fcb1c4', '2025-08-05 00:24:20', '2025-08-05 00:24:20', NULL, '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');
INSERT INTO `config_info`
VALUES (8, 'SapientiaCloud-EduPivot--teacher.yaml', 'DEFAULT_GROUP',
        'pagehelper:\r\n  helper-dialect: mysql\r\n  reasonable: true\r\n  support-methods-arguments: true\r\n  params: count=countSql\r\n\r\nmybatis-plus:\r\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.teacher.entity\r\n  mapper-locations: classpath:mapper/**/*.xml\r\n  configuration:\r\n    cache-enabled: false\r\n    map-underscore-to-camel-case: true\r\n  global-config:\r\n    db-config:\r\n      id-type: assign_id\r\n      update-strategy: not_null\r\n      logic-delete-field: deleted\r\n      logic-delete-value: 1\r\n      logic-not-delete-value: 0\r\n\r\nfeign:\r\n  client:\r\n    config:\r\n      default:\r\n        connectTimeout: 5000\r\n        readTimeout: 5000\r\n        loggerLevel: full\r\n  httpclient:\r\n    enabled: true\r\n\r\nspring:\r\n  jpa:\r\n    open-in-view: false\r\n    show-sql: true\r\n    hibernate:\r\n      ddl-auto: update',
        'c426f80a9941ae3402503a299113cbb8', '2025-08-05 00:25:40', '2025-08-05 00:25:40', NULL, '172.18.0.1', '', '',
        NULL, NULL, NULL, 'yaml', NULL, '');

-- ----------------------------
-- Table structure for config_info_aggr
-- ----------------------------
DROP TABLE IF EXISTS `config_info_aggr`;
CREATE TABLE `config_info_aggr`
(
    `id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id`      varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
    `group_id`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
    `datum_id`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'datum_id',
    `content`      longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin     NOT NULL COMMENT '内容',
    `gmt_modified` datetime                                               NULL DEFAULT NULL COMMENT '修改时间',
    `app_name`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL,
    `tenant_id`    varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_configinfoaggr_datagrouptenantdatum` (`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `datum_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = '增加租户字段'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_aggr
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_beta
-- ----------------------------
DROP TABLE IF EXISTS `config_info_beta`;
CREATE TABLE `config_info_beta`
(
    `id`                 bigint                                                  NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id`            varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NOT NULL COMMENT 'data_id',
    `group_id`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NOT NULL COMMENT 'group_id',
    `app_name`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'app_name',
    `content`            longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin      NOT NULL COMMENT 'content',
    `beta_ips`           varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'betaIps',
    `md5`                varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin   NULL DEFAULT NULL COMMENT 'md5',
    `gmt_create`         datetime                                                NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`       datetime                                                NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user`           text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin          NULL COMMENT 'source user',
    `src_ip`             varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin   NULL DEFAULT NULL COMMENT 'source ip',
    `tenant_id`          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin          NULL COMMENT '秘钥',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_configinfobeta_datagrouptenant` (`data_id` ASC, `group_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = 'config_info_beta'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_beta
-- ----------------------------

-- ----------------------------
-- Table structure for config_info_tag
-- ----------------------------
DROP TABLE IF EXISTS `config_info_tag`;
CREATE TABLE `config_info_tag`
(
    `id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `data_id`      varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
    `group_id`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
    `tenant_id`    varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
    `tag_id`       varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_id',
    `app_name`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
    `content`      longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin     NOT NULL COMMENT 'content',
    `md5`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'md5',
    `gmt_create`   datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `src_user`     text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL COMMENT 'source user',
    `src_ip`       varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'source ip',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_configinfotag_datagrouptenanttag` (`data_id` ASC, `group_id` ASC, `tenant_id` ASC, `tag_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = 'config_info_tag'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_info_tag
-- ----------------------------

-- ----------------------------
-- Table structure for config_tags_relation
-- ----------------------------
DROP TABLE IF EXISTS `config_tags_relation`;
CREATE TABLE `config_tags_relation`
(
    `id`        bigint                                                 NOT NULL COMMENT 'id',
    `tag_name`  varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'tag_name',
    `tag_type`  varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'tag_type',
    `data_id`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'data_id',
    `group_id`  varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'group_id',
    `tenant_id` varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
    `nid`       bigint                                                 NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`nid`) USING BTREE,
    UNIQUE INDEX `uk_configtagrelation_configidtag` (`id` ASC, `tag_name` ASC, `tag_type` ASC) USING BTREE,
    INDEX `idx_tenant_id` (`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = 'config_tag_relation'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of config_tags_relation
-- ----------------------------

-- ----------------------------
-- Table structure for group_capacity
-- ----------------------------
DROP TABLE IF EXISTS `group_capacity`;
CREATE TABLE `group_capacity`
(
    `id`                bigint UNSIGNED                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `group_id`          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Group ID，空字符表示整个集群',
    `quota`             int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
    `usage`             int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '使用量',
    `max_size`          int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
    `max_aggr_count`    int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数，，0表示使用默认值',
    `max_aggr_size`     int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
    `max_history_count` int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
    `gmt_create`        datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`      datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_group_id` (`group_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = '集群、各Group容量信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for his_config_info
-- ----------------------------
DROP TABLE IF EXISTS `his_config_info`;
CREATE TABLE `his_config_info`
(
    `id`                 bigint UNSIGNED                                        NOT NULL,
    `nid`                bigint UNSIGNED                                        NOT NULL AUTO_INCREMENT,
    `data_id`            varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
    `group_id`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL,
    `app_name`           varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'app_name',
    `content`            longtext CHARACTER SET utf8mb3 COLLATE utf8mb3_bin     NOT NULL,
    `md5`                varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL,
    `gmt_create`         datetime                                               NULL DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`       datetime                                               NULL DEFAULT CURRENT_TIMESTAMP,
    `src_user`           text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL,
    `src_ip`             varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL,
    `op_type`            char(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin     NULL DEFAULT NULL,
    `tenant_id`          varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT '租户字段',
    `encrypted_data_key` text CHARACTER SET utf8mb3 COLLATE utf8mb3_bin         NULL COMMENT '秘钥',
    PRIMARY KEY (`nid`) USING BTREE,
    INDEX `idx_gmt_create` (`gmt_create` ASC) USING BTREE,
    INDEX `idx_gmt_modified` (`gmt_modified` ASC) USING BTREE,
    INDEX `idx_did` (`data_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 18
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = '多租户改造'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of his_config_info
-- ----------------------------
INSERT INTO `his_config_info`
VALUES (6, 12, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\r\n  helper-dialect: mysql\r\n  reasonable: true\r\n  support-methods-arguments: true\r\n  params: count=countSql\r\n\r\nmybatis-plus:\r\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\r\n  mapper-locations: classpath:mapper/**/*.xml\r\n  configuration:\r\n    cache-enabled: false\r\n    map-underscore-to-camel-case: true\r\n  global-config:\r\n    db-config:\r\n      id-type: assign_id\r\n      update-strategy: not_null\r\n      logic-delete-field: deleted\r\n      logic-delete-value: 1\r\n      logic-not-delete-value: 0\r\n\r\nfeign:\r\n  client:\r\n    config:\r\n      default:\r\n        connectTimeout: 5000\r\n        readTimeout: 5000\r\n        loggerLevel: full\r\n  httpclient:\r\n    enabled: true\r\n\r\nspring:\r\n  jpa:\r\n    open-in-view: false\r\n    show-sql: true\r\n    hibernate:\r\n      ddl-auto: update',
        '0300dfa307f15cd7d15857fc26fc470e', '2025-09-22 14:36:44', '2025-09-22 22:36:44', 'nacos', '172.18.0.1', 'U',
        '', '');
INSERT INTO `his_config_info`
VALUES (6, 13, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      host: ${MONGODB_HOST}\n      port: ${MONGODB_PORT}\n      database: ${MONGODB_DATABASE}\n      username: ${MONGODB_USERNAME}\n      password: ${MONGODB_PASSWORD}\n      authentication-database: ${MONGODB_AUTH_DATABASE}\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng123\nMONGODB_AUTH_DATABASE: admin',
        'cdcfccec82060a9a03999123e249576f', '2025-09-23 11:34:22', '2025-09-23 19:34:22', 'nacos', '172.18.0.1', 'U',
        '', '');
INSERT INTO `his_config_info`
VALUES (6, 14, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      uri: mongodb://root:zhaosheng123@localhost:27017/sapientiacloud_edupivot?authSource=admin\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng123\nMONGODB_AUTH_DATABASE: admin',
        'e74bf7274279617d8fd3069f4aa48e87', '2025-09-23 11:35:39', '2025-09-23 19:35:40', NULL, '172.18.0.1', 'U', '',
        '');
INSERT INTO `his_config_info`
VALUES (6, 15, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      host: ${MONGODB_HOST}\n      port: ${MONGODB_PORT}\n      database: ${MONGODB_DATABASE}\n      username: ${MONGODB_USERNAME}\n      password: ${MONGODB_PASSWORD}\n      authentication-database: ${MONGODB_AUTH_DATABASE}\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng123\nMONGODB_AUTH_DATABASE: admin',
        'cdcfccec82060a9a03999123e249576f', '2025-09-23 11:36:06', '2025-09-23 19:36:06', 'nacos', '172.18.0.1', 'U',
        '', '');
INSERT INTO `his_config_info`
VALUES (6, 16, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      host: ${MONGODB_HOST}\n      port: ${MONGODB_PORT}\n      database: ${MONGODB_DATABASE}\n      username: ${MONGODB_USERNAME}\n      password: ${MONGODB_PASSWORD}\n      authentication-database: ${MONGODB_AUTH_DATABASE}\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng12\nMONGODB_AUTH_DATABASE: admin',
        '2402ae5cbd5b6f5288aaaa23cf08cb46', '2025-09-23 11:36:39', '2025-09-23 19:36:40', 'nacos', '172.18.0.1', 'U',
        '', '');
INSERT INTO `his_config_info`
VALUES (6, 17, 'SapientiaCloud-EduPivot--course.yaml', 'DEFAULT_GROUP', '',
        'pagehelper:\n  helper-dialect: mysql\n  reasonable: true\n  support-methods-arguments: true\n  params: count=countSql\n\nmybatis-plus:\n  type-aliases-package: com.dayz.sapientiacloud_edupivot.course.entity\n  mapper-locations: classpath:mapper/**/*.xml\n  configuration:\n    cache-enabled: false\n    map-underscore-to-camel-case: true\n  global-config:\n    db-config:\n      id-type: assign_id\n      update-strategy: not_null\n      logic-delete-field: deleted\n      logic-delete-value: 1\n      logic-not-delete-value: 0\n\nfeign:\n  client:\n    config:\n      default:\n        connectTimeout: 5000\n        readTimeout: 5000\n        loggerLevel: full\n  httpclient:\n    enabled: true\n\nspring:\n  jpa:\n    open-in-view: false\n    show-sql: true\n    hibernate:\n      ddl-auto: update\n  data:\n    mongodb:\n      host: ${MONGODB_HOST}\n      port: ${MONGODB_PORT}\n      database: ${MONGODB_DATABASE}\n      username: ${MONGODB_USERNAME}\n      password: ${MONGODB_PASSWORD}\n      authentication-database: ${MONGODB_AUTH_DATABASE}\n      \n      \nMONGODB_HOST: localhost\nMONGODB_PORT: 27017\nMONGODB_DATABASE: sapientiacloud_edupivot\nMONGODB_USERNAME: root\nMONGODB_PASSWORD: zhaosheng123\nMONGODB_AUTH_DATABASE: admin',
        'cdcfccec82060a9a03999123e249576f', '2025-09-27 03:54:34', '2025-09-27 11:54:34', 'nacos', '172.18.0.1', 'U',
        '', '');

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`
(
    `role`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL,
    `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `action`   varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   NOT NULL,
    UNIQUE INDEX `uk_role_permission` (`role` ASC, `resource` ASC, `action` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of permissions
-- ----------------------------

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`
(
    `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `role`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    UNIQUE INDEX `idx_user_role` (`username` ASC, `role` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles`
VALUES ('nacos', 'ROLE_ADMIN');

-- ----------------------------
-- Table structure for tenant_capacity
-- ----------------------------
DROP TABLE IF EXISTS `tenant_capacity`;
CREATE TABLE `tenant_capacity`
(
    `id`                bigint UNSIGNED                                        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tenant_id`         varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'Tenant ID',
    `quota`             int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '配额，0表示使用默认值',
    `usage`             int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '使用量',
    `max_size`          int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '单个配置大小上限，单位为字节，0表示使用默认值',
    `max_aggr_count`    int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '聚合子配置最大个数',
    `max_aggr_size`     int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '单个聚合数据的子配置大小上限，单位为字节，0表示使用默认值',
    `max_history_count` int UNSIGNED                                           NOT NULL DEFAULT 0 COMMENT '最大变更历史数量',
    `gmt_create`        datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified`      datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_tenant_id` (`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = '租户容量信息表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tenant_capacity
-- ----------------------------

-- ----------------------------
-- Table structure for tenant_info
-- ----------------------------
DROP TABLE IF EXISTS `tenant_info`;
CREATE TABLE `tenant_info`
(
    `id`            bigint                                                 NOT NULL AUTO_INCREMENT COMMENT 'id',
    `kp`            varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT 'kp',
    `tenant_id`     varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_id',
    `tenant_name`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT '' COMMENT 'tenant_name',
    `tenant_desc`   varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT 'tenant_desc',
    `create_source` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin  NULL DEFAULT NULL COMMENT 'create_source',
    `gmt_create`    bigint                                                 NULL DEFAULT NULL COMMENT '创建时间',
    `gmt_modified`  bigint                                                 NULL DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_tenant_info_kptenantid` (`kp` ASC, `tenant_id` ASC) USING BTREE,
    INDEX `idx_tenant_id` (`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_bin COMMENT = 'tenant_info'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tenant_info
-- ----------------------------

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`
(
    `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL,
    `password` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `enabled`  tinyint(1)                                                    NOT NULL,
    PRIMARY KEY (`username`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users`
VALUES ('nacos', '$2a$10$k3Pb1RMscWhysKCYT0zMoONDKH1f3QKJEYf9zcjBFd/V73AH3G/7G', 1);

SET FOREIGN_KEY_CHECKS = 1;
