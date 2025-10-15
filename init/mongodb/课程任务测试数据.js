db.mg_course_task.insertMany([
    // 第一章 函数与极限作业
    {
        "_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "task_name": "高等数学第一章作业 - 函数与极限",
        "description": "完成第一章函数与极限的相关练习题，包括函数概念、极限定义、极限运算法则等内容。",
        "task_type": 0, // 0=作业
        "task_content": "<h3>第一章 函数与极限</h3><p>请完成以下题目：</p><ol><li><strong>函数概念题</strong><br/>求函数 f(x) = √(x²-4) 的定义域和值域</li><li><strong>极限计算题</strong><br/>计算下列极限：<br/>a) lim(x→0) sin(x)/x<br/>b) lim(x→∞) (x²+1)/(2x²-3x+1)</li><li><strong>极限证明题</strong><br/>用ε-δ定义证明 lim(x→2) (3x-1) = 5</li><li><strong>连续性问题</strong><br/>讨论函数 f(x) = {x², x≤1; 2x-1, x>1} 在 x=1 处的连续性</li></ol>",
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/calculus_chapter1_homework.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_examples.docx"
        ],
        "resource_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/calculus_textbook_chapter1.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/limit_concepts_video.mp4",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/practice_problems.pdf"
        ],
        "max_score": 100.00,
        "start_time": ISODate("2024-01-15T08:00:00.000Z"),
        "end_time": ISODate("2024-01-22T23:59:59.000Z"),
        "allow_late_submit": 1,
        "max_submit_count": 3,
        "auto_grade": 0, // 手动评分
        "tags": ["高等数学", "函数", "极限", "作业", "第一章"],
        "difficulty": 2, // 中等
        "estimated_time": 120, // 2小时
        "view_count": 0,
        "status": 1, // 发布
        "create_time": ISODate("2024-01-10T10:00:00.000Z"),
        "update_time": ISODate("2024-01-10T10:00:00.000Z"),
        "is_deleted": 0
    },

    // 第二章 导数与微分测验
    {
        "_id": "550e8400-e29b-41d4-a716-446655440001",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "task_name": "导数与微分在线测验",
        "description": "测试学生对导数概念、求导法则和微分应用的掌握程度。",
        "task_type": 1, // 1=测验
        "task_content": "<h3>导数与微分测验</h3><p><strong>时间限制：60分钟</strong></p><p>请回答以下选择题和计算题：</p><ol><li>函数 f(x) = x³ 的导数是？<br/>A) 3x² B) x² C) 3x D) x³</li><li>求函数 f(x) = sin(x)cos(x) 的导数</li><li>求函数 f(x) = ln(x²+1) 的导数</li><li>求函数 f(x) = e^(2x) 的二阶导数</li><li>求函数 f(x) = x²+3x+2 在 x=1 处的微分</li></ol>",
        "attachment_urls": [],
        "resource_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/derivative_rules.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/chain_rule_examples.mp4"
        ],
        "max_score": 50.00,
        "start_time": ISODate("2024-01-25T09:00:00.000Z"),
        "end_time": ISODate("2024-01-25T10:00:00.000Z"),
        "allow_late_submit": 0,
        "max_submit_count": 1,
        "auto_grade": 1, // 自动评分
        "tags": ["高等数学", "导数", "微分", "测验"],
        "difficulty": 2, // 中等
        "estimated_time": 60, // 1小时
        "view_count": 0,
        "status": 1, // 发布
        "create_time": ISODate("2024-01-20T14:30:00.000Z"),
        "update_time": ISODate("2024-01-20T14:30:00.000Z"),
        "is_deleted": 0
    },

    // 第四章 积分应用数学建模项目
    {
        "_id": "550e8400-e29b-41d4-a716-446655440002",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "task_name": "积分应用数学建模项目",
        "description": "运用积分知识解决实际问题的数学建模项目，培养数学应用能力。",
        "task_type": 2, // 2=项目
        "task_content": "<h3>积分应用数学建模项目</h3><p><strong>项目要求：</strong></p><ol><li><strong>问题选择</strong><br/>从以下主题中选择一个进行建模：<br/>• 人口增长模型<br/>• 经济增长模型<br/>• 物理运动问题<br/>• 工程优化问题</li><li><strong>模型建立</strong><br/>• 建立微分方程模型<br/>• 运用积分方法求解<br/>• 分析模型参数</li><li><strong>结果分析</strong><br/>• 数值计算和图形展示<br/>• 模型验证和误差分析<br/>• 实际意义解释</li><li><strong>报告撰写</strong><br/>• 完整的数学推导过程<br/>• 清晰的图表和计算<br/>• 结论和建议</li></ol>",
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/modeling_guidelines.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integration_examples.zip",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/report_template.docx"
        ],
        "resource_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/mathematical_modeling.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/integration_applications.mp4",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/matlab_tutorial.pdf"
        ],
        "max_score": 150.00,
        "start_time": ISODate("2024-02-01T08:00:00.000Z"),
        "end_time": ISODate("2024-03-15T23:59:59.000Z"),
        "allow_late_submit": 1,
        "max_submit_count": 2,
        "auto_grade": 0, // 手动评分
        "tags": ["高等数学", "积分", "数学建模", "项目", "应用"],
        "difficulty": 3, // 困难
        "estimated_time": 1440, // 24小时
        "view_count": 0,
        "status": 1, // 发布
        "create_time": ISODate("2024-01-25T16:00:00.000Z"),
        "update_time": ISODate("2024-01-25T16:00:00.000Z"),
        "is_deleted": 0
    },

    // 第六章 多元函数微分学实验
    {
        "_id": "550e8400-e29b-41d4-a716-446655440003",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "task_name": "多元函数微分学实验",
        "description": "通过计算机实验探索多元函数的偏导数、全微分和极值问题。",
        "task_type": 3, // 3=实验
        "task_content": "<h3>多元函数微分学实验</h3><p><strong>实验目标：</strong>通过MATLAB或Python编程，研究多元函数的微分性质</p><p><strong>实验内容：</strong></p><ol><li><strong>偏导数计算</strong><br/>• 计算函数 f(x,y) = x²y + xy² 的一阶和二阶偏导数<br/>• 验证混合偏导数相等性</li><li><strong>全微分研究</strong><br/>• 计算函数 f(x,y) = sin(xy) 的全微分<br/>• 分析全微分的几何意义</li><li><strong>极值问题</strong><br/>• 求函数 f(x,y) = x³ + y³ - 3xy 的极值点<br/>• 绘制函数图形和等高线图</li><li><strong>条件极值</strong><br/>• 在约束条件 x² + y² = 1 下求 f(x,y) = xy 的极值<br/>• 使用拉格朗日乘数法</li></ol><p><strong>实验报告要求：</strong></p><ul><li>完整的代码和运行结果</li><li>数学推导过程</li><li>图形分析和解释</li><li>实验心得和思考</li></ul>",
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/matlab_scripts.zip",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/experiment_template.m"
        ],
        "resource_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/multivariable_calculus.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/matlab_tutorial.mp4",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/3d_plotting_guide.pdf"
        ],
        "max_score": 80.00,
        "start_time": ISODate("2024-02-10T08:00:00.000Z"),
        "end_time": ISODate("2024-02-17T23:59:59.000Z"),
        "allow_late_submit": 1,
        "max_submit_count": 2,
        "auto_grade": 0, // 手动评分
        "tags": ["高等数学", "多元函数", "偏导数", "实验", "MATLAB"],
        "difficulty": 2, // 中等
        "estimated_time": 180, // 3小时
        "view_count": 0,
        "status": 1, // 发布
        "create_time": ISODate("2024-02-05T11:20:00.000Z"),
        "update_time": ISODate("2024-02-05T11:20:00.000Z"),
        "is_deleted": 0
    },

    // 第九章 级数收敛性分析作业
    {
        "_id": "550e8400-e29b-41d4-a716-446655440004",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "sys_user_id": "0198086c-fcd1-7a09-b3c3-537de3493332",
        "task_name": "级数收敛性分析作业",
        "description": "分析各种级数的收敛性，掌握级数收敛的判别方法。",
        "task_type": 0, // 0=作业
        "task_content": "<h3>级数收敛性分析</h3><p>请判断下列级数的收敛性，并说明理由：</p><ol><li>∑(n=1 to ∞) 1/n²</li><li>∑(n=1 to ∞) (-1)ⁿ/n</li><li>∑(n=1 to ∞) n!/nⁿ</li><li>∑(n=1 to ∞) (2n)!/(n!)²</li><li>∑(n=1 to ∞) sin(n)/n²</li></ol><p><strong>要求：</strong></p><ul><li>使用适当的收敛判别法</li><li>详细说明判断过程</li><li>如果收敛，求其和（如果可能）</li><li>如果发散，说明发散类型</li></ul>",
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/series_convergence_guide.pdf"
        ],
        "resource_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/series_convergence.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/convergence_tests.mp4"
        ],
        "max_score": 100.00,
        "start_time": ISODate("2024-02-20T08:00:00.000Z"),
        "end_time": ISODate("2024-02-27T23:59:59.000Z"),
        "allow_late_submit": 1,
        "max_submit_count": 3,
        "auto_grade": 0, // 手动评分
        "tags": ["高等数学", "级数", "收敛性", "作业"],
        "difficulty": 3, // 困难
        "estimated_time": 150, // 2.5小时
        "view_count": 0,
        "status": 0, // 草稿
        "create_time": ISODate("2024-02-15T09:45:00.000Z"),
        "update_time": ISODate("2024-02-15T09:45:00.000Z"),
        "is_deleted": 0
    }
]);
