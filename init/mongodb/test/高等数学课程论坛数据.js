// 高等数学课程论坛测试数据
// 课程ID: 78d44b4a-becd-4f65-9461-f2dcdda03e01
// 用户ID: 01983258-afb0-79b3-847b-0abf6a991c88, 01992cb7-735d-7857-9c8c-edbe566ad0d2, 01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7

// 1. 创建课程论坛数据
db.mg_course_forum.insertMany([
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "forum_name": "高等数学讨论区",
        "description": "高等数学课程学习讨论区，同学们可以在这里交流学习心得、讨论问题",
        "forum_type": 0, // 讨论区
        "is_public": 0, // 仅课程成员
        "allow_anonymous": 0, // 不允许匿名
        "moderator_ids": ["01983258-afb0-79b3-847b-0abf6a991c88"],
        "post_count": 15,
        "reply_count": 45,
        "last_post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a01",
        "last_post_time": "2024-01-20T14:30:00.000Z",
        "sort_order": 1,
        "status": 0, // 正常
        "rules": "1. 请保持文明用语，尊重他人\n2. 发帖前请先搜索是否已有类似问题\n3. 问题描述要清晰，便于他人理解和回答\n4. 禁止发布与课程无关的内容",
        "tags": ["高等数学", "讨论", "学习交流"],
        "create_time": ISODate("2024-01-15T08:00:00.000Z"),
        "update_time": ISODate("2024-01-20T14:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03f02",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "forum_name": "高等数学问答区",
        "description": "专门用于提问和回答高等数学相关问题的区域",
        "forum_type": 1, // 问答区
        "is_public": 0, // 仅课程成员
        "allow_anonymous": 1, // 允许匿名
        "moderator_ids": ["01983258-afb0-79b3-847b-0abf6a991c88", "01992cb7-735d-7857-9c8c-edbe566ad0d2"],
        "post_count": 8,
        "reply_count": 23,
        "last_post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a08",
        "last_post_time": "2024-01-19T16:45:00.000Z",
        "sort_order": 2,
        "status": 0, // 正常
        "rules": "1. 提问时请详细描述问题背景和具体疑问\n2. 回答时请提供详细的解题步骤\n3. 对于好的回答，请及时采纳\n4. 鼓励大家互相帮助，共同进步",
        "tags": ["高等数学", "问答", "问题求助"],
        "create_time": ISODate("2024-01-15T08:30:00.000Z"),
        "update_time": ISODate("2024-01-19T16:45:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03f03",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "forum_name": "高等数学作业区",
        "description": "发布和讨论高等数学作业的区域",
        "forum_type": 2, // 作业区
        "is_public": 0, // 仅课程成员
        "allow_anonymous": 0, // 不允许匿名
        "moderator_ids": ["01983258-afb0-79b3-847b-0abf6a991c88"],
        "post_count": 5,
        "reply_count": 12,
        "last_post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a12",
        "last_post_time": "2024-01-18T10:20:00.000Z",
        "sort_order": 3,
        "status": 0, // 正常
        "rules": "1. 作业发布后请及时完成\n2. 可以讨论作业中的难点问题\n3. 禁止直接发布作业答案\n4. 鼓励分享解题思路和方法",
        "tags": ["高等数学", "作业", "练习"],
        "create_time": ISODate("2024-01-15T09:00:00.000Z"),
        "update_time": ISODate("2024-01-18T10:20:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03f04",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "forum_name": "课程公告区",
        "description": "发布课程相关通知和公告的区域",
        "forum_type": 3, // 公告区
        "is_public": 0, // 仅课程成员
        "allow_anonymous": 0, // 不允许匿名
        "moderator_ids": ["01983258-afb0-79b3-847b-0abf6a991c88"],
        "post_count": 3,
        "reply_count": 0,
        "last_post_id": "78d44b4a-becd-4f65-9461-f2dcdda03f15",
        "last_post_time": "2024-01-17T09:00:00.000Z",
        "sort_order": 0,
        "status": 0, // 正常
        "rules": "1. 仅教师和管理员可以发布公告\n2. 学生可以查看和回复公告\n3. 重要公告会置顶显示",
        "tags": ["高等数学", "公告", "通知"],
        "create_time": ISODate("2024-01-15T07:30:00.000Z"),
        "update_time": ISODate("2024-01-17T09:00:00.000Z"),
        "is_deleted": 0
    }
]);

// 2. 创建论坛帖子数据
db.forum_posts.insertMany([
    // 讨论区帖子
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03a01",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "title": "极限计算中的常见错误分析",
        "content": "在学习极限的过程中，我发现很多同学容易犯一些常见的错误。今天想和大家分享一下这些错误以及正确的解题方法。\n\n**常见错误1：直接代入法使用不当**\n很多同学在计算极限时，直接代入x的值，但忽略了某些情况下直接代入会导致分母为0的情况。\n\n**常见错误2：洛必达法则使用条件不满足**\n洛必达法则有严格的使用条件，必须是0/0型或∞/∞型的不定式，且分子分母都可导。\n\n**常见错误3：等价无穷小替换错误**\n在使用等价无穷小替换时，要注意替换的时机和条件。\n\n大家在学习过程中还遇到过哪些问题呢？欢迎分享！",
        "post_type": 0, // 普通帖子
        "is_anonymous": 0, // 实名
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_common_errors.pdf"
        ],
        "image_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/images/limit_error_example1.png",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/images/limit_error_example2.png"
        ],
        "tags": ["极限", "错误分析", "学习方法"],
        "view_count": 156,
        "like_count": 23,
        "reply_count": 8,
        "share_count": 5,
        "is_top": 0,
        "is_essence": 1, // 精华帖子
        "is_locked": 0,
        "last_reply_id": "78d44b4a-becd-4f65-9461-f2dcdda03b08",
        "last_reply_time": "2024-01-20T14:30:00.000Z",
        "last_reply_user_id": "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
        "status": 0, // 正常
        "chapter_id": "78d44b4a-becd-4f65-9461-f2dcdda03e02", // 第一章
        "create_time": ISODate("2024-01-16T10:00:00.000Z"),
        "update_time": ISODate("2024-01-20T14:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03a02",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
        "title": "导数与微分的几何意义理解",
        "content": "最近在学习导数与微分这一章，对几何意义有些困惑。导数的几何意义是函数在某点的切线斜率，这个我能理解。但是微分的几何意义是什么呢？\n\n从几何上看，微分dy表示的是什么呢？是切线的增量吗？还是其他什么？\n\n另外，在实际应用中，什么时候用导数，什么时候用微分呢？\n\n希望有同学能帮忙解释一下，最好能结合图形来说明。",
        "post_type": 0, // 普通帖子
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "tags": ["导数", "微分", "几何意义"],
        "view_count": 89,
        "like_count": 12,
        "reply_count": 5,
        "share_count": 2,
        "is_top": 0,
        "is_essence": 0,
        "is_locked": 0,
        "last_reply_id": "78d44b4a-becd-4f65-9461-f2dcdda03b12",
        "last_reply_time": "2024-01-19T15:20:00.000Z",
        "last_reply_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "status": 0, // 正常
        "chapter_id": "78d44b4a-becd-4f65-9461-f2dcdda03e05", // 第二章
        "create_time": ISODate("2024-01-17T14:30:00.000Z"),
        "update_time": ISODate("2024-01-19T15:20:00.000Z"),
        "is_deleted": 0
    },
    // 问答区帖子
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03a08",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f02",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "title": "如何计算这个极限？lim(x→0) (sin x - x) / x³",
        "content": "题目：计算极限 lim(x→0) (sin x - x) / x³\n\n我尝试了几种方法：\n1. 直接代入：得到0/0型\n2. 洛必达法则：分子分母求导后还是0/0型\n3. 泰勒展开：sin x = x - x³/6 + x⁵/120 - ...\n\n用泰勒展开：\nsin x - x = -x³/6 + x⁵/120 - ...\n所以原式 = lim(x→0) (-x³/6 + x⁵/120 - ...) / x³\n= lim(x→0) (-1/6 + x²/120 - ...)\n= -1/6\n\n请问这个解法对吗？还有其他方法吗？",
        "post_type": 0, // 普通帖子
        "is_anonymous": 0, // 实名
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_problem.pdf"
        ],
        "image_urls": [],
        "tags": ["极限", "洛必达法则", "泰勒展开"],
        "view_count": 234,
        "like_count": 18,
        "reply_count": 6,
        "share_count": 3,
        "is_top": 0,
        "is_essence": 0,
        "is_locked": 0,
        "last_reply_id": "78d44b4a-becd-4f65-9461-f2dcdda03b15",
        "last_reply_time": "2024-01-19T16:45:00.000Z",
        "last_reply_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "status": 0, // 正常
        "chapter_id": "78d44b4a-becd-4f65-9461-f2dcdda03e02", // 第一章
        "create_time": ISODate("2024-01-18T11:15:00.000Z"),
        "update_time": ISODate("2024-01-19T16:45:00.000Z"),
        "is_deleted": 0
    },
    // 作业区帖子
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03a12",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f03",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "title": "第一章作业：极限计算练习",
        "content": "**作业内容：**\n请完成以下极限计算题：\n\n1. lim(x→2) (x² - 4) / (x - 2)\n2. lim(x→0) (1 - cos x) / x²\n3. lim(x→∞) (2x² + 3x + 1) / (3x² - 2x + 5)\n4. lim(x→0) (e^x - 1) / x\n5. lim(x→1) (x³ - 1) / (x² - 1)\n\n**要求：**\n- 每题都要写出详细的解题过程\n- 使用多种方法验证答案\n- 下周一前提交\n\n**提交方式：**\n将作业拍照或扫描后上传到课程平台",
        "post_type": 0, // 普通帖子
        "is_anonymous": 0, // 实名
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/chapter1_homework.pdf"
        ],
        "image_urls": [],
        "tags": ["作业", "极限", "第一章"],
        "view_count": 145,
        "like_count": 8,
        "reply_count": 4,
        "share_count": 1,
        "is_top": 0,
        "is_essence": 0,
        "is_locked": 0,
        "last_reply_id": "78d44b4a-becd-4f65-9461-f2dcdda03b18",
        "last_reply_time": "2024-01-18T10:20:00.000Z",
        "last_reply_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "status": 0, // 正常
        "chapter_id": "78d44b4a-becd-4f65-9461-f2dcdda03e02", // 第一章
        "create_time": ISODate("2024-01-15T16:00:00.000Z"),
        "update_time": ISODate("2024-01-18T10:20:00.000Z"),
        "is_deleted": 0
    },
    // 公告区帖子
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03f15",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f04",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "title": "【重要通知】期中考试安排",
        "content": "**期中考试通知**\n\n**考试时间：** 2024年2月15日 上午9:00-11:00\n**考试地点：** 教学楼A101、A102、A103\n**考试范围：** 第一章至第三章（函数与极限、导数与微分、积分）\n\n**注意事项：**\n1. 请携带学生证和身份证\n2. 考试期间禁止使用手机和计算器\n3. 提前15分钟到达考场\n4. 考试形式为闭卷笔试\n\n**复习建议：**\n- 重点复习基本概念和定理\n- 多做练习题，特别是历年真题\n- 注意解题步骤的规范性\n\n如有疑问，请及时联系老师。祝大家考试顺利！",
        "post_type": 3, // 公告
        "is_anonymous": 0, // 实名
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/midterm_exam_schedule.pdf"
        ],
        "image_urls": [],
        "tags": ["通知", "期中考试", "重要"],
        "view_count": 298,
        "like_count": 15,
        "reply_count": 0,
        "share_count": 8,
        "is_top": 1, // 置顶
        "is_essence": 0,
        "is_locked": 0,
        "last_reply_id": null,
        "last_reply_time": null,
        "last_reply_user_id": null,
        "status": 0, // 正常
        "chapter_id": null,
        "create_time": ISODate("2024-01-17T09:00:00.000Z"),
        "update_time": ISODate("2024-01-17T09:00:00.000Z"),
        "is_deleted": 0
    }
]);

// 3. 创建论坛回复数据
db.forum_replies.insertMany([
    // 对帖子p01的回复
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b01",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a01",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "content": "总结得很好！我补充一点，在计算极限时还要注意左右极限是否相等，特别是分段函数和含有绝对值的函数。",
        "parent_reply_id": null,
        "reply_to_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "like_count": 5,
        "reply_count": 2,
        "is_accepted": 0,
        "floor_number": 1,
        "status": 0, // 正常
        "ip_address": "192.168.1.100",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-16T11:30:00.000Z"),
        "update_time": ISODate("2024-01-16T11:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b02",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a01",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
        "content": "老师说得对！我经常忘记考虑左右极限，导致答案错误。",
        "parent_reply_id": "78d44b4a-becd-4f65-9461-f2dcdda03b01",
        "reply_to_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "like_count": 2,
        "reply_count": 0,
        "is_accepted": 0,
        "floor_number": 2,
        "status": 0, // 正常
        "ip_address": "192.168.1.101",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-16T12:00:00.000Z"),
        "update_time": ISODate("2024-01-16T12:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b03",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a01",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "content": "还有一个常见错误是混淆了无穷大和无穷小，比如把lim(x→0) 1/x当作无穷小，实际上当x→0⁺时，1/x→+∞。",
        "parent_reply_id": null,
        "reply_to_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "like_count": 8,
        "reply_count": 1,
        "is_accepted": 0,
        "floor_number": 3,
        "status": 0, // 正常
        "ip_address": "192.168.1.102",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-16T14:15:00.000Z"),
        "update_time": ISODate("2024-01-16T14:15:00.000Z"),
        "is_deleted": 0
    },
    // 对帖子p02的回复
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b12",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a02",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f01",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "content": "很好的问题！微分的几何意义可以这样理解：\n\n**微分的几何意义：**\n微分dy表示的是函数y=f(x)在点x处的切线在x方向上的增量dx对应的y方向上的增量。\n\n**具体来说：**\n- 当x有一个很小的增量Δx时，函数值的变化Δy ≈ dy = f'(x)Δx\n- 从几何上看，dy就是切线在x方向移动Δx时，y方向的变化量\n- 而Δy是函数曲线在x方向移动Δx时，y方向的实际变化量\n\n**应用区别：**\n- 导数主要用于求切线斜率、判断单调性、求极值等\n- 微分主要用于近似计算、误差估计等\n\n建议你画个图来理解，这样会更直观！",
        "parent_reply_id": null,
        "reply_to_user_id": "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
        "is_anonymous": 0, // 实名
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/differential_geometry.pdf"
        ],
        "image_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/images/differential_geometry_diagram.png"
        ],
        "like_count": 15,
        "reply_count": 2,
        "is_accepted": 1, // 被采纳
        "floor_number": 1,
        "status": 0, // 正常
        "ip_address": "192.168.1.100",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-19T15:20:00.000Z"),
        "update_time": ISODate("2024-01-19T15:20:00.000Z"),
        "is_deleted": 0
    },
    // 对帖子p08的回复
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b15",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a08",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f02",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "content": "你的解法完全正确！泰勒展开是解决这类问题的标准方法。\n\n**验证过程：**\n使用洛必达法则也可以得到相同结果：\n\n第一次洛必达：\nlim(x→0) (cos x - 1) / (3x²) = 0/0\n\n第二次洛必达：\nlim(x→0) (-sin x) / (6x) = 0/0\n\n第三次洛必达：\nlim(x→0) (-cos x) / 6 = -1/6\n\n**其他方法：**\n还可以使用等价无穷小：\nsin x - x = -x³/6 + o(x³)\n所以原式 = lim(x→0) (-x³/6 + o(x³)) / x³ = -1/6\n\n你的泰勒展开方法是最直观的，很好！",
        "parent_reply_id": null,
        "reply_to_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "like_count": 12,
        "reply_count": 1,
        "is_accepted": 1, // 被采纳
        "floor_number": 1,
        "status": 0, // 正常
        "ip_address": "192.168.1.100",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-19T16:45:00.000Z"),
        "update_time": ISODate("2024-01-19T16:45:00.000Z"),
        "is_deleted": 0
    },
    // 对帖子p12的回复
    {
        "_id": "78d44b4a-becd-4f65-9461-f2dcdda03b18",
        "post_id": "78d44b4a-becd-4f65-9461-f2dcdda03a12",
        "forum_id": "78d44b4a-becd-4f65-9461-f2dcdda03f03",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "01992cb7-735d-7857-9c8c-edbe566ad0d2",
        "content": "老师，第2题我用了洛必达法则，但是分子分母求导后还是0/0型，应该怎么处理？",
        "parent_reply_id": null,
        "reply_to_user_id": "01983258-afb0-79b3-847b-0abf6a991c88",
        "is_anonymous": 0, // 实名
        "attachment_urls": [],
        "image_urls": [],
        "like_count": 3,
        "reply_count": 1,
        "is_accepted": 0,
        "floor_number": 1,
        "status": 0, // 正常
        "ip_address": "192.168.1.102",
        "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
        "create_time": ISODate("2024-01-18T10:20:00.000Z"),
        "update_time": ISODate("2024-01-18T10:20:00.000Z"),
        "is_deleted": 0
    }
]);

print("高等数学课程论坛测试数据创建完成！");
print("创建了4个论坛：讨论区、问答区、作业区、公告区");
print("创建了5个帖子：包含讨论、问答、作业、公告等不同类型");
print("创建了6个回复：包含对帖子的回复和回复的回复");