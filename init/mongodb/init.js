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
db.createCollection('mg_course_chapter');
db.course_chapters.createIndex({"course_id": 1});
db.course_chapters.createIndex({"chapter_number": 1});
db.course_chapters.createIndex({"parent_chapter_id": 1});
db.course_chapters.createIndex({"status": 1});
db.course_chapters.createIndex({"create_time": -1});

// 课程论坛集合
db.createCollection('mg_course_forum');
db.course_forums.createIndex({"course_id": 1});
db.course_forums.createIndex({"forum_type": 1});
db.course_forums.createIndex({"status": 1});
db.course_forums.createIndex({"sort_order": 1});

// 论坛帖子集合
db.createCollection('mg_forum_post');
db.forum_posts.createIndex({"forum_id": 1});
db.forum_posts.createIndex({"course_id": 1});
db.forum_posts.createIndex({"author_id": 1});
db.forum_posts.createIndex({"post_type": 1});
db.forum_posts.createIndex({"is_top": 1, "create_time": -1});
db.forum_posts.createIndex({"status": 1});
db.forum_posts.createIndex({"create_time": -1});
db.forum_posts.createIndex({"tags": 1});

// 论坛回复集合
db.createCollection('mg_forum_reply');
db.forum_replies.createIndex({"post_id": 1});
db.forum_replies.createIndex({"forum_id": 1});
db.forum_replies.createIndex({"course_id": 1});
db.forum_replies.createIndex({"author_id": 1});
db.forum_replies.createIndex({"parent_reply_id": 1});
db.forum_replies.createIndex({"status": 1});
db.forum_replies.createIndex({"create_time": -1});
db.forum_replies.createIndex({"floor_number": 1});

// 课程任务集合
db.createCollection('mg_course_task');
db.mg_course_task.createIndex({"course_id": 1});
db.mg_course_task.createIndex({"sysUserId_id": 1});
db.mg_course_task.createIndex({"task_type": 1});
db.mg_course_task.createIndex({"status": 1});
db.mg_course_task.createIndex({"start_time": 1});
db.mg_course_task.createIndex({"end_time": 1});
db.mg_course_task.createIndex({"difficulty": 1});
db.mg_course_task.createIndex({"tags": 1});
db.mg_course_task.createIndex({"create_time": -1});
db.mg_course_task.createIndex({"view_count": -1});

// 课程题库集合
db.createCollection('mg_course_question_bank');
db.mg_course_question_bank.createIndex({"course_id": 1});
db.mg_course_question_bank.createIndex({"sys_user_id": 1});
db.mg_course_question_bank.createIndex({"bank_type": 1});
db.mg_course_question_bank.createIndex({"difficulty": 1});
db.mg_course_question_bank.createIndex({"is_public": 1});
db.mg_course_question_bank.createIndex({"tags": 1});
db.mg_course_question_bank.createIndex({"create_time": -1});

// 题目集合
db.createCollection('mg_question');
db.mg_question.createIndex({"question_bank_id": 1});
db.mg_question.createIndex({"sys_user_id": 1});
db.mg_question.createIndex({"question_type": 1});
db.mg_question.createIndex({"difficulty": 1});
db.mg_question.createIndex({"status": 1});
db.mg_question.createIndex({"tags": 1});
db.mg_question.createIndex({"create_time": -1});
db.mg_question.createIndex({"view_count": -1});
db.mg_question.createIndex({"score": 1});

// 题目选项集合
db.createCollection('mg_question_option');
db.mg_question_option.createIndex({"question_id": 1});
db.mg_question_option.createIndex({"is_correct": 1});
db.mg_question_option.createIndex({"option_label": 1});
db.mg_question_option.createIndex({"create_time": -1});

// 题目答案集合
db.createCollection('mg_question_answer');
db.mg_question_answer.createIndex({"question_id": 1});
db.mg_question_answer.createIndex({"sys_user_id": 1});
db.mg_question_answer.createIndex({"is_correct": 1});
db.mg_question_answer.createIndex({"score": 1});
db.mg_question_answer.createIndex({"create_time": -1});
