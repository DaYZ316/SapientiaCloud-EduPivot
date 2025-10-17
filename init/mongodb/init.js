// MongoDB 初始化脚本
// 创建数据库和用户

// 切换到目标数据库
db = db.getSiblingDB('sapientiacloud_edupivot');

// 创建应用用户
db.createUser({
    user: 'root',
    pwd: 'zhaosheng123',
    roles: [
        {
            role: 'readWrite',
            db: 'sapientiacloud_edupivot'
        }
    ]
});