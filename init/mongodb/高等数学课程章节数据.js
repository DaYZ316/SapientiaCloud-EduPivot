db.course_chapters.insertMany([
    // 第一章 函数与极限
    {
        "_id": "19989528049-1234-7de4-8000-e8d8f58b5",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第一章 函数与极限",
        "chapter_number": 1,
        "parent_chapter_id": null,
        "description": "学习函数的基本概念、性质以及极限的定义和计算方法",
        "content": "<h2>1.1 函数的概念</h2><p>函数是数学中的基本概念之一，它描述了两个变量之间的依赖关系。设D是一个非空数集，如果对于D中的每一个数x，按照某种对应法则f，都有唯一确定的数y与之对应，则称y是x的函数，记作y=f(x)。</p><h2>1.2 函数的性质</h2><p>函数具有单调性、奇偶性、周期性等性质。单调性分为单调递增和单调递减；奇偶性分为奇函数和偶函数；周期性是指函数值按一定规律重复出现。</p><h2>1.3 极限的概念</h2><p>极限是微积分的基础概念，描述函数在某点附近的变化趋势。当自变量x无限接近某个值时，函数值f(x)无限接近的常数A称为函数在该点的极限。</p><h2>1.4 极限的运算法则</h2><p>极限运算遵循四则运算法则、复合函数法则等基本规律，这些法则为后续的导数计算奠定了基础。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter1_limits.mp4",
        "video_duration": 3600,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limits_exercises.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limits_solutions.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 1250,
        "like_count": 89,
        "comment_count": 23,
        "create_time": ISODate("2024-01-15T09:00:00.000Z"),
        "update_time": ISODate("2024-01-15T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第一章的小节
    {
        "_id": "19989528049-1234-78bb-8000-0063e94fb",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "1.1 函数的概念",
        "chapter_number": 1,
        "parent_chapter_id": "19989528049-1234-7de4-8000-e8d8f58b5",
        "description": "学习函数的基本定义、定义域、值域等基本概念",
        "content": "<h3>1.1.1 函数的定义</h3><p>设D是一个非空数集，如果对于D中的每一个数x，按照某种对应法则f，都有唯一确定的数y与之对应，则称y是x的函数，记作y=f(x)。</p><h3>1.1.2 定义域和值域</h3><p>定义域是自变量x的取值范围，值域是因变量y的取值范围。</p><h3>1.1.3 函数的表示方法</h3><p>函数可以用解析式、图像、表格等方式表示。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter1_1_function_concept.mp4",
        "video_duration": 1200,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/function_basics.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 450,
        "like_count": 32,
        "comment_count": 8,
        "create_time": ISODate("2024-01-15T09:30:00.000Z"),
        "update_time": ISODate("2024-01-15T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-78c5-8000-61db35179",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "1.2 函数的性质",
        "chapter_number": 1,
        "parent_chapter_id": "19989528049-1234-7de4-8000-e8d8f58b5",
        "description": "掌握函数的单调性、奇偶性、周期性等基本性质",
        "content": "<h3>1.2.1 单调性</h3><p>函数在某个区间内单调递增或单调递减的性质。</p><h3>1.2.2 奇偶性</h3><p>奇函数满足f(-x)=-f(x)，偶函数满足f(-x)=f(x)。</p><h3>1.2.3 周期性</h3><p>如果存在正数T，使得f(x+T)=f(x)对所有x成立，则称f(x)是周期函数。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter1_2_function_properties.mp4",
        "video_duration": 1500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/function_properties.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 380,
        "like_count": 28,
        "comment_count": 6,
        "create_time": ISODate("2024-01-15T10:00:00.000Z"),
        "update_time": ISODate("2024-01-15T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7f82-8000-d8dff972b",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "1.3 极限的概念",
        "chapter_number": 1,
        "parent_chapter_id": "19989528049-1234-7de4-8000-e8d8f58b5",
        "description": "理解极限的严格定义和几何意义",
        "content": "<h3>1.3.1 极限的直观定义</h3><p>当x无限接近a时，f(x)无限接近某个常数A。</p><h3>1.3.2 极限的严格定义</h3><p>ε-δ定义：对于任意ε>0，存在δ>0，使得当0<|x-a|<δ时，|f(x)-A|<ε。</p><h3>1.3.3 左极限和右极限</h3><p>分别考虑x从左侧和右侧趋近于a时的极限。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter1_3_limit_concept.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_definition.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 420,
        "like_count": 29,
        "comment_count": 9,
        "create_time": ISODate("2024-01-15T10:30:00.000Z"),
        "update_time": ISODate("2024-01-15T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7d1b-8000-9614b8596",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "1.4 极限的运算法则",
        "chapter_number": 1,
        "parent_chapter_id": "19989528049-1234-7de4-8000-e8d8f58b5",
        "description": "掌握极限的四则运算法则和复合函数极限",
        "content": "<h3>1.4.1 四则运算法则</h3><p>极限的和、差、积、商的运算法则。</p><h3>1.4.2 复合函数极限</h3><p>复合函数的极限运算法则。</p><h3>1.4.3 夹逼定理</h3><p>利用夹逼定理求极限的重要方法。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter1_4_limit_rules.mp4",
        "video_duration": 1200,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_rules.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 350,
        "like_count": 25,
        "comment_count": 7,
        "create_time": ISODate("2024-01-15T11:00:00.000Z"),
        "update_time": ISODate("2024-01-15T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第二章 导数与微分
    {
        "_id": "19989528049-1234-70b5-8000-f984596c0",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第二章 导数与微分",
        "chapter_number": 2,
        "parent_chapter_id": null,
        "description": "掌握导数的定义、几何意义、计算方法以及微分的概念和应用",
        "content": "<h2>2.1 导数的定义</h2><p>导数描述函数在某点的瞬时变化率，是微积分的核心概念。导数的定义基于极限概念，表示函数值随自变量变化的快慢程度。</p><h2>2.2 导数的几何意义</h2><p>导数的几何意义是函数曲线在某点处的切线斜率，这为理解函数的局部性质提供了直观的几何解释。</p><h2>2.3 导数的计算</h2><p>掌握基本初等函数的导数公式、导数的四则运算法则、复合函数求导法则（链式法则）以及隐函数求导方法。</p><h2>2.4 高阶导数</h2><p>高阶导数是导数的导数，在物理学和工程学中有重要应用，如加速度是速度的导数，速度是位移的导数。</p><h2>2.5 微分</h2><p>微分是导数的另一种表达形式，在近似计算和误差分析中有重要应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter2_derivatives.mp4",
        "video_duration": 4200,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivatives_exercises.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivatives_solutions.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivative_rules_cheatsheet.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 1180,
        "like_count": 95,
        "comment_count": 31,
        "create_time": ISODate("2024-01-16T09:00:00.000Z"),
        "update_time": ISODate("2024-01-16T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第二章的小节
    {
        "_id": "19989528049-1234-7152-8000-c939f940f",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "2.1 导数的定义",
        "chapter_number": 2,
        "parent_chapter_id": "19989528049-1234-70b5-8000-f984596c0",
        "description": "理解导数的定义和几何意义",
        "content": "<h3>2.1.1 导数的定义</h3><p>f'(x) = lim[h→0] [f(x+h)-f(x)]/h</p><h3>2.1.2 导数的几何意义</h3><p>导数表示函数曲线在某点处的切线斜率。</p><h3>2.1.3 导数的物理意义</h3><p>导数表示瞬时变化率，如速度是位移的导数。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter2_1_derivative_definition.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivative_definition.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 420,
        "like_count": 35,
        "comment_count": 12,
        "create_time": ISODate("2024-01-16T09:30:00.000Z"),
        "update_time": ISODate("2024-01-16T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-77a5-8000-ac8c85398",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "2.2 导数的计算",
        "chapter_number": 2,
        "parent_chapter_id": "19989528049-1234-70b5-8000-f984596c0",
        "description": "掌握基本初等函数的导数公式",
        "content": "<h3>2.2.1 基本初等函数的导数</h3><p>幂函数、指数函数、对数函数、三角函数的导数公式。</p><h3>2.2.2 导数的四则运算法则</h3><p>和、差、积、商的导数运算法则。</p><h3>2.2.3 复合函数求导法则</h3><p>链式法则：d/dx[f(g(x))] = f'(g(x))·g'(x)。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter2_2_derivative_calculation.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivative_formulas.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 380,
        "like_count": 32,
        "comment_count": 10,
        "create_time": ISODate("2024-01-16T10:00:00.000Z"),
        "update_time": ISODate("2024-01-16T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7316-8000-a02bc38dc",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "2.3 高阶导数",
        "chapter_number": 2,
        "parent_chapter_id": "19989528049-1234-70b5-8000-f984596c0",
        "description": "学习高阶导数的概念和计算",
        "content": "<h3>2.3.1 高阶导数的定义</h3><p>二阶导数、三阶导数等概念。</p><h3>2.3.2 高阶导数的计算</h3><p>逐次求导的方法。</p><h3>2.3.3 高阶导数的应用</h3><p>在物理学和工程学中的应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter2_3_higher_derivatives.mp4",
        "video_duration": 1200,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/higher_derivatives.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 300,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-16T10:30:00.000Z"),
        "update_time": ISODate("2024-01-16T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7a87-8000-535c68bb4",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "2.4 微分",
        "chapter_number": 2,
        "parent_chapter_id": "19989528049-1234-70b5-8000-f984596c0",
        "description": "理解微分的概念和应用",
        "content": "<h3>2.4.1 微分的定义</h3><p>dy = f'(x)dx，微分是导数的另一种表达形式。</p><h3>2.4.2 微分的几何意义</h3><p>微分表示函数增量的线性主部。</p><h3>2.4.3 微分的应用</h3><p>在近似计算和误差分析中的应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter2_4_differential.mp4",
        "video_duration": 1500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/differential_concept.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 280,
        "like_count": 18,
        "comment_count": 4,
        "create_time": ISODate("2024-01-16T11:00:00.000Z"),
        "update_time": ISODate("2024-01-16T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第三章 导数的应用
    {
        "_id": "19989528049-1234-7fcc-8000-e5ecba24a",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第三章 导数的应用",
        "chapter_number": 3,
        "parent_chapter_id": null,
        "description": "学习导数在函数性质研究、最值问题、曲线描绘等方面的应用",
        "content": "<h2>3.1 函数的单调性</h2><p>利用导数判断函数的单调性：当f'(x)>0时，函数单调递增；当f'(x)<0时，函数单调递减。</p><h2>3.2 函数的极值</h2><p>极值是函数在局部范围内的最大值或最小值。通过求导并令导数为零，可以找到可能的极值点。</p><h2>3.3 函数的最值</h2><p>最值问题是导数应用的重要领域，包括闭区间上的最值问题和实际应用中的优化问题。</p><h2>3.4 曲线的凹凸性</h2><p>利用二阶导数判断曲线的凹凸性，当f''(x)>0时曲线凹向上，当f''(x)<0时曲线凹向下。</p><h2>3.5 拐点</h2><p>拐点是曲线凹凸性发生改变的点，在函数图像分析中具有重要意义。</p><h2>3.6 渐近线</h2><p>渐近线包括水平渐近线、垂直渐近线和斜渐近线，是描绘函数图像的重要工具。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter3_derivative_applications.mp4",
        "video_duration": 4800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/derivative_applications_exercises.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/optimization_problems.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/curve_sketching_guide.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 1080,
        "like_count": 78,
        "comment_count": 19,
        "create_time": ISODate("2024-01-17T09:00:00.000Z"),
        "update_time": ISODate("2024-01-17T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第三章的小节
    {
        "_id": "19989528049-1234-761b-8000-9ee6a055e",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "3.1 函数的单调性",
        "chapter_number": 3,
        "parent_chapter_id": "19989528049-1234-7fcc-8000-e5ecba24a",
        "description": "利用导数判断函数的单调性",
        "content": "<h3>3.1.1 单调性的判定</h3><p>当f'(x)>0时，函数单调递增；当f'(x)<0时，函数单调递减。</p><h3>3.1.2 单调区间的求法</h3><p>通过求导数的符号变化来确定单调区间。</p><h3>3.1.3 应用实例</h3><p>利用单调性解决实际问题。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter3_1_monotonicity.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/monotonicity_analysis.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 350,
        "like_count": 28,
        "comment_count": 8,
        "create_time": ISODate("2024-01-17T09:30:00.000Z"),
        "update_time": ISODate("2024-01-17T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7348-8000-64953e36e",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "3.2 函数的极值",
        "chapter_number": 3,
        "parent_chapter_id": "19989528049-1234-7fcc-8000-e5ecba24a",
        "description": "学习函数极值的概念和求法",
        "content": "<h3>3.2.1 极值的定义</h3><p>函数在局部范围内的最大值或最小值。</p><h3>3.2.2 极值的必要条件</h3><p>可导函数在极值点处导数必为零。</p><h3>3.2.3 极值的充分条件</h3><p>利用一阶导数或二阶导数判断极值。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter3_2_extrema.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/extrema_theory.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 320,
        "like_count": 25,
        "comment_count": 7,
        "create_time": ISODate("2024-01-17T10:00:00.000Z"),
        "update_time": ISODate("2024-01-17T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-75f9-8000-ab6154a77",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "3.3 函数的最值",
        "chapter_number": 3,
        "parent_chapter_id": "19989528049-1234-7fcc-8000-e5ecba24a",
        "description": "掌握最值问题的求解方法",
        "content": "<h3>3.3.1 闭区间上的最值</h3><p>在闭区间上连续函数必有最大值和最小值。</p><h3>3.3.2 最值的求法</h3><p>比较函数在驻点、端点处的函数值。</p><h3>3.3.3 实际应用问题</h3><p>利用最值理论解决优化问题。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter3_3_optimization.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/optimization_problems.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 6,
        "create_time": ISODate("2024-01-17T10:30:00.000Z"),
        "update_time": ISODate("2024-01-17T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7f74-8000-96738ab5b",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "3.4 曲线的凹凸性",
        "chapter_number": 3,
        "parent_chapter_id": "19989528049-1234-7fcc-8000-e5ecba24a",
        "description": "利用二阶导数判断曲线的凹凸性",
        "content": "<h3>3.4.1 凹凸性的定义</h3><p>曲线向上凸或向下凸的性质。</p><h3>3.4.2 凹凸性的判定</h3><p>当f''(x)>0时曲线凹向上，当f''(x)<0时曲线凹向下。</p><h3>3.4.3 拐点</h3><p>曲线凹凸性发生改变的点。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter3_4_concavity.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/concavity_analysis.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-17T11:00:00.000Z"),
        "update_time": ISODate("2024-01-17T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第四章 不定积分
    {
        "_id": "19989528049-1234-7757-8000-da1bb5c49",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第四章 不定积分",
        "chapter_number": 4,
        "parent_chapter_id": null,
        "description": "掌握不定积分的概念、性质和基本积分方法",
        "content": "<h2>4.1 不定积分的概念</h2><p>不定积分是导数的逆运算，表示所有导数为给定函数的函数族。如果F'(x)=f(x)，则F(x)是f(x)的一个原函数。</p><h2>4.2 基本积分公式</h2><p>掌握基本初等函数的不定积分公式，包括幂函数、指数函数、三角函数、对数函数等的积分。</p><h2>4.3 积分的性质</h2><p>不定积分具有线性性质，即∫[af(x)+bg(x)]dx = a∫f(x)dx + b∫g(x)dx。</p><h2>4.4 换元积分法</h2><p>换元积分法是不定积分的重要方法，包括第一类换元法（凑微分法）和第二类换元法。</p><h2>4.5 分部积分法</h2><p>分部积分法适用于被积函数是两个函数乘积的情况，公式为∫udv = uv - ∫vdu。</p><h2>4.6 有理函数的积分</h2><p>有理函数的积分通过部分分式分解等方法求解，是积分计算中的重要技巧。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter4_indefinite_integral.mp4",
        "video_duration": 4500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integral_formulas.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integration_techniques.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integration_exercises.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 980,
        "like_count": 72,
        "comment_count": 25,
        "create_time": ISODate("2024-01-18T09:00:00.000Z"),
        "update_time": ISODate("2024-01-18T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第四章的小节
    {
        "_id": "19989528049-1234-7541-8000-59bac4238",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "4.1 不定积分的概念",
        "chapter_number": 4,
        "parent_chapter_id": "19989528049-1234-7757-8000-da1bb5c49",
        "description": "理解不定积分的定义和基本概念",
        "content": "<h3>4.1.1 原函数的概念</h3><p>如果F'(x)=f(x)，则F(x)是f(x)的一个原函数。</p><h3>4.1.2 不定积分的定义</h3><p>∫f(x)dx = F(x) + C，其中C是任意常数。</p><h3>4.1.3 不定积分的几何意义</h3><p>不定积分表示一族平行曲线。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter4_1_indefinite_integral_concept.mp4",
        "video_duration": 1500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/indefinite_integral_basics.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 320,
        "like_count": 24,
        "comment_count": 6,
        "create_time": ISODate("2024-01-18T09:30:00.000Z"),
        "update_time": ISODate("2024-01-18T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7746-8000-08caa114c",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "4.2 基本积分公式",
        "chapter_number": 4,
        "parent_chapter_id": "19989528049-1234-7757-8000-da1bb5c49",
        "description": "掌握基本初等函数的积分公式",
        "content": "<h3>4.2.1 幂函数的积分</h3><p>∫x^n dx = x^(n+1)/(n+1) + C (n≠-1)</p><h3>4.2.2 指数函数的积分</h3><p>∫e^x dx = e^x + C, ∫a^x dx = a^x/ln(a) + C</p><h3>4.2.3 三角函数的积分</h3><p>∫sin(x)dx = -cos(x) + C, ∫cos(x)dx = sin(x) + C</p><h3>4.2.4 对数函数的积分</h3><p>∫1/x dx = ln|x| + C</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter4_2_basic_integral_formulas.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/basic_integral_formulas.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 380,
        "like_count": 28,
        "comment_count": 8,
        "create_time": ISODate("2024-01-18T10:00:00.000Z"),
        "update_time": ISODate("2024-01-18T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7597-8000-3e4f20dc7",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "4.3 换元积分法",
        "chapter_number": 4,
        "parent_chapter_id": "19989528049-1234-7757-8000-da1bb5c49",
        "description": "掌握第一类和第二类换元积分法",
        "content": "<h3>4.3.1 第一类换元法（凑微分法）</h3><p>∫f(g(x))g'(x)dx = ∫f(u)du，其中u=g(x)。</p><h3>4.3.2 第二类换元法</h3><p>通过变量替换简化积分计算。</p><h3>4.3.3 三角换元</h3><p>利用三角函数进行变量替换。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter4_3_substitution_method.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/substitution_methods.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 350,
        "like_count": 26,
        "comment_count": 7,
        "create_time": ISODate("2024-01-18T10:30:00.000Z"),
        "update_time": ISODate("2024-01-18T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-719c-8000-33669a091",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "4.4 分部积分法",
        "chapter_number": 4,
        "parent_chapter_id": "19989528049-1234-7757-8000-da1bb5c49",
        "description": "掌握分部积分法的应用",
        "content": "<h3>4.4.1 分部积分公式</h3><p>∫udv = uv - ∫vdu</p><h3>4.4.2 分部积分的应用</h3><p>适用于被积函数是两个函数乘积的情况。</p><h3>4.4.3 循环积分</h3><p>某些积分需要多次使用分部积分法。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter4_4_integration_by_parts.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integration_by_parts.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 5,
        "create_time": ISODate("2024-01-18T11:00:00.000Z"),
        "update_time": ISODate("2024-01-18T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第五章 定积分
    {
        "_id": "19989528049-1234-74ea-8000-ace1c3479",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第五章 定积分",
        "chapter_number": 5,
        "parent_chapter_id": null,
        "description": "学习定积分的定义、性质、计算方法和几何意义",
        "content": "<h2>5.1 定积分的定义</h2><p>定积分是黎曼积分的概念，通过分割、近似、求和、取极限的过程定义。定积分∫[a,b]f(x)dx表示函数f(x)在区间[a,b]上的积分。</p><h2>5.2 定积分的性质</h2><p>定积分具有线性性质、区间可加性、单调性等基本性质，这些性质为定积分的计算提供了理论基础。</p><h2>5.3 微积分基本定理</h2><p>微积分基本定理建立了导数与积分之间的联系，包括第一基本定理和第二基本定理。</p><h2>5.4 定积分的计算</h2><p>利用牛顿-莱布尼茨公式计算定积分，结合换元积分法和分部积分法求解复杂的定积分问题。</p><h2>5.5 定积分的几何意义</h2><p>定积分在几何上表示曲线与坐标轴围成的面积，这为理解积分的几何意义提供了直观的解释。</p><h2>5.6 定积分的应用</h2><p>定积分在计算面积、体积、弧长等几何量以及解决物理问题中有重要应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter5_definite_integral.mp4",
        "video_duration": 4200,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/definite_integral_properties.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/fundamental_theorem.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/area_volume_calculations.pdf"
        ],
        "sort_order": 5,
        "status": 1,
        "view_count": 920,
        "like_count": 85,
        "comment_count": 28,
        "create_time": ISODate("2024-01-19T09:00:00.000Z"),
        "update_time": ISODate("2024-01-19T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第五章的小节
    {
        "_id": "19989528049-1234-73ed-8000-08ad372b2",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "5.1 定积分的定义",
        "chapter_number": 5,
        "parent_chapter_id": "19989528049-1234-74ea-8000-ace1c3479",
        "description": "理解定积分的黎曼定义",
        "content": "<h3>5.1.1 黎曼和</h3><p>通过分割、近似、求和、取极限的过程定义定积分。</p><h3>5.1.2 定积分的几何意义</h3><p>定积分表示曲线与坐标轴围成的面积。</p><h3>5.1.3 定积分的物理意义</h3><p>在物理学中表示累积量，如位移、功等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter5_1_definite_integral_definition.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/definite_integral_definition.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 320,
        "like_count": 24,
        "comment_count": 6,
        "create_time": ISODate("2024-01-19T09:30:00.000Z"),
        "update_time": ISODate("2024-01-19T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7f62-8000-3370cb297",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "5.2 定积分的性质",
        "chapter_number": 5,
        "parent_chapter_id": "19989528049-1234-74ea-8000-ace1c3479",
        "description": "掌握定积分的基本性质",
        "content": "<h3>5.2.1 线性性质</h3><p>∫[a,b][αf(x)+βg(x)]dx = α∫[a,b]f(x)dx + β∫[a,b]g(x)dx</p><h3>5.2.2 区间可加性</h3><p>∫[a,b]f(x)dx = ∫[a,c]f(x)dx + ∫[c,b]f(x)dx</p><h3>5.2.3 单调性</h3><p>如果f(x)≤g(x)，则∫[a,b]f(x)dx ≤ ∫[a,b]g(x)dx</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter5_2_definite_integral_properties.mp4",
        "video_duration": 1500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/definite_integral_properties.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-19T10:00:00.000Z"),
        "update_time": ISODate("2024-01-19T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7790-8000-3cb63e025",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "5.3 微积分基本定理",
        "chapter_number": 5,
        "parent_chapter_id": "19989528049-1234-74ea-8000-ace1c3479",
        "description": "理解微积分基本定理的重要性",
        "content": "<h3>5.3.1 第一基本定理</h3><p>如果F(x)=∫[a,x]f(t)dt，则F'(x)=f(x)。</p><h3>5.3.2 第二基本定理</h3><p>∫[a,b]f(x)dx = F(b) - F(a)，其中F'(x)=f(x)。</p><h3>5.3.3 牛顿-莱布尼茨公式</h3><p>建立了导数与积分之间的联系。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter5_3_fundamental_theorem.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/fundamental_theorem.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 350,
        "like_count": 28,
        "comment_count": 8,
        "create_time": ISODate("2024-01-19T10:30:00.000Z"),
        "update_time": ISODate("2024-01-19T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-764d-8000-263d7e5b0",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "5.4 定积分的应用",
        "chapter_number": 5,
        "parent_chapter_id": "19989528049-1234-74ea-8000-ace1c3479",
        "description": "学习定积分在几何和物理中的应用",
        "content": "<h3>5.4.1 计算面积</h3><p>利用定积分计算平面图形的面积。</p><h3>5.4.2 计算体积</h3><p>利用定积分计算旋转体的体积。</p><h3>5.4.3 计算弧长</h3><p>利用定积分计算曲线的弧长。</p><h3>5.4.4 物理应用</h3><p>在物理学中计算功、质心等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter5_4_definite_integral_applications.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/definite_integral_applications.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 6,
        "create_time": ISODate("2024-01-19T11:00:00.000Z"),
        "update_time": ISODate("2024-01-19T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第六章 多元函数微分学
    {
        "_id": "19989528049-1234-7d96-8000-c31897b2f",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第六章 多元函数微分学",
        "chapter_number": 6,
        "parent_chapter_id": null,
        "description": "学习多元函数的概念、偏导数、全微分以及多元函数的极值问题",
        "content": "<h2>6.1 多元函数的概念</h2><p>多元函数是定义在n维空间上的函数，最常见的是二元函数z=f(x,y)。多元函数在几何上表示空间中的曲面。</p><h2>6.2 偏导数</h2><p>偏导数是多元函数对某个变量的导数，其他变量视为常数。偏导数∂f/∂x表示函数f对x的偏导数。</p><h2>6.3 全微分</h2><p>全微分是多元函数增量的线性主部，dz = (∂f/∂x)dx + (∂f/∂y)dy。全微分在近似计算中有重要应用。</p><h2>6.4 复合函数求导</h2><p>多元复合函数的求导法则，包括链式法则在多元函数中的应用。</p><h2>6.5 隐函数求导</h2><p>隐函数是由方程F(x,y)=0确定的函数关系，通过隐函数求导法则可以求出dy/dx。</p><h2>6.6 多元函数的极值</h2><p>多元函数的极值问题包括无条件极值和条件极值，拉格朗日乘数法是求解条件极值的重要方法。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter6_multivariable_calculus.mp4",
        "video_duration": 5100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/partial_derivatives.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/multivariable_optimization.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/lagrange_multipliers.pdf"
        ],
        "sort_order": 6,
        "status": 1,
        "view_count": 850,
        "like_count": 68,
        "comment_count": 22,
        "create_time": ISODate("2024-01-20T09:00:00.000Z"),
        "update_time": ISODate("2024-01-20T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第六章的小节
    {
        "_id": "19989528049-1234-7fba-8000-8e88c2dc8",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "6.1 多元函数的概念",
        "chapter_number": 6,
        "parent_chapter_id": "19989528049-1234-7d96-8000-c31897b2f",
        "description": "理解多元函数的定义和几何意义",
        "content": "<h3>6.1.1 二元函数的定义</h3><p>z=f(x,y)表示定义在平面区域D上的函数。</p><h3>6.1.2 多元函数的几何意义</h3><p>二元函数在几何上表示空间中的曲面。</p><h3>6.1.3 多元函数的极限</h3><p>多元函数极限的概念和性质。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter6_1_multivariable_functions.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/multivariable_functions.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 6,
        "create_time": ISODate("2024-01-20T09:30:00.000Z"),
        "update_time": ISODate("2024-01-20T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7464-8000-ff0c91671",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "6.2 偏导数",
        "chapter_number": 6,
        "parent_chapter_id": "19989528049-1234-7d96-8000-c31897b2f",
        "description": "掌握偏导数的概念和计算",
        "content": "<h3>6.2.1 偏导数的定义</h3><p>∂f/∂x = lim[h→0][f(x+h,y)-f(x,y)]/h</p><h3>6.2.2 偏导数的几何意义</h3><p>偏导数表示曲面在某个方向上的切线斜率。</p><h3>6.2.3 高阶偏导数</h3><p>二阶偏导数、混合偏导数等概念。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter6_2_partial_derivatives.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/partial_derivatives.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-20T10:00:00.000Z"),
        "update_time": ISODate("2024-01-20T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7560-8000-10dd7cc96",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "6.3 全微分",
        "chapter_number": 6,
        "parent_chapter_id": "19989528049-1234-7d96-8000-c31897b2f",
        "description": "理解全微分的概念和应用",
        "content": "<h3>6.3.1 全微分的定义</h3><p>dz = (∂f/∂x)dx + (∂f/∂y)dy</p><h3>6.3.2 全微分的几何意义</h3><p>全微分表示函数增量的线性主部。</p><h3>6.3.3 全微分的应用</h3><p>在近似计算和误差分析中的应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter6_3_total_differential.mp4",
        "video_duration": 1500,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/total_differential.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 250,
        "like_count": 18,
        "comment_count": 4,
        "create_time": ISODate("2024-01-20T10:30:00.000Z"),
        "update_time": ISODate("2024-01-20T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-72f2-8000-0ec7edc32",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "6.4 多元函数的极值",
        "chapter_number": 6,
        "parent_chapter_id": "19989528049-1234-7d96-8000-c31897b2f",
        "description": "学习多元函数的极值问题",
        "content": "<h3>6.4.1 无条件极值</h3><p>多元函数在定义域内的极值问题。</p><h3>6.4.2 条件极值</h3><p>在约束条件下的极值问题。</p><h3>6.4.3 拉格朗日乘数法</h3><p>求解条件极值的重要方法。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter6_4_multivariable_extrema.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/multivariable_extrema.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 320,
        "like_count": 25,
        "comment_count": 7,
        "create_time": ISODate("2024-01-20T11:00:00.000Z"),
        "update_time": ISODate("2024-01-20T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第七章 重积分
    {
        "_id": "19989528049-1234-7700-8000-a1e125786",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第七章 重积分",
        "chapter_number": 7,
        "parent_chapter_id": null,
        "description": "掌握二重积分和三重积分的概念、性质、计算方法和应用",
        "content": "<h2>7.1 二重积分的概念</h2><p>二重积分是定积分在二维空间的推广，表示函数在平面区域上的积分。二重积分∬D f(x,y)dxdy表示函数f(x,y)在区域D上的积分。</p><h2>7.2 二重积分的性质</h2><p>二重积分具有线性性质、区域可加性、单调性等基本性质，这些性质为二重积分的计算提供了理论基础。</p><h2>7.3 二重积分的计算</h2><p>二重积分的计算通常通过化为累次积分进行，包括直角坐标系下的计算和极坐标系下的计算。</p><h2>7.4 二重积分的应用</h2><p>二重积分在计算平面图形的面积、曲面的面积、物体的质量、重心等几何和物理量中有重要应用。</p><h2>7.5 三重积分</h2><p>三重积分是定积分在三维空间的推广，表示函数在空间区域上的积分。三重积分∭V f(x,y,z)dxdydz表示函数f(x,y,z)在区域V上的积分。</p><h2>7.6 三重积分的计算</h2><p>三重积分的计算通过化为累次积分进行，包括直角坐标系、柱坐标系和球坐标系下的计算。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter7_multiple_integrals.mp4",
        "video_duration": 4800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/double_integral_techniques.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/triple_integral_methods.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/coordinate_systems.pdf"
        ],
        "sort_order": 7,
        "status": 1,
        "view_count": 780,
        "like_count": 62,
        "comment_count": 18,
        "create_time": ISODate("2024-01-21T09:00:00.000Z"),
        "update_time": ISODate("2024-01-21T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第七章的小节
    {
        "_id": "19989528049-1234-7c94-8000-abd2cc0c1",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "7.1 二重积分的概念",
        "chapter_number": 7,
        "parent_chapter_id": "19989528049-1234-7700-8000-a1e125786",
        "description": "理解二重积分的定义和几何意义",
        "content": "<h3>7.1.1 二重积分的定义</h3><p>∬D f(x,y)dxdy表示函数f(x,y)在区域D上的积分。</p><h3>7.1.2 二重积分的几何意义</h3><p>二重积分表示曲面与坐标平面围成的体积。</p><h3>7.1.3 二重积分的性质</h3><p>线性性质、区域可加性等基本性质。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter7_1_double_integral_concept.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/double_integral_concept.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-21T09:30:00.000Z"),
        "update_time": ISODate("2024-01-21T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7de3-8000-560ceeb3e",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "7.2 二重积分的计算",
        "chapter_number": 7,
        "parent_chapter_id": "19989528049-1234-7700-8000-a1e125786",
        "description": "掌握二重积分的计算方法",
        "content": "<h3>7.2.1 直角坐标系下的计算</h3><p>化为累次积分进行计算。</p><h3>7.2.2 极坐标系下的计算</h3><p>利用极坐标变换简化计算。</p><h3>7.2.3 积分次序的交换</h3><p>交换积分次序的技巧。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter7_2_double_integral_calculation.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/double_integral_calculation.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 320,
        "like_count": 24,
        "comment_count": 6,
        "create_time": ISODate("2024-01-21T10:00:00.000Z"),
        "update_time": ISODate("2024-01-21T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7b2b-8000-edf4b8212",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "7.3 三重积分",
        "chapter_number": 7,
        "parent_chapter_id": "19989528049-1234-7700-8000-a1e125786",
        "description": "学习三重积分的概念和计算",
        "content": "<h3>7.3.1 三重积分的定义</h3><p>∭V f(x,y,z)dxdydz表示函数f(x,y,z)在区域V上的积分。</p><h3>7.3.2 直角坐标系下的计算</h3><p>化为累次积分进行计算。</p><h3>7.3.3 柱坐标系和球坐标系</h3><p>利用不同坐标系简化计算。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter7_3_triple_integral.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/triple_integral.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 260,
        "like_count": 18,
        "comment_count": 4,
        "create_time": ISODate("2024-01-21T10:30:00.000Z"),
        "update_time": ISODate("2024-01-21T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7cfb-8000-07f4677ed",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "7.4 重积分的应用",
        "chapter_number": 7,
        "parent_chapter_id": "19989528049-1234-7700-8000-a1e125786",
        "description": "学习重积分在几何和物理中的应用",
        "content": "<h3>7.4.1 计算面积和体积</h3><p>利用重积分计算平面图形的面积和空间立体的体积。</p><h3>7.4.2 计算质量</h3><p>利用重积分计算物体的质量。</p><h3>7.4.3 计算重心</h3><p>利用重积分计算物体的重心坐标。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter7_4_multiple_integral_applications.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/multiple_integral_applications.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 240,
        "like_count": 16,
        "comment_count": 3,
        "create_time": ISODate("2024-01-21T11:00:00.000Z"),
        "update_time": ISODate("2024-01-21T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第八章 曲线积分与曲面积分
    {
        "_id": "19989528049-1234-73ac-8000-8008f8d3c",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第八章 曲线积分与曲面积分",
        "chapter_number": 8,
        "parent_chapter_id": null,
        "description": "学习曲线积分和曲面积分的概念、性质、计算方法和物理意义",
        "content": "<h2>8.1 对弧长的曲线积分</h2><p>对弧长的曲线积分是函数沿曲线的积分，表示函数在曲线上的积分。第一类曲线积分∫L f(x,y)ds表示函数f(x,y)沿曲线L的积分。</p><h2>8.2 对坐标的曲线积分</h2><p>对坐标的曲线积分是向量场沿曲线的积分，在物理学中有重要应用。第二类曲线积分∫L Pdx + Qdy表示向量场(P,Q)沿曲线L的积分。</p><h2>8.3 格林公式</h2><p>格林公式建立了平面区域上的二重积分与边界曲线上的曲线积分之间的关系，是曲线积分理论的重要定理。</p><h2>8.4 对面积的曲面积分</h2><p>对面积的曲面积分是函数在曲面上的积分，表示函数在曲面上的积分。第一类曲面积分∬Σ f(x,y,z)dS表示函数f(x,y,z)在曲面Σ上的积分。</p><h2>8.5 对坐标的曲面积分</h2><p>对坐标的曲面积分是向量场通过曲面的积分，在电磁学中有重要应用。第二类曲面积分∬Σ Pdydz + Qdzdx + Rdxdy表示向量场(P,Q,R)通过曲面Σ的积分。</p><h2>8.6 高斯公式和斯托克斯公式</h2><p>高斯公式建立了空间区域上的三重积分与边界曲面上的曲面积分之间的关系；斯托克斯公式建立了曲面上的曲面积分与边界曲线上的曲线积分之间的关系。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter8_line_surface_integrals.mp4",
        "video_duration": 5400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/line_integral_methods.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/surface_integral_techniques.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/green_stokes_gauss_theorems.pdf"
        ],
        "sort_order": 8,
        "status": 1,
        "view_count": 720,
        "like_count": 58,
        "comment_count": 15,
        "create_time": ISODate("2024-01-22T09:00:00.000Z"),
        "update_time": ISODate("2024-01-22T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第八章的小节
    {
        "_id": "19989528049-1234-7673-8000-f73248eec",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "8.1 对弧长的曲线积分",
        "chapter_number": 8,
        "parent_chapter_id": "19989528049-1234-73ac-8000-8008f8d3c",
        "description": "学习第一类曲线积分的概念和计算",
        "content": "<h3>8.1.1 第一类曲线积分的定义</h3><p>∫L f(x,y)ds表示函数f(x,y)沿曲线L的积分。</p><h3>8.1.2 第一类曲线积分的计算</h3><p>化为定积分进行计算。</p><h3>8.1.3 第一类曲线积分的应用</h3><p>计算曲线的质量、重心等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter8_1_line_integral_arc_length.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/line_integral_arc_length.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 250,
        "like_count": 18,
        "comment_count": 4,
        "create_time": ISODate("2024-01-22T09:30:00.000Z"),
        "update_time": ISODate("2024-01-22T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7d3d-8000-4491c4628",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "8.2 对坐标的曲线积分",
        "chapter_number": 8,
        "parent_chapter_id": "19989528049-1234-73ac-8000-8008f8d3c",
        "description": "学习第二类曲线积分的概念和计算",
        "content": "<h3>8.2.1 第二类曲线积分的定义</h3><p>∫L Pdx + Qdy表示向量场(P,Q)沿曲线L的积分。</p><h3>8.2.2 第二类曲线积分的计算</h3><p>化为定积分进行计算。</p><h3>8.2.3 第二类曲线积分的应用</h3><p>在物理学中计算功、环流等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter8_2_line_integral_coordinates.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/line_integral_coordinates.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-22T10:00:00.000Z"),
        "update_time": ISODate("2024-01-22T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-72d2-8000-495339fea",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "8.3 格林公式",
        "chapter_number": 8,
        "parent_chapter_id": "19989528049-1234-73ac-8000-8008f8d3c",
        "description": "掌握格林公式及其应用",
        "content": "<h3>8.3.1 格林公式的表述</h3><p>∬D (∂Q/∂x - ∂P/∂y)dxdy = ∮L Pdx + Qdy</p><h3>8.3.2 格林公式的应用</h3><p>简化曲线积分的计算。</p><h3>8.3.3 平面区域的面积</h3><p>利用格林公式计算平面区域的面积。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter8_3_green_theorem.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/green_theorem.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 6,
        "create_time": ISODate("2024-01-22T10:30:00.000Z"),
        "update_time": ISODate("2024-01-22T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7141-8000-b87d5d649",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "8.4 曲面积分",
        "chapter_number": 8,
        "parent_chapter_id": "19989528049-1234-73ac-8000-8008f8d3c",
        "description": "学习曲面积分的概念和计算",
        "content": "<h3>8.4.1 对面积的曲面积分</h3><p>∬Σ f(x,y,z)dS表示函数f(x,y,z)在曲面Σ上的积分。</p><h3>8.4.2 对坐标的曲面积分</h3><p>∬Σ Pdydz + Qdzdx + Rdxdy表示向量场通过曲面的积分。</p><h3>8.4.3 高斯公式和斯托克斯公式</h3><p>建立重积分与曲面积分之间的关系。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter8_4_surface_integral.mp4",
        "video_duration": 2700,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/surface_integral.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 320,
        "like_count": 24,
        "comment_count": 7,
        "create_time": ISODate("2024-01-22T11:00:00.000Z"),
        "update_time": ISODate("2024-01-22T11:00:00.000Z"),
        "is_deleted": 0
    },

    // 第九章 无穷级数
    {
        "_id": "19989528049-1234-7028-8000-b707768fc",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "第九章 无穷级数",
        "chapter_number": 9,
        "parent_chapter_id": null,
        "description": "掌握无穷级数的概念、收敛性判别法、幂级数和傅里叶级数",
        "content": "<h2>9.1 无穷级数的概念</h2><p>无穷级数是无穷多个数的和，是数学分析中的重要概念。级数∑(n=1 to ∞)an的收敛性通过部分和序列的极限来定义。</p><h2>9.2 级数的基本性质</h2><p>级数具有线性性质、收敛级数的必要条件等基本性质。如果级数收敛，则其通项趋于零。</p><h2>9.3 正项级数的收敛性判别法</h2><p>正项级数的收敛性判别法包括比较判别法、比值判别法、根值判别法、积分判别法等。</p><h2>9.4 交错级数</h2><p>交错级数是正负项交替出现的级数，莱布尼茨判别法是判断交错级数收敛性的重要方法。</p><h2>9.5 幂级数</h2><p>幂级数是形如∑(n=0 to ∞)an(x-a)^n的级数，在函数展开和近似计算中有重要应用。</p><h2>9.6 函数的幂级数展开</h2><p>许多函数可以展开为幂级数，包括泰勒级数和麦克劳林级数，这为函数的近似计算提供了重要工具。</p><h2>9.7 傅里叶级数</h2><p>傅里叶级数是周期函数的三角级数展开，在信号处理、物理学等领域有重要应用。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter9_infinite_series.mp4",
        "video_duration": 5100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/series_convergence_tests.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/power_series_expansion.pdf",
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/fourier_series_introduction.pdf"
        ],
        "sort_order": 9,
        "status": 1,
        "view_count": 680,
        "like_count": 55,
        "comment_count": 20,
        "create_time": ISODate("2024-01-23T09:00:00.000Z"),
        "update_time": ISODate("2024-01-23T09:00:00.000Z"),
        "is_deleted": 0
    },

    // 第九章的小节
    {
        "_id": "19989528049-1234-769c-8000-f32febf9c",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "9.1 无穷级数的概念",
        "chapter_number": 9,
        "parent_chapter_id": "19989528049-1234-7028-8000-b707768fc",
        "description": "理解无穷级数的定义和收敛性",
        "content": "<h3>9.1.1 无穷级数的定义</h3><p>∑(n=1 to ∞)an = a1 + a2 + a3 + ...</p><h3>9.1.2 级数的收敛性</h3><p>通过部分和序列的极限来定义级数的收敛性。</p><h3>9.1.3 级数的基本性质</h3><p>收敛级数的必要条件、线性性质等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter9_1_infinite_series_concept.mp4",
        "video_duration": 1800,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/infinite_series_concept.pdf"
        ],
        "sort_order": 1,
        "status": 1,
        "view_count": 280,
        "like_count": 20,
        "comment_count": 5,
        "create_time": ISODate("2024-01-23T09:30:00.000Z"),
        "update_time": ISODate("2024-01-23T09:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7e03-8000-82d16f8b4",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "9.2 正项级数的收敛性判别法",
        "chapter_number": 9,
        "parent_chapter_id": "19989528049-1234-7028-8000-b707768fc",
        "description": "掌握正项级数的各种收敛性判别法",
        "content": "<h3>9.2.1 比较判别法</h3><p>通过比较两个级数来判断收敛性。</p><h3>9.2.2 比值判别法</h3><p>利用lim(n→∞)|an+1/an|来判断收敛性。</p><h3>9.2.3 根值判别法</h3><p>利用lim(n→∞)ⁿ√|an|来判断收敛性。</p><h3>9.2.4 积分判别法</h3><p>利用积分来判断级数的收敛性。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter9_2_positive_series_tests.mp4",
        "video_duration": 2400,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/positive_series_tests.pdf"
        ],
        "sort_order": 2,
        "status": 1,
        "view_count": 320,
        "like_count": 24,
        "comment_count": 6,
        "create_time": ISODate("2024-01-23T10:00:00.000Z"),
        "update_time": ISODate("2024-01-23T10:00:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-7a82-8000-14043ac36",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "9.3 幂级数",
        "chapter_number": 9,
        "parent_chapter_id": "19989528049-1234-7028-8000-b707768fc",
        "description": "学习幂级数的概念和性质",
        "content": "<h3>9.3.1 幂级数的定义</h3><p>∑(n=0 to ∞)an(x-a)^n形式的级数。</p><h3>9.3.2 收敛半径</h3><p>幂级数的收敛半径和收敛区间。</p><h3>9.3.3 幂级数的性质</h3><p>幂级数的和函数、逐项求导、逐项积分等。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter9_3_power_series.mp4",
        "video_duration": 2100,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/power_series.pdf"
        ],
        "sort_order": 3,
        "status": 1,
        "view_count": 300,
        "like_count": 22,
        "comment_count": 6,
        "create_time": ISODate("2024-01-23T10:30:00.000Z"),
        "update_time": ISODate("2024-01-23T10:30:00.000Z"),
        "is_deleted": 0
    },
    {
        "_id": "19989528049-1234-72d4-8000-57ccab86b",
        "course_id": "78d44b4a-becd-4f65-9461-f2dcdda03e01",
        "chapter_name": "9.4 函数的幂级数展开",
        "chapter_number": 9,
        "parent_chapter_id": "19989528049-1234-7028-8000-b707768fc",
        "description": "学习函数的泰勒级数和麦克劳林级数展开",
        "content": "<h3>9.4.1 泰勒级数</h3><p>f(x) = ∑(n=0 to ∞)[f^(n)(a)/n!](x-a)^n</p><h3>9.4.2 麦克劳林级数</h3><p>当a=0时的泰勒级数。</p><h3>9.4.3 常用函数的幂级数展开</h3><p>e^x、sin(x)、cos(x)、ln(1+x)等的幂级数展开。</p><h3>9.4.4 傅里叶级数</h3><p>周期函数的三角级数展开。</p>",
        "video_url": "http://127.0.0.1:31589/sapientiacloud-edupivot/videos/chapter9_4_function_expansion.mp4",
        "video_duration": 2700,
        "attachment_urls": [
            "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/function_expansion.pdf"
        ],
        "sort_order": 4,
        "status": 1,
        "view_count": 350,
        "like_count": 26,
        "comment_count": 8,
        "create_time": ISODate("2024-01-23T11:00:00.000Z"),
        "update_time": ISODate("2024-01-23T11:00:00.000Z"),
        "is_deleted": 0
    }
]);
