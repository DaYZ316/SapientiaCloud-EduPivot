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
    status          int      default 0                 null,
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