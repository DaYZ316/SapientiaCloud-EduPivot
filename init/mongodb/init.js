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

// 创建集合和索引
// 课程章节集合
db.createCollection('course_chapters');
db.course_chapters.createIndex({ "course_id": 1 });
db.course_chapters.createIndex({ "chapter_number": 1 });
db.course_chapters.createIndex({ "parent_chapter_id": 1 });
db.course_chapters.createIndex({ "status": 1 });
db.course_chapters.createIndex({ "create_time": -1 });

// 课程论坛集合
db.createCollection('course_forums');
db.course_forums.createIndex({ "course_id": 1 });
db.course_forums.createIndex({ "forum_type": 1 });
db.course_forums.createIndex({ "status": 1 });
db.course_forums.createIndex({ "sort_order": 1 });

// 论坛帖子集合
db.createCollection('forum_posts');
db.forum_posts.createIndex({ "forum_id": 1 });
db.forum_posts.createIndex({ "course_id": 1 });
db.forum_posts.createIndex({ "author_id": 1 });
db.forum_posts.createIndex({ "post_type": 1 });
db.forum_posts.createIndex({ "is_top": 1, "create_time": -1 });
db.forum_posts.createIndex({ "status": 1 });
db.forum_posts.createIndex({ "create_time": -1 });
db.forum_posts.createIndex({ "tags": 1 });

// 论坛回复集合
db.createCollection('forum_replies');
db.forum_replies.createIndex({ "post_id": 1 });
db.forum_replies.createIndex({ "forum_id": 1 });
db.forum_replies.createIndex({ "course_id": 1 });
db.forum_replies.createIndex({ "author_id": 1 });
db.forum_replies.createIndex({ "parent_reply_id": 1 });
db.forum_replies.createIndex({ "status": 1 });
db.forum_replies.createIndex({ "create_time": -1 });
db.forum_replies.createIndex({ "floor_number": 1 });
