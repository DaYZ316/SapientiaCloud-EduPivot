// 课程题库测试数据
db.mg_course_question_bank.insertMany([
    {
        "_id": "550e8400-e29b-41d4-a716-446655440001",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "bank_name": "高等数学基础题库",
        "description": "高等数学基础概念和计算题目的综合题库，涵盖函数、极限、导数等基础内容。",
        "bank_type": 0, // 0=基础题库
        "difficulty": 1, // 简单
        "is_public": 1, // 公开
        "tags": ["高等数学", "基础", "函数", "极限"],
        "create_time": ISODate("2024-01-10T10:00:00.000Z"),
        "update_time": ISODate("2024-01-10T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440002",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "bank_name": "微积分进阶题库",
        "description": "微积分进阶题目集合，包含积分、微分方程、级数等高级内容。",
        "bank_type": 1, // 1=进阶题库
        "difficulty": 3, // 困难
        "is_public": 1, // 公开
        "tags": ["微积分", "积分", "微分方程", "级数"],
        "create_time": ISODate("2024-01-15T14:30:00.000Z"),
        "update_time": ISODate("2024-01-15T14:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440003",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "bank_name": "期末考试题库",
        "description": "期末考试专用题库，综合各章节重点难点题目。",
        "bank_type": 2, // 2=考试题库
        "difficulty": 2, // 中等
        "is_public": 0, // 私有
        "tags": ["期末考试", "综合", "重点"],
        "create_time": ISODate("2024-01-20T09:15:00.000Z"),
        "update_time": ISODate("2024-01-20T09:15:00.000Z"),
        "is_deleted": 0
    }
]);

// 题目测试数据
db.mg_question.insertMany([
    // 基础题库题目
    {
        "_id": "550e8400-e29b-41d4-a716-446655440101",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440001",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "函数定义域计算",
        "question_content": "求函数 f(x) = √(x²-4) 的定义域。",
        "question_type": 0, // 0=选择题
        "difficulty": 1, // 简单
        "score": 5.0,
        "status": 1, // 发布
        "tags": ["函数", "定义域", "根号函数"],
        "view_count": 0,
        "create_time": ISODate("2024-01-10T10:30:00.000Z"),
        "update_time": ISODate("2024-01-10T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440102",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440001",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "极限计算",
        "question_content": "计算极限 lim(x→0) sin(x)/x",
        "question_type": 1, // 1=填空题
        "difficulty": 2, // 中等
        "score": 8.0,
        "status": 1, // 发布
        "tags": ["极限", "三角函数", "重要极限"],
        "view_count": 0,
        "create_time": ISODate("2024-01-10T11:00:00.000Z"),
        "update_time": ISODate("2024-01-10T11:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440103",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440001",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "导数计算",
        "question_content": "求函数 f(x) = x³ + 2x² - 5x + 3 的导数。",
        "question_type": 2, // 2=计算题
        "difficulty": 1, // 简单
        "score": 6.0,
        "status": 1, // 发布
        "tags": ["导数", "多项式", "基本求导"],
        "view_count": 0,
        "create_time": ISODate("2024-01-10T11:30:00.000Z"),
        "update_time": ISODate("2024-01-10T11:30:00.000Z"),
        "is_deleted": 0
    },
    // 进阶题库题目
    {
        "_id": "550e8400-e29b-41d4-a716-446655440104",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440002",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "不定积分计算",
        "question_content": "计算不定积分 ∫(x² + 3x + 2)dx",
        "question_type": 2, // 2=计算题
        "difficulty": 2, // 中等
        "score": 10.0,
        "status": 1, // 发布
        "tags": ["积分", "不定积分", "多项式积分"],
        "view_count": 0,
        "create_time": ISODate("2024-01-15T15:00:00.000Z"),
        "update_time": ISODate("2024-01-15T15:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440105",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440002",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "级数收敛性判断",
        "question_content": "判断级数 ∑(n=1 to ∞) 1/n² 的收敛性。",
        "question_type": 2, // 2=计算题
        "difficulty": 3, // 困难
        "score": 12.0,
        "status": 1, // 发布
        "tags": ["级数", "收敛性", "p级数"],
        "view_count": 0,
        "create_time": ISODate("2024-01-15T15:30:00.000Z"),
        "update_time": ISODate("2024-01-15T15:30:00.000Z"),
        "is_deleted": 0
    },
    // 考试题库题目
    {
        "_id": "550e8400-e29b-41d4-a716-446655440106",
        "question_bank_id": "550e8400-e29b-41d4-a716-446655440003",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "question_title": "综合应用题",
        "question_content": "已知函数 f(x) = x³ - 3x + 1，求：(1) 函数的单调区间；(2) 函数的极值；(3) 函数在区间[-2,2]上的最大值和最小值。",
        "question_type": 2, // 2=计算题
        "difficulty": 3, // 困难
        "score": 20.0,
        "status": 1, // 发布
        "tags": ["综合应用", "单调性", "极值", "最值"],
        "view_count": 0,
        "create_time": ISODate("2024-01-20T10:00:00.000Z"),
        "update_time": ISODate("2024-01-20T10:00:00.000Z"),
        "is_deleted": 0
    }
]);

// 题目选项测试数据
db.mg_question_option.insertMany([
    // 选择题选项
    {
        "_id": "550e8400-e29b-41d4-a716-446655440201",
        "question_id": "550e8400-e29b-41d4-a716-446655440101",
        "option_content": "x ∈ (-∞, -2] ∪ [2, +∞)",
        "option_label": "A",
        "is_correct": 1, // 正确选项
        "create_time": ISODate("2024-01-10T10:35:00.000Z"),
        "update_time": ISODate("2024-01-10T10:35:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440202",
        "question_id": "550e8400-e29b-41d4-a716-446655440101",
        "option_content": "x ∈ [-2, 2]",
        "option_label": "B",
        "is_correct": 0, // 错误选项
        "create_time": ISODate("2024-01-10T10:35:00.000Z"),
        "update_time": ISODate("2024-01-10T10:35:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440203",
        "question_id": "550e8400-e29b-41d4-a716-446655440101",
        "option_content": "x ∈ (-∞, -2) ∪ (2, +∞)",
        "option_label": "C",
        "is_correct": 0, // 错误选项
        "create_time": ISODate("2024-01-10T10:35:00.000Z"),
        "update_time": ISODate("2024-01-10T10:35:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440204",
        "question_id": "550e8400-e29b-41d4-a716-446655440101",
        "option_content": "x ∈ R",
        "option_label": "D",
        "is_correct": 0, // 错误选项
        "create_time": ISODate("2024-01-10T10:35:00.000Z"),
        "update_time": ISODate("2024-01-10T10:35:00.000Z"),
        "is_deleted": 0
    }
]);

// 题目答案测试数据
db.mg_question_answer.insertMany([
    {
        "_id": "550e8400-e29b-41d4-a716-446655440301",
        "question_id": "550e8400-e29b-41d4-a716-446655440101",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "A",
        "is_correct": 1, // 正确
        "score": 5.0,
        "create_time": ISODate("2024-01-12T14:30:00.000Z"),
        "update_time": ISODate("2024-01-12T14:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440302",
        "question_id": "550e8400-e29b-41d4-a716-446655440102",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "1",
        "is_correct": 1, // 正确
        "score": 8.0,
        "create_time": ISODate("2024-01-12T15:00:00.000Z"),
        "update_time": ISODate("2024-01-12T15:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440303",
        "question_id": "550e8400-e29b-41d4-a716-446655440103",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "f'(x) = 3x² + 4x - 5",
        "is_correct": 1, // 正确
        "score": 6.0,
        "create_time": ISODate("2024-01-12T15:30:00.000Z"),
        "update_time": ISODate("2024-01-12T15:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440304",
        "question_id": "550e8400-e29b-41d4-a716-446655440104",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "∫(x² + 3x + 2)dx = x³/3 + 3x²/2 + 2x + C",
        "is_correct": 1, // 正确
        "score": 10.0,
        "create_time": ISODate("2024-01-18T16:00:00.000Z"),
        "update_time": ISODate("2024-01-18T16:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440305",
        "question_id": "550e8400-e29b-41d4-a716-446655440105",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "该级数收敛，因为它是p级数，p=2>1，所以收敛。",
        "is_correct": 1, // 正确
        "score": 12.0,
        "create_time": ISODate("2024-01-18T16:30:00.000Z"),
        "update_time": ISODate("2024-01-18T16:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "550e8400-e29b-41d4-a716-446655440306",
        "question_id": "550e8400-e29b-41d4-a716-446655440106",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "answer_content": "(1) 单调递增区间：(-∞,-1)∪(1,+∞)，单调递减区间：(-1,1)；(2) 极大值：f(-1)=3，极小值：f(1)=-1；(3) 最大值：f(-2)=3，最小值：f(1)=-1",
        "is_correct": 1, // 正确
        "score": 20.0,
        "create_time": ISODate("2024-01-22T10:00:00.000Z"),
        "update_time": ISODate("2024-01-22T10:00:00.000Z"),
        "is_deleted": 0
    }
]);
