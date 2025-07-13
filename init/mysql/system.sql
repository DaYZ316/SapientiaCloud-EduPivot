create table sys_user
(
    id              binary(16)                         not null comment '用户ID'
        primary key,
    username        varchar(255)                       null comment '用户名',
    password        varchar(255)                       null comment '密码',
    nick_name       varchar(255)                       null comment '用户昵称',
    email           varchar(255)                       null comment '邮箱',
    mobile          varchar(255)                       null comment '手机号',
    gender          int                                null comment '性别 (0=未知, 1=男, 2=女)',
    avatar          varchar(255)                       null comment '用户头像URL',
    status          int      default 0                 null comment '状态 (0=正常, 1=停用)',
    last_login_time datetime                           null comment '最后登录时间',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间 (系统自动生成)',
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间 (系统自动生成)',
    is_deleted      int      default 0                 null comment '逻辑删除标记 (内部使用)'
)
    comment '系统用户表';

