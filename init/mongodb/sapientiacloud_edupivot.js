/*
 Navicat Premium Dump Script

 Source Server         : localhost_27017
 Source Server Type    : MongoDB
 Source Server Version : 60005 (6.0.5)
 Source Host           : localhost:27017
 Source Schema         : sapientiacloud_edupivot

 Target Server Type    : MongoDB
 Target Server Version : 60005 (6.0.5)
 File Encoding         : 65001

 Date: 16/10/2025 22:53:54
*/


// ----------------------------
// Collection structure for mg_course_chapter
// ----------------------------
db.getCollection("mg_course_chapter").drop();
db.createCollection("mg_course_chapter");
db.getCollection("mg_course_chapter").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_course_chapter").createIndex({
    chapter_number: Int32("1")
}, {
    name: "chapter_number_1"
});
db.getCollection("mg_course_chapter").createIndex({
    parent_chapter_id: Int32("1")
}, {
    name: "parent_chapter_id_1"
});
db.getCollection("mg_course_chapter").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_course_chapter").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});

// ----------------------------
// Documents of mg_course_chapter
// ----------------------------
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第一章 函数与极限",
    description: "学习函数的基本概念、性质以及极限的定义和计算方法",
    content: "<h2>1.1 函数的概念</h2><p>函数是数学中的基本概念之一，它描述了两个变量之间的依赖关系。设D是一个非空数集，如果对于D中的每一个数x，按照某种对应法则f，都有唯一确定的数y与之对应，则称y是x的函数，记作y=f(x)。</p><h2>1.2 函数的性质</h2><p>函数具有单调性、奇偶性、周期性等性质。单调性分为单调递增和单调递减；奇偶性分为奇函数和偶函数；周期性是指函数值按一定规律重复出现。</p><h2>1.3 极限的概念</h2><p>极限是微积分的基础概念，描述函数在某点附近的变化趋势。当自变量x无限接近某个值时，函数值f(x)无限接近的常数A称为函数在该点的极限。</p><h2>1.4 极限的运算法则</h2><p>极限运算遵循四则运算法则、复合函数法则等基本规律，这些法则为后续的导数计算奠定了基础。</p>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/5006bd9f5dd62fdc13a670dbbb8d23ca.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065248Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=29731a80c5d1a164c21cbac9dbe99000fbe7aeffe359aa7b26df40712c913ffa",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/O1CN01TTAgWE1Y6AoAKpJXV_%21%212210898473009.avif?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f0c35a4487345fee35a8a4ab101ec5be5df05e1c1cecab2c1b110f7d095d2e84",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/e1604019539295.webp?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=590b50ee9219167428803db6b6cf3bca41321c8d24ab9feff21d4acc5732ce9a",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/1756915008131.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=cdb1e4669c62fb36adecfe8ea2167d53c92ec5a05fe8c35b3b762742b920e300",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/login-background.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=9a6801328680441629ee27fc0a9173db4ca4bf9cf094dcfad0adc264a951a109",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251001183400_907_138.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=59dcc19c76025171a7289f3c173b094a5d1432b28019687697bad148f2a6daec",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903152605_5_110.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=bc5a9ff7a53699893559ad151fdb091b0d6b724c8d00192ce0806c9f19847700",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903153836_6_110.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251005%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251005T065249Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=1dd9b0fab332f3b9df462e7d35e56d991b874d9ccb7d79257280962b4adb515a"
    ],
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Long("1250"),
    like_count: Long("89"),
    create_time: ISODate("2024-01-15T09:00:00.000Z"),
    update_time: ISODate("2025-10-05T06:52:59.534Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter"
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e03",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "1.1 函数的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    description: "学习函数的基本定义、定义域、值域等基本概念",
    content: "<h3>1.1.1 函数的定义</h3><p>设D是一个非空数集，如果对于D中的每一个数x，按照某种对应法则f，都有唯一确定的数y与之对应，则称y是x的函数，记作y=f(x)。</p><h3>1.1.2 定义域和值域</h3><p>定义域是自变量x的取值范围，值域是因变量y的取值范围。</p><h3>1.1.3 函数的表示方法</h3><p>函数可以用解析式、图像、表格等方式表示。</p>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903152605_5_110.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=1a33844050667bcb236c97d8b36b6c52609e4dc489da04700ee6e265617ef9fe",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E7%94%A8%E6%88%B7%E4%B8%AD%E5%BF%83.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=c5bda626c8180390e8ccb894c128dce8a5f4c913544117fb71ca79da4bb6ff7f",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251001183400_907_138.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=ffbbb05b76df5f3a0ed640d6e760ec6a163551caaaba6eb5252f0acc9d7cbebf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903153836_6_110.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=b91265e39809570e13f3c951c101cf08e4d5b47e508a406d94fff13f117e3ef8",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%B1%8F%E5%B9%95%E5%BD%95%E5%88%B6%202025-09-28%20154745.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T125530Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=f73bd59924fb6183494367fcbc06ebda973154bdba44e6808868aa3db860b8e1"
    ],
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Long("450"),
    like_count: Long("32"),
    create_time: ISODate("2024-01-15T09:30:00.000Z"),
    update_time: ISODate("2025-10-04T12:55:34.385Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter"
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e04",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "1.2 函数的性质",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    description: "掌握函数的单调性、奇偶性、周期性等基本性质",
    content: "<h3>1.2.1 单调性</h3><p>函数在某个区间内单调递增或单调递减的性质。</p><h3>1.2.2 奇偶性</h3><p>奇函数满足f(-x)=-f(x)，偶函数满足f(-x)=f(x)。</p><h3>1.2.3 周期性</h3><p>如果存在正数T，使得f(x+T)=f(x)对所有x成立，则称f(x)是周期函数。</p>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%AE%9E%E9%AA%8C%E5%85%AB%20Spring%20Boot%E6%95%B0%E6%8D%AE%E5%BA%93%E6%93%8D%E4%BD%9C.doc?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T110913Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=17a43dc9ef4684d648d95e15dafad75609fe4bbf9f82df256b05c7b08bb5bb51"
    ],
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Long("380"),
    like_count: Long("28"),
    create_time: ISODate("2024-01-15T10:00:00.000Z"),
    update_time: ISODate("2025-10-04T11:09:15.799Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter"
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e05",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "1.3 极限的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    description: "理解极限的严格定义和几何意义",
    content: "<h3>1.3.1 极限的直观定义</h3><p>当x无限接近a时，f(x)无限接近某个常数A。</p><h3>1.3.2 极限的严格定义</h3><p>ε-δ定义：对于任意ε&gt;0，存在δ&gt;0，使得当0&lt;|x-a|&lt;δ时，|f(x)-A|&lt;ε。</p><h3>1.3.3 左极限和右极限</h3><p>分别考虑x从左侧和右侧趋近于a时的极限。</p>",
    attachment_urls: [],
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Long("420"),
    like_count: Long("29"),
    create_time: ISODate("2024-01-15T10:30:00.000Z"),
    update_time: ISODate("2025-10-04T08:12:25.809Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter"
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e06",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "1.4 极限的运算法则",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    description: "掌握极限的四则运算法则和复合函数极限",
    content: "<h3>1.4.1 四则运算法则</h3><p>极限的和、差、积、商的运算法则。</p><h3>1.4.2 复合函数极限</h3><p>复合函数的极限运算法则。</p><h3>1.4.3 夹逼定理</h3><p>利用夹逼定理求极限的重要方法。</p>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/Java%E5%AE%9E%E9%AA%8C%E6%8A%A5%E5%91%8A_%E8%B5%B5%E7%9B%9B.doc?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T152734Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=62f9d6830a13eb4e89e6194dfdfb8048224d46b93de5bcab3539c43b99e87a3e",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%AE%9E%E9%AA%8C8%EF%BC%9AGUI%EF%BC%881%EF%BC%89.pptx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T152735Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=5e0febad6545c4de3a80bd515925bdf90adafb2cd96af7d589b5ea9dc17fbe8b",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E6%9F%A5%E6%89%BE%E6%8E%92%E5%BA%8F%28%E8%B5%B5%E7%9B%9B%20202300501414%29.docx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T153539Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=a8a7d115e0be6a738dc6d0e1d97b693b1fc4110cf6123a0a90fecbe84b8b8b52",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/student.txt?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T154103Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=7b662aa29006cd50d3a06d6f91050f1cfa916a3df07fbcc13980bf765dd97070",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/readme.xlsx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T154812Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=3ba0626d774a0d91a4ec4a7cfc27c307651bf3e9cb31e85fe9807ab37e3384ce",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/04%E3%80%81%E7%BB%BC%E5%90%88%E6%B5%8B%E8%AF%84%E8%AE%A1%E7%AE%97%E8%A1%A8%E6%A0%BC.xlsx?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T161151Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=06ed3f2822123c60ca6bf4e40083399961c4974f13d9aea1bf63b18b81351b05"
    ],
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Long("350"),
    like_count: Long("25"),
    create_time: ISODate("2024-01-15T11:00:00.000Z"),
    update_time: ISODate("2025-10-04T16:11:53.166Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter"
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e07",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第二章 导数与微分",
    parent_chapter_id: null,
    description: "掌握导数的定义、几何意义、计算方法以及微分的概念和应用",
    content: "<h2>2.1 导数的定义</h2><p>导数描述函数在某点的瞬时变化率，是微积分的核心概念。导数的定义基于极限概念，表示函数值随自变量变化的快慢程度。</p><h2>2.2 导数的几何意义</h2><p>导数的几何意义是函数曲线在某点处的切线斜率，这为理解函数的局部性质提供了直观的几何解释。</p><h2>2.3 导数的计算</h2><p>掌握基本初等函数的导数公式、导数的四则运算法则、复合函数求导法则（链式法则）以及隐函数求导方法。</p><h2>2.4 高阶导数</h2><p>高阶导数是导数的导数，在物理学和工程学中有重要应用，如加速度是速度的导数，速度是位移的导数。</p><h2>2.5 微分</h2><p>微分是导数的另一种表达形式，在近似计算和误差分析中有重要应用。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("1180"),
    like_count: Int32("95"),
    create_time: ISODate("2024-01-16T09:00:00.000Z"),
    update_time: ISODate("2024-01-16T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e08",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "2.1 导数的定义",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e07",
    description: "理解导数的定义和几何意义",
    content: "<h3>2.1.1 导数的定义</h3><p>f'(x) = lim[h→0] [f(x+h)-f(x)]/h</p><h3>2.1.2 导数的几何意义</h3><p>导数表示函数曲线在某点处的切线斜率。</p><h3>2.1.3 导数的物理意义</h3><p>导数表示瞬时变化率，如速度是位移的导数。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("420"),
    like_count: Int32("35"),
    create_time: ISODate("2024-01-16T09:30:00.000Z"),
    update_time: ISODate("2024-01-16T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e09",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "2.2 导数的计算",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e07",
    description: "掌握基本初等函数的导数公式",
    content: "<h3>2.2.1 基本初等函数的导数</h3><p>幂函数、指数函数、对数函数、三角函数的导数公式。</p><h3>2.2.2 导数的四则运算法则</h3><p>和、差、积、商的导数运算法则。</p><h3>2.2.3 复合函数求导法则</h3><p>链式法则：d/dx[f(g(x))] = f'(g(x))·g'(x)。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("380"),
    like_count: Int32("32"),
    create_time: ISODate("2024-01-16T10:00:00.000Z"),
    update_time: ISODate("2024-01-16T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e10",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "2.3 高阶导数",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e07",
    description: "学习高阶导数的概念和计算",
    content: "<h3>2.3.1 高阶导数的定义</h3><p>二阶导数、三阶导数等概念。</p><h3>2.3.2 高阶导数的计算</h3><p>逐次求导的方法。</p><h3>2.3.3 高阶导数的应用</h3><p>在物理学和工程学中的应用。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-16T10:30:00.000Z"),
    update_time: ISODate("2024-01-16T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e11",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "2.4 微分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e07",
    description: "理解微分的概念和应用",
    content: "<h3>2.4.1 微分的定义</h3><p>dy = f'(x)dx，微分是导数的另一种表达形式。</p><h3>2.4.2 微分的几何意义</h3><p>微分表示函数增量的线性主部。</p><h3>2.4.3 微分的应用</h3><p>在近似计算和误差分析中的应用。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("18"),
    create_time: ISODate("2024-01-16T11:00:00.000Z"),
    update_time: ISODate("2024-01-16T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e12",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第三章 导数的应用",
    parent_chapter_id: null,
    description: "学习导数在函数性质研究、最值问题、曲线描绘等方面的应用",
    content: "<h2>3.1 函数的单调性</h2><p>利用导数判断函数的单调性：当f'(x)>0时，函数单调递增；当f'(x)<0时，函数单调递减。</p><h2>3.2 函数的极值</h2><p>极值是函数在局部范围内的最大值或最小值。通过求导并令导数为零，可以找到可能的极值点。</p><h2>3.3 函数的最值</h2><p>最值问题是导数应用的重要领域，包括闭区间上的最值问题和实际应用中的优化问题。</p><h2>3.4 曲线的凹凸性</h2><p>利用二阶导数判断曲线的凹凸性，当f''(x)>0时曲线凹向上，当f''(x)<0时曲线凹向下。</p><h2>3.5 拐点</h2><p>拐点是曲线凹凸性发生改变的点，在函数图像分析中具有重要意义。</p><h2>3.6 渐近线</h2><p>渐近线包括水平渐近线、垂直渐近线和斜渐近线，是描绘函数图像的重要工具。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("1080"),
    like_count: Int32("78"),
    create_time: ISODate("2024-01-17T09:00:00.000Z"),
    update_time: ISODate("2024-01-17T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e13",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "3.1 函数的单调性",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e12",
    description: "利用导数判断函数的单调性",
    content: "<h3>3.1.1 单调性的判定</h3><p>当f'(x)>0时，函数单调递增；当f'(x)<0时，函数单调递减。</p><h3>3.1.2 单调区间的求法</h3><p>通过求导数的符号变化来确定单调区间。</p><h3>3.1.3 应用实例</h3><p>利用单调性解决实际问题。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("350"),
    like_count: Int32("28"),
    create_time: ISODate("2024-01-17T09:30:00.000Z"),
    update_time: ISODate("2024-01-17T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e14",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "3.2 函数的极值",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e12",
    description: "学习函数极值的概念和求法",
    content: "<h3>3.2.1 极值的定义</h3><p>函数在局部范围内的最大值或最小值。</p><h3>3.2.2 极值的必要条件</h3><p>可导函数在极值点处导数必为零。</p><h3>3.2.3 极值的充分条件</h3><p>利用一阶导数或二阶导数判断极值。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("25"),
    create_time: ISODate("2024-01-17T10:00:00.000Z"),
    update_time: ISODate("2024-01-17T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e15",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "3.3 函数的最值",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e12",
    description: "掌握最值问题的求解方法",
    content: "<h3>3.3.1 闭区间上的最值</h3><p>在闭区间上连续函数必有最大值和最小值。</p><h3>3.3.2 最值的求法</h3><p>比较函数在驻点、端点处的函数值。</p><h3>3.3.3 实际应用问题</h3><p>利用最值理论解决优化问题。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-17T10:30:00.000Z"),
    update_time: ISODate("2024-01-17T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e16",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "3.4 曲线的凹凸性",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e12",
    description: "利用二阶导数判断曲线的凹凸性",
    content: "<h3>3.4.1 凹凸性的定义</h3><p>曲线向上凸或向下凸的性质。</p><h3>3.4.2 凹凸性的判定</h3><p>当f''(x)>0时曲线凹向上，当f''(x)<0时曲线凹向下。</p><h3>3.4.3 拐点</h3><p>曲线凹凸性发生改变的点。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-17T11:00:00.000Z"),
    update_time: ISODate("2024-01-17T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e17",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第四章 不定积分",
    parent_chapter_id: null,
    description: "掌握不定积分的概念、性质和基本积分方法",
    content: "<h2>4.1 不定积分的概念</h2><p>不定积分是导数的逆运算，表示所有导数为给定函数的函数族。如果F'(x)=f(x)，则F(x)是f(x)的一个原函数。</p><h2>4.2 基本积分公式</h2><p>掌握基本初等函数的不定积分公式，包括幂函数、指数函数、三角函数、对数函数等的积分。</p><h2>4.3 积分的性质</h2><p>不定积分具有线性性质，即∫[af(x)+bg(x)]dx = a∫f(x)dx + b∫g(x)dx。</p><h2>4.4 换元积分法</h2><p>换元积分法是不定积分的重要方法，包括第一类换元法（凑微分法）和第二类换元法。</p><h2>4.5 分部积分法</h2><p>分部积分法适用于被积函数是两个函数乘积的情况，公式为∫udv = uv - ∫vdu。</p><h2>4.6 有理函数的积分</h2><p>有理函数的积分通过部分分式分解等方法求解，是积分计算中的重要技巧。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("980"),
    like_count: Int32("72"),
    comment_count: Int32("25"),
    create_time: ISODate("2024-01-18T09:00:00.000Z"),
    update_time: ISODate("2024-01-18T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e18",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "4.1 不定积分的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e17",
    description: "理解不定积分的定义和基本概念",
    content: "<h3>4.1.1 原函数的概念</h3><p>如果F'(x)=f(x)，则F(x)是f(x)的一个原函数。</p><h3>4.1.2 不定积分的定义</h3><p>∫f(x)dx = F(x) + C，其中C是任意常数。</p><h3>4.1.3 不定积分的几何意义</h3><p>不定积分表示一族平行曲线。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("24"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-18T09:30:00.000Z"),
    update_time: ISODate("2024-01-18T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e19",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "4.2 基本积分公式",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e17",
    description: "掌握基本初等函数的积分公式",
    content: "<h3>4.2.1 幂函数的积分</h3><p>∫x^n dx = x^(n+1)/(n+1) + C (n≠-1)</p><h3>4.2.2 指数函数的积分</h3><p>∫e^x dx = e^x + C, ∫a^x dx = a^x/ln(a) + C</p><h3>4.2.3 三角函数的积分</h3><p>∫sin(x)dx = -cos(x) + C, ∫cos(x)dx = sin(x) + C</p><h3>4.2.4 对数函数的积分</h3><p>∫1/x dx = ln|x| + C</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("380"),
    like_count: Int32("28"),
    create_time: ISODate("2024-01-18T10:00:00.000Z"),
    update_time: ISODate("2024-01-18T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e20",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "4.3 换元积分法",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e17",
    description: "掌握第一类和第二类换元积分法",
    content: "<h3>4.3.1 第一类换元法（凑微分法）</h3><p>∫f(g(x))g'(x)dx = ∫f(u)du，其中u=g(x)。</p><h3>4.3.2 第二类换元法</h3><p>通过变量替换简化积分计算。</p><h3>4.3.3 三角换元</h3><p>利用三角函数进行变量替换。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("350"),
    like_count: Int32("26"),
    comment_count: Int32("7"),
    create_time: ISODate("2024-01-18T10:30:00.000Z"),
    update_time: ISODate("2024-01-18T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e21",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "4.4 分部积分法",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e17",
    description: "掌握分部积分法的应用",
    content: "<h3>4.4.1 分部积分公式</h3><p>∫udv = uv - ∫vdu</p><h3>4.4.2 分部积分的应用</h3><p>适用于被积函数是两个函数乘积的情况。</p><h3>4.4.3 循环积分</h3><p>某些积分需要多次使用分部积分法。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("5"),
    create_time: ISODate("2024-01-18T11:00:00.000Z"),
    update_time: ISODate("2024-01-18T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e22",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第五章 定积分",
    parent_chapter_id: null,
    description: "学习定积分的定义、性质、计算方法和几何意义",
    content: "<h2>5.1 定积分的定义</h2><p>定积分是黎曼积分的概念，通过分割、近似、求和、取极限的过程定义。定积分∫[a,b]f(x)dx表示函数f(x)在区间[a,b]上的积分。</p><h2>5.2 定积分的性质</h2><p>定积分具有线性性质、区间可加性、单调性等基本性质，这些性质为定积分的计算提供了理论基础。</p><h2>5.3 微积分基本定理</h2><p>微积分基本定理建立了导数与积分之间的联系，包括第一基本定理和第二基本定理。</p><h2>5.4 定积分的计算</h2><p>利用牛顿-莱布尼茨公式计算定积分，结合换元积分法和分部积分法求解复杂的定积分问题。</p><h2>5.5 定积分的几何意义</h2><p>定积分在几何上表示曲线与坐标轴围成的面积，这为理解积分的几何意义提供了直观的解释。</p><h2>5.6 定积分的应用</h2><p>定积分在计算面积、体积、弧长等几何量以及解决物理问题中有重要应用。</p>",
    sort_order: Int32("5"),
    status: Int32("1"),
    view_count: Int32("920"),
    like_count: Int32("85"),
    comment_count: Int32("28"),
    create_time: ISODate("2024-01-19T09:00:00.000Z"),
    update_time: ISODate("2024-01-19T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e23",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "5.1 定积分的定义",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e22",
    description: "理解定积分的黎曼定义",
    content: "<h3>5.1.1 黎曼和</h3><p>通过分割、近似、求和、取极限的过程定义定积分。</p><h3>5.1.2 定积分的几何意义</h3><p>定积分表示曲线与坐标轴围成的面积。</p><h3>5.1.3 定积分的物理意义</h3><p>在物理学中表示累积量，如位移、功等。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("24"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-19T09:30:00.000Z"),
    update_time: ISODate("2024-01-19T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e24",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "5.2 定积分的性质",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e22",
    description: "掌握定积分的基本性质",
    content: "<h3>5.2.1 线性性质</h3><p>∫[a,b][αf(x)+βg(x)]dx = α∫[a,b]f(x)dx + β∫[a,b]g(x)dx</p><h3>5.2.2 区间可加性</h3><p>∫[a,b]f(x)dx = ∫[a,c]f(x)dx + ∫[c,b]f(x)dx</p><h3>5.2.3 单调性</h3><p>如果f(x)≤g(x)，则∫[a,b]f(x)dx ≤ ∫[a,b]g(x)dx</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-19T10:00:00.000Z"),
    update_time: ISODate("2024-01-19T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e25",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "5.3 微积分基本定理",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e22",
    description: "理解微积分基本定理的重要性",
    content: "<h3>5.3.1 第一基本定理</h3><p>如果F(x)=∫[a,x]f(t)dt，则F'(x)=f(x)。</p><h3>5.3.2 第二基本定理</h3><p>∫[a,b]f(x)dx = F(b) - F(a)，其中F'(x)=f(x)。</p><h3>5.3.3 牛顿-莱布尼茨公式</h3><p>建立了导数与积分之间的联系。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("350"),
    like_count: Int32("28"),
    create_time: ISODate("2024-01-19T10:30:00.000Z"),
    update_time: ISODate("2024-01-19T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e26",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "5.4 定积分的应用",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e22",
    description: "学习定积分在几何和物理中的应用",
    content: "<h3>5.4.1 计算面积</h3><p>利用定积分计算平面图形的面积。</p><h3>5.4.2 计算体积</h3><p>利用定积分计算旋转体的体积。</p><h3>5.4.3 计算弧长</h3><p>利用定积分计算曲线的弧长。</p><h3>5.4.4 物理应用</h3><p>在物理学中计算功、质心等。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-19T11:00:00.000Z"),
    update_time: ISODate("2024-01-19T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e27",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第六章 多元函数微分学",
    parent_chapter_id: null,
    description: "学习多元函数的概念、偏导数、全微分以及多元函数的极值问题",
    content: "<h2>6.1 多元函数的概念</h2><p>多元函数是定义在n维空间上的函数，最常见的是二元函数z=f(x,y)。多元函数在几何上表示空间中的曲面。</p><h2>6.2 偏导数</h2><p>偏导数是多元函数对某个变量的导数，其他变量视为常数。偏导数∂f/∂x表示函数f对x的偏导数。</p><h2>6.3 全微分</h2><p>全微分是多元函数增量的线性主部，dz = (∂f/∂x)dx + (∂f/∂y)dy。全微分在近似计算中有重要应用。</p><h2>6.4 复合函数求导</h2><p>多元复合函数的求导法则，包括链式法则在多元函数中的应用。</p><h2>6.5 隐函数求导</h2><p>隐函数是由方程F(x,y)=0确定的函数关系，通过隐函数求导法则可以求出dy/dx。</p><h2>6.6 多元函数的极值</h2><p>多元函数的极值问题包括无条件极值和条件极值，拉格朗日乘数法是求解条件极值的重要方法。</p>",
    sort_order: Int32("6"),
    status: Int32("1"),
    view_count: Int32("850"),
    like_count: Int32("68"),
    comment_count: Int32("22"),
    create_time: ISODate("2024-01-20T09:00:00.000Z"),
    update_time: ISODate("2024-01-20T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e28",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "6.1 多元函数的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e27",
    description: "理解多元函数的定义和几何意义",
    content: "<h3>6.1.1 二元函数的定义</h3><p>z=f(x,y)表示定义在平面区域D上的函数。</p><h3>6.1.2 多元函数的几何意义</h3><p>二元函数在几何上表示空间中的曲面。</p><h3>6.1.3 多元函数的极限</h3><p>多元函数极限的概念和性质。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-20T09:30:00.000Z"),
    update_time: ISODate("2024-01-20T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e29",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "6.2 偏导数",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e27",
    description: "掌握偏导数的概念和计算",
    content: "<h3>6.2.1 偏导数的定义</h3><p>∂f/∂x = lim[h→0][f(x+h,y)-f(x,y)]/h</p><h3>6.2.2 偏导数的几何意义</h3><p>偏导数表示曲面在某个方向上的切线斜率。</p><h3>6.2.3 高阶偏导数</h3><p>二阶偏导数、混合偏导数等概念。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-20T10:00:00.000Z"),
    update_time: ISODate("2024-01-20T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e30",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "6.3 全微分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e27",
    description: "理解全微分的概念和应用",
    content: "<h3>6.3.1 全微分的定义</h3><p>dz = (∂f/∂x)dx + (∂f/∂y)dy</p><h3>6.3.2 全微分的几何意义</h3><p>全微分表示函数增量的线性主部。</p><h3>6.3.3 全微分的应用</h3><p>在近似计算和误差分析中的应用。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("250"),
    like_count: Int32("18"),
    create_time: ISODate("2024-01-20T10:30:00.000Z"),
    update_time: ISODate("2024-01-20T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e31",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "6.4 多元函数的极值",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e27",
    description: "学习多元函数的极值问题",
    content: "<h3>6.4.1 无条件极值</h3><p>多元函数在定义域内的极值问题。</p><h3>6.4.2 条件极值</h3><p>在约束条件下的极值问题。</p><h3>6.4.3 拉格朗日乘数法</h3><p>求解条件极值的重要方法。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("25"),
    create_time: ISODate("2024-01-20T11:00:00.000Z"),
    update_time: ISODate("2024-01-20T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e32",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第七章 重积分",
    parent_chapter_id: null,
    description: "掌握二重积分和三重积分的概念、性质、计算方法和应用",
    content: "<h2>7.1 二重积分的概念</h2><p>二重积分是定积分在二维空间的推广，表示函数在平面区域上的积分。二重积分∬D f(x,y)dxdy表示函数f(x,y)在区域D上的积分。</p><h2>7.2 二重积分的性质</h2><p>二重积分具有线性性质、区域可加性、单调性等基本性质，这些性质为二重积分的计算提供了理论基础。</p><h2>7.3 二重积分的计算</h2><p>二重积分的计算通常通过化为累次积分进行，包括直角坐标系下的计算和极坐标系下的计算。</p><h2>7.4 二重积分的应用</h2><p>二重积分在计算平面图形的面积、曲面的面积、物体的质量、重心等几何和物理量中有重要应用。</p><h2>7.5 三重积分</h2><p>三重积分是定积分在三维空间的推广，表示函数在空间区域上的积分。三重积分∭V f(x,y,z)dxdydz表示函数f(x,y,z)在区域V上的积分。</p><h2>7.6 三重积分的计算</h2><p>三重积分的计算通过化为累次积分进行，包括直角坐标系、柱坐标系和球坐标系下的计算。</p>",
    sort_order: Int32("7"),
    status: Int32("1"),
    view_count: Int32("780"),
    like_count: Int32("62"),
    comment_count: Int32("18"),
    create_time: ISODate("2024-01-21T09:00:00.000Z"),
    update_time: ISODate("2024-01-21T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e33",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "7.1 二重积分的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e32",
    description: "理解二重积分的定义和几何意义",
    content: "<h3>7.1.1 二重积分的定义</h3><p>∬D f(x,y)dxdy表示函数f(x,y)在区域D上的积分。</p><h3>7.1.2 二重积分的几何意义</h3><p>二重积分表示曲面与坐标平面围成的体积。</p><h3>7.1.3 二重积分的性质</h3><p>线性性质、区域可加性等基本性质。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-21T09:30:00.000Z"),
    update_time: ISODate("2024-01-21T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e34",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "7.2 二重积分的计算",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e32",
    description: "掌握二重积分的计算方法",
    content: "<h3>7.2.1 直角坐标系下的计算</h3><p>化为累次积分进行计算。</p><h3>7.2.2 极坐标系下的计算</h3><p>利用极坐标变换简化计算。</p><h3>7.2.3 积分次序的交换</h3><p>交换积分次序的技巧。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("24"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-21T10:00:00.000Z"),
    update_time: ISODate("2024-01-21T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e35",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "7.3 三重积分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e32",
    description: "学习三重积分的概念和计算",
    content: "<h3>7.3.1 三重积分的定义</h3><p>∭V f(x,y,z)dxdydz表示函数f(x,y,z)在区域V上的积分。</p><h3>7.3.2 直角坐标系下的计算</h3><p>化为累次积分进行计算。</p><h3>7.3.3 柱坐标系和球坐标系</h3><p>利用不同坐标系简化计算。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("260"),
    like_count: Int32("18"),
    create_time: ISODate("2024-01-21T10:30:00.000Z"),
    update_time: ISODate("2024-01-21T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e36",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "7.4 重积分的应用",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e32",
    description: "学习重积分在几何和物理中的应用",
    content: "<h3>7.4.1 计算面积和体积</h3><p>利用重积分计算平面图形的面积和空间立体的体积。</p><h3>7.4.2 计算质量</h3><p>利用重积分计算物体的质量。</p><h3>7.4.3 计算重心</h3><p>利用重积分计算物体的重心坐标。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("240"),
    like_count: Int32("16"),
    comment_count: Int32("3"),
    create_time: ISODate("2024-01-21T11:00:00.000Z"),
    update_time: ISODate("2024-01-21T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e37",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第八章 曲线积分与曲面积分",
    parent_chapter_id: null,
    description: "学习曲线积分和曲面积分的概念、性质、计算方法和物理意义",
    content: "<h2>8.1 对弧长的曲线积分</h2><p>对弧长的曲线积分是函数沿曲线的积分，表示函数在曲线上的积分。第一类曲线积分∫L f(x,y)ds表示函数f(x,y)沿曲线L的积分。</p><h2>8.2 对坐标的曲线积分</h2><p>对坐标的曲线积分是向量场沿曲线的积分，在物理学中有重要应用。第二类曲线积分∫L Pdx + Qdy表示向量场(P,Q)沿曲线L的积分。</p><h2>8.3 格林公式</h2><p>格林公式建立了平面区域上的二重积分与边界曲线上的曲线积分之间的关系，是曲线积分理论的重要定理。</p><h2>8.4 对面积的曲面积分</h2><p>对面积的曲面积分是函数在曲面上的积分，表示函数在曲面上的积分。第一类曲面积分∬Σ f(x,y,z)dS表示函数f(x,y,z)在曲面Σ上的积分。</p><h2>8.5 对坐标的曲面积分</h2><p>对坐标的曲面积分是向量场通过曲面的积分，在电磁学中有重要应用。第二类曲面积分∬Σ Pdydz + Qdzdx + Rdxdy表示向量场(P,Q,R)通过曲面Σ的积分。</p><h2>8.6 高斯公式和斯托克斯公式</h2><p>高斯公式建立了空间区域上的三重积分与边界曲面上的曲面积分之间的关系；斯托克斯公式建立了曲面上的曲面积分与边界曲线上的曲线积分之间的关系。</p>",
    sort_order: Int32("8"),
    status: Int32("1"),
    view_count: Int32("720"),
    like_count: Int32("58"),
    comment_count: Int32("15"),
    create_time: ISODate("2024-01-22T09:00:00.000Z"),
    update_time: ISODate("2024-01-22T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e38",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "8.1 对弧长的曲线积分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e37",
    description: "学习第一类曲线积分的概念和计算",
    content: "<h3>8.1.1 第一类曲线积分的定义</h3><p>∫L f(x,y)ds表示函数f(x,y)沿曲线L的积分。</p><h3>8.1.2 第一类曲线积分的计算</h3><p>化为定积分进行计算。</p><h3>8.1.3 第一类曲线积分的应用</h3><p>计算曲线的质量、重心等。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("250"),
    like_count: Int32("18"),
    create_time: ISODate("2024-01-22T09:30:00.000Z"),
    update_time: ISODate("2024-01-22T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e39",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "8.2 对坐标的曲线积分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e37",
    description: "学习第二类曲线积分的概念和计算",
    content: "<h3>8.2.1 第二类曲线积分的定义</h3><p>∫L Pdx + Qdy表示向量场(P,Q)沿曲线L的积分。</p><h3>8.2.2 第二类曲线积分的计算</h3><p>化为定积分进行计算。</p><h3>8.2.3 第二类曲线积分的应用</h3><p>在物理学中计算功、环流等。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-22T10:00:00.000Z"),
    update_time: ISODate("2024-01-22T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e40",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "8.3 格林公式",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e37",
    description: "掌握格林公式及其应用",
    content: "<h3>8.3.1 格林公式的表述</h3><p>∬D (∂Q/∂x - ∂P/∂y)dxdy = ∮L Pdx + Qdy</p><h3>8.3.2 格林公式的应用</h3><p>简化曲线积分的计算。</p><h3>8.3.3 平面区域的面积</h3><p>利用格林公式计算平面区域的面积。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-22T10:30:00.000Z"),
    update_time: ISODate("2024-01-22T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e41",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "8.4 曲面积分",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e37",
    description: "学习曲面积分的概念和计算",
    content: "<h3>8.4.1 对面积的曲面积分</h3><p>∬Σ f(x,y,z)dS表示函数f(x,y,z)在曲面Σ上的积分。</p><h3>8.4.2 对坐标的曲面积分</h3><p>∬Σ Pdydz + Qdzdx + Rdxdy表示向量场通过曲面的积分。</p><h3>8.4.3 高斯公式和斯托克斯公式</h3><p>建立重积分与曲面积分之间的关系。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("24"),
    comment_count: Int32("7"),
    create_time: ISODate("2024-01-22T11:00:00.000Z"),
    update_time: ISODate("2024-01-22T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e42",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "第九章 无穷级数",
    parent_chapter_id: null,
    description: "掌握无穷级数的概念、收敛性判别法、幂级数和傅里叶级数",
    content: "<h2>9.1 无穷级数的概念</h2><p>无穷级数是无穷多个数的和，是数学分析中的重要概念。级数∑(n=1 to ∞)an的收敛性通过部分和序列的极限来定义。</p><h2>9.2 级数的基本性质</h2><p>级数具有线性性质、收敛级数的必要条件等基本性质。如果级数收敛，则其通项趋于零。</p><h2>9.3 正项级数的收敛性判别法</h2><p>正项级数的收敛性判别法包括比较判别法、比值判别法、根值判别法、积分判别法等。</p><h2>9.4 交错级数</h2><p>交错级数是正负项交替出现的级数，莱布尼茨判别法是判断交错级数收敛性的重要方法。</p><h2>9.5 幂级数</h2><p>幂级数是形如∑(n=0 to ∞)an(x-a)^n的级数，在函数展开和近似计算中有重要应用。</p><h2>9.6 函数的幂级数展开</h2><p>许多函数可以展开为幂级数，包括泰勒级数和麦克劳林级数，这为函数的近似计算提供了重要工具。</p><h2>9.7 傅里叶级数</h2><p>傅里叶级数是周期函数的三角级数展开，在信号处理、物理学等领域有重要应用。</p>",
    sort_order: Int32("9"),
    status: Int32("1"),
    view_count: Int32("680"),
    like_count: Int32("55"),
    comment_count: Int32("20"),
    create_time: ISODate("2024-01-23T09:00:00.000Z"),
    update_time: ISODate("2024-01-23T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e43",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "9.1 无穷级数的概念",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e42",
    description: "理解无穷级数的定义和收敛性",
    content: "<h3>9.1.1 无穷级数的定义</h3><p>∑(n=1 to ∞)an = a1 + a2 + a3 + ...</p><h3>9.1.2 级数的收敛性</h3><p>通过部分和序列的极限来定义级数的收敛性。</p><h3>9.1.3 级数的基本性质</h3><p>收敛级数的必要条件、线性性质等。</p>",
    sort_order: Int32("1"),
    status: Int32("1"),
    view_count: Int32("280"),
    like_count: Int32("20"),
    create_time: ISODate("2024-01-23T09:30:00.000Z"),
    update_time: ISODate("2024-01-23T09:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e44",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "9.2 正项级数的收敛性判别法",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e42",
    description: "掌握正项级数的各种收敛性判别法",
    content: "<h3>9.2.1 比较判别法</h3><p>通过比较两个级数来判断收敛性。</p><h3>9.2.2 比值判别法</h3><p>利用lim(n→∞)|an+1/an|来判断收敛性。</p><h3>9.2.3 根值判别法</h3><p>利用lim(n→∞)ⁿ√|an|来判断收敛性。</p><h3>9.2.4 积分判别法</h3><p>利用积分来判断级数的收敛性。</p>",
    sort_order: Int32("2"),
    status: Int32("1"),
    view_count: Int32("320"),
    like_count: Int32("24"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-23T10:00:00.000Z"),
    update_time: ISODate("2024-01-23T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e45",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "9.3 幂级数",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e42",
    description: "学习幂级数的概念和性质",
    content: "<h3>9.3.1 幂级数的定义</h3><p>∑(n=0 to ∞)an(x-a)^n形式的级数。</p><h3>9.3.2 收敛半径</h3><p>幂级数的收敛半径和收敛区间。</p><h3>9.3.3 幂级数的性质</h3><p>幂级数的和函数、逐项求导、逐项积分等。</p>",
    sort_order: Int32("3"),
    status: Int32("1"),
    view_count: Int32("300"),
    like_count: Int32("22"),
    comment_count: Int32("6"),
    create_time: ISODate("2024-01-23T10:30:00.000Z"),
    update_time: ISODate("2024-01-23T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_chapter").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03e46",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    chapter_name: "9.4 函数的幂级数展开",
    parent_chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e42",
    description: "学习函数的泰勒级数和麦克劳林级数展开",
    content: "<h3>9.4.1 泰勒级数</h3><p>f(x) = ∑(n=0 to ∞)[f^(n)(a)/n!](x-a)^n</p><h3>9.4.2 麦克劳林级数</h3><p>当a=0时的泰勒级数。</p><h3>9.4.3 常用函数的幂级数展开</h3><p>e^x、sin(x)、cos(x)、ln(1+x)等的幂级数展开。</p><h3>9.4.4 傅里叶级数</h3><p>周期函数的三角级数展开。</p>",
    sort_order: Int32("4"),
    status: Int32("1"),
    view_count: Int32("350"),
    like_count: Int32("26"),
    create_time: ISODate("2024-01-23T11:00:00.000Z"),
    update_time: ISODate("2024-01-23T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_course_forum
// ----------------------------
db.getCollection("mg_course_forum").drop();
db.createCollection("mg_course_forum");
db.getCollection("mg_course_forum").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_course_forum").createIndex({
    forum_type: Int32("1")
}, {
    name: "forum_type_1"
});
db.getCollection("mg_course_forum").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_course_forum").createIndex({
    sort_order: Int32("1")
}, {
    name: "sort_order_1"
});

// ----------------------------
// Documents of mg_course_forum
// ----------------------------
db.getCollection("mg_course_forum").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    forum_name: "高等数学讨论区",
    description: "高等数学课程学习讨论区，同学们可以在这里交流学习心得、讨论问题",
    forum_type: Int32("0"),
    is_public: Int32("0"),
    allow_anonymous: Int32("0"),
    moderator_ids: [
        "01983258-afb0-79b3-847b-0abf6a991c88"
    ],
    post_count: Int32("15"),
    reply_count: Int32("45"),
    last_post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    last_post_time: "2024-01-20T14:30:00.000Z",
    sort_order: Int32("1"),
    status: Int32("0"),
    rules: "1. 请保持文明用语，尊重他人\n2. 发帖前请先搜索是否已有类似问题\n3. 问题描述要清晰，便于他人理解和回答\n4. 禁止发布与课程无关的内容",
    tags: [
        "高等数学",
        "讨论",
        "学习交流"
    ],
    create_time: ISODate("2024-01-15T08:00:00.000Z"),
    update_time: ISODate("2024-01-20T14:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_forum").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03f02",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    forum_name: "高等数学问答区",
    description: "专门用于提问和回答高等数学相关问题的区域",
    forum_type: Int32("1"),
    is_public: Int32("0"),
    allow_anonymous: Int32("1"),
    moderator_ids: [
        "01983258-afb0-79b3-847b-0abf6a991c88",
        "01992cb7-735d-7857-9c8c-edbe566ad0d2"
    ],
    post_count: Int32("8"),
    reply_count: Int32("23"),
    last_post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a08",
    last_post_time: "2024-01-19T16:45:00.000Z",
    sort_order: Int32("2"),
    status: Int32("0"),
    rules: "1. 提问时请详细描述问题背景和具体疑问\n2. 回答时请提供详细的解题步骤\n3. 对于好的回答，请及时采纳\n4. 鼓励大家互相帮助，共同进步",
    tags: [
        "高等数学",
        "问答",
        "问题求助"
    ],
    create_time: ISODate("2024-01-15T08:30:00.000Z"),
    update_time: ISODate("2024-01-19T16:45:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_forum").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03f03",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    forum_name: "高等数学作业区",
    description: "发布和讨论高等数学作业的区域",
    forum_type: Int32("2"),
    is_public: Int32("0"),
    allow_anonymous: Int32("0"),
    moderator_ids: [
        "01983258-afb0-79b3-847b-0abf6a991c88"
    ],
    post_count: Int32("5"),
    reply_count: Int32("12"),
    last_post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a12",
    last_post_time: "2024-01-18T10:20:00.000Z",
    sort_order: Int32("3"),
    status: Int32("0"),
    rules: "1. 作业发布后请及时完成\n2. 可以讨论作业中的难点问题\n3. 禁止直接发布作业答案\n4. 鼓励分享解题思路和方法",
    tags: [
        "高等数学",
        "作业",
        "练习"
    ],
    create_time: ISODate("2024-01-15T09:00:00.000Z"),
    update_time: ISODate("2024-01-18T10:20:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_forum").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03f04",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    forum_name: "课程公告区",
    description: "发布课程相关通知和公告的区域",
    forum_type: Int32("3"),
    is_public: Int32("0"),
    allow_anonymous: Int32("0"),
    moderator_ids: [
        "01983258-afb0-79b3-847b-0abf6a991c88"
    ],
    post_count: Int32("3"),
    reply_count: Int32("0"),
    last_post_id: "78d44b4a-becd-4f65-9461-f2dcdda03f15",
    last_post_time: "2024-01-17T09:00:00.000Z",
    sort_order: Int32("0"),
    status: Int32("0"),
    rules: "1. 仅教师和管理员可以发布公告\n2. 学生可以查看和回复公告\n3. 重要公告会置顶显示",
    tags: [
        "高等数学",
        "公告",
        "通知"
    ],
    create_time: ISODate("2024-01-15T07:30:00.000Z"),
    update_time: ISODate("2024-01-17T09:00:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_course_question_bank
// ----------------------------
db.getCollection("mg_course_question_bank").drop();
db.createCollection("mg_course_question_bank");
db.getCollection("mg_course_question_bank").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    sys_user_id: Int32("1")
}, {
    name: "sys_user_id_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    bank_type: Int32("1")
}, {
    name: "bank_type_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    difficulty: Int32("1")
}, {
    name: "difficulty_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    is_public: Int32("1")
}, {
    name: "is_public_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    tags: Int32("1")
}, {
    name: "tags_1"
});
db.getCollection("mg_course_question_bank").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});

// ----------------------------
// Documents of mg_course_question_bank
// ----------------------------
db.getCollection("mg_course_question_bank").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440001",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    bank_name: "高等数学基础题库",
    description: "高等数学基础概念和计算题目的综合题库，涵盖函数、极限、导数等基础内容。",
    bank_type: Int32("0"),
    difficulty: Int32("1"),
    is_public: Int32("1"),
    tags: [
        "高等数学",
        "基础",
        "函数",
        "极限"
    ],
    create_time: ISODate("2024-01-10T10:00:00.000Z"),
    update_time: ISODate("2024-01-10T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_question_bank").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440002",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    bank_name: "微积分进阶题库",
    description: "微积分进阶题目集合，包含积分、微分方程、级数等高级内容。",
    bank_type: Int32("1"),
    tags: [
        "微积分",
        "积分",
        "微分方程",
        "级数"
    ],
    difficulty: Int32("3"),
    is_public: Int32("1"),
    create_time: ISODate("2024-01-15T14:30:00.000Z"),
    update_time: ISODate("2025-10-14T16:08:24.865Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.CourseQuestionBank"
}]);
db.getCollection("mg_course_question_bank").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440003",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    bank_name: "期末考试题库",
    description: "期末考试专用题库，综合各章节重点难点题目。",
    bank_type: Int32("2"),
    difficulty: Int32("2"),
    is_public: Int32("0"),
    tags: [
        "期末考试",
        "综合",
        "重点"
    ],
    create_time: ISODate("2024-01-20T09:15:00.000Z"),
    update_time: ISODate("2024-01-20T09:15:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_course_task
// ----------------------------
db.getCollection("mg_course_task").drop();
db.createCollection("mg_course_task");
db.getCollection("mg_course_task").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_course_task").createIndex({
    sysUserId_id: Int32("1")
}, {
    name: "sysUserId_id_1"
});
db.getCollection("mg_course_task").createIndex({
    task_type: Int32("1")
}, {
    name: "task_type_1"
});
db.getCollection("mg_course_task").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_course_task").createIndex({
    start_time: Int32("1")
}, {
    name: "start_time_1"
});
db.getCollection("mg_course_task").createIndex({
    end_time: Int32("1")
}, {
    name: "end_time_1"
});
db.getCollection("mg_course_task").createIndex({
    difficulty: Int32("1")
}, {
    name: "difficulty_1"
});
db.getCollection("mg_course_task").createIndex({
    tags: Int32("1")
}, {
    name: "tags_1"
});
db.getCollection("mg_course_task").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});
db.getCollection("mg_course_task").createIndex({
    view_count: Int32("-1")
}, {
    name: "view_count_-1"
});

// ----------------------------
// Documents of mg_course_task
// ----------------------------
db.getCollection("mg_course_task").insert([{
    _id: "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    task_name: "高等数学第一章作业 - 函数与极限",
    description: "完成第一章函数与极限的相关练习题，包括函数概念、极限定义、极限运算法则等内容。",
    task_type: Int32("0"),
    task_content: "<h3>第一章 函数与极限</h3><p>请完成以下题目：</p><ol><li><strong>函数概念题</strong><br/>求函数 f(x) = √(x²-4) 的定义域和值域</li><li><strong>极限计算题</strong><br/>计算下列极限：<br/>a) lim(x→0) sin(x)/x<br/>b) lim(x→∞) (x²+1)/(2x²-3x+1)</li><li><strong>极限证明题</strong><br/>用ε-δ定义证明 lim(x→2) (3x-1) = 5</li><li><strong>连续性问题</strong><br/>讨论函数 f(x) = {x², x≤1; 2x-1, x>1} 在 x=1 处的连续性</li></ol>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/calculus_chapter1_homework.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_examples.docx"
    ],
    resource_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/calculus_textbook_chapter1.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/limit_concepts_video.mp4",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/practice_problems.pdf"
    ],
    max_score: Int32("100"),
    start_time: ISODate("2024-01-15T08:00:00.000Z"),
    end_time: ISODate("2024-01-22T23:59:59.000Z"),
    allow_late_submit: Int32("1"),
    max_submit_count: Int32("3"),
    auto_grade: Int32("0"),
    tags: [
        "高等数学",
        "函数",
        "极限",
        "作业",
        "第一章"
    ],
    difficulty: Int32("2"),
    estimated_time: Int32("120"),
    view_count: Int32("0"),
    status: Int32("1"),
    create_time: ISODate("2024-01-10T10:00:00.000Z"),
    update_time: ISODate("2024-01-10T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_task").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440001",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    task_name: "导数与微分在线测验",
    description: "测试学生对导数概念、求导法则和微分应用的掌握程度。",
    task_type: Int32("1"),
    task_content: "<h3>导数与微分测验</h3><p><strong>时间限制：60分钟</strong></p><p>请回答以下选择题和计算题：</p><ol><li>函数 f(x) = x³ 的导数是？<br/>A) 3x² B) x² C) 3x D) x³</li><li>求函数 f(x) = sin(x)cos(x) 的导数</li><li>求函数 f(x) = ln(x²+1) 的导数</li><li>求函数 f(x) = e^(2x) 的二阶导数</li><li>求函数 f(x) = x²+3x+2 在 x=1 处的微分</li></ol>",
    attachment_urls: [],
    resource_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/derivative_rules.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/chain_rule_examples.mp4"
    ],
    max_score: Int32("50"),
    start_time: ISODate("2024-01-25T09:00:00.000Z"),
    end_time: ISODate("2024-01-25T10:00:00.000Z"),
    allow_late_submit: Int32("0"),
    max_submit_count: Int32("1"),
    auto_grade: Int32("1"),
    tags: [
        "高等数学",
        "导数",
        "微分",
        "测验"
    ],
    difficulty: Int32("2"),
    estimated_time: Int32("60"),
    view_count: Int32("0"),
    status: Int32("1"),
    create_time: ISODate("2024-01-20T14:30:00.000Z"),
    update_time: ISODate("2024-01-20T14:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_task").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440002",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    task_name: "积分应用数学建模项目",
    description: "运用积分知识解决实际问题的数学建模项目，培养数学应用能力。",
    task_type: Int32("2"),
    task_content: "<h3>积分应用数学建模项目</h3><p><strong>项目要求：</strong></p><ol><li><strong>问题选择</strong><br/>从以下主题中选择一个进行建模：<br/>• 人口增长模型<br/>• 经济增长模型<br/>• 物理运动问题<br/>• 工程优化问题</li><li><strong>模型建立</strong><br/>• 建立微分方程模型<br/>• 运用积分方法求解<br/>• 分析模型参数</li><li><strong>结果分析</strong><br/>• 数值计算和图形展示<br/>• 模型验证和误差分析<br/>• 实际意义解释</li><li><strong>报告撰写</strong><br/>• 完整的数学推导过程<br/>• 清晰的图表和计算<br/>• 结论和建议</li></ol>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/modeling_guidelines.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/integration_examples.zip",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/report_template.docx"
    ],
    resource_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/mathematical_modeling.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/integration_applications.mp4",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/matlab_tutorial.pdf"
    ],
    max_score: Int32("150"),
    start_time: ISODate("2024-02-01T08:00:00.000Z"),
    end_time: ISODate("2024-03-15T23:59:59.000Z"),
    allow_late_submit: Int32("1"),
    max_submit_count: Int32("2"),
    auto_grade: Int32("0"),
    tags: [
        "高等数学",
        "积分",
        "数学建模",
        "项目",
        "应用"
    ],
    difficulty: Int32("3"),
    estimated_time: Int32("1440"),
    view_count: Int32("0"),
    status: Int32("1"),
    create_time: ISODate("2024-01-25T16:00:00.000Z"),
    update_time: ISODate("2024-01-25T16:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_task").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440003",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    task_name: "多元函数微分学实验",
    description: "通过计算机实验探索多元函数的偏导数、全微分和极值问题。",
    task_type: Int32("3"),
    task_content: "<h3>多元函数微分学实验</h3><p><strong>实验目标：</strong>通过MATLAB或Python编程，研究多元函数的微分性质</p><p><strong>实验内容：</strong></p><ol><li><strong>偏导数计算</strong><br/>• 计算函数 f(x,y) = x²y + xy² 的一阶和二阶偏导数<br/>• 验证混合偏导数相等性</li><li><strong>全微分研究</strong><br/>• 计算函数 f(x,y) = sin(xy) 的全微分<br/>• 分析全微分的几何意义</li><li><strong>极值问题</strong><br/>• 求函数 f(x,y) = x³ + y³ - 3xy 的极值点<br/>• 绘制函数图形和等高线图</li><li><strong>条件极值</strong><br/>• 在约束条件 x² + y² = 1 下求 f(x,y) = xy 的极值<br/>• 使用拉格朗日乘数法</li></ol><p><strong>实验报告要求：</strong></p><ul><li>完整的代码和运行结果</li><li>数学推导过程</li><li>图形分析和解释</li><li>实验心得和思考</li></ul>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/matlab_scripts.zip",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/experiment_template.m"
    ],
    resource_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/multivariable_calculus.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/matlab_tutorial.mp4",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/3d_plotting_guide.pdf"
    ],
    max_score: Int32("80"),
    start_time: ISODate("2024-02-10T08:00:00.000Z"),
    end_time: ISODate("2024-02-17T23:59:59.000Z"),
    allow_late_submit: Int32("1"),
    max_submit_count: Int32("2"),
    auto_grade: Int32("0"),
    tags: [
        "高等数学",
        "多元函数",
        "偏导数",
        "实验",
        "MATLAB"
    ],
    difficulty: Int32("2"),
    estimated_time: Int32("180"),
    view_count: Int32("0"),
    status: Int32("1"),
    create_time: ISODate("2024-02-05T11:20:00.000Z"),
    update_time: ISODate("2024-02-05T11:20:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_course_task").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440004",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "0197ee62-be08-7c57-b1ff-42b1fa3c8b3f",
    task_name: "级数收敛性分析作业",
    description: "分析各种级数的收敛性，掌握级数收敛的判别方法。",
    task_type: Int32("0"),
    task_content: "<h3>级数收敛性分析</h3><p>请判断下列级数的收敛性，并说明理由：</p><ol><li>∑(n=1 to ∞) 1/n²</li><li>∑(n=1 to ∞) (-1)ⁿ/n</li><li>∑(n=1 to ∞) n!/nⁿ</li><li>∑(n=1 to ∞) (2n)!/(n!)²</li><li>∑(n=1 to ∞) sin(n)/n²</li></ol><p><strong>要求：</strong></p><ul><li>使用适当的收敛判别法</li><li>详细说明判断过程</li><li>如果收敛，求其和（如果可能）</li><li>如果发散，说明发散类型</li></ul>",
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/series_convergence_guide.pdf"
    ],
    resource_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/series_convergence.pdf",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/resources/convergence_tests.mp4"
    ],
    max_score: Int32("100"),
    start_time: ISODate("2024-02-20T08:00:00.000Z"),
    end_time: ISODate("2024-02-27T23:59:59.000Z"),
    allow_late_submit: Int32("1"),
    max_submit_count: Int32("3"),
    auto_grade: Int32("0"),
    tags: [
        "高等数学",
        "级数",
        "收敛性",
        "作业"
    ],
    difficulty: Int32("3"),
    estimated_time: Int32("150"),
    view_count: Int32("0"),
    status: Int32("0"),
    create_time: ISODate("2024-02-15T09:45:00.000Z"),
    update_time: ISODate("2024-02-15T09:45:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_forum_post
// ----------------------------
db.getCollection("mg_forum_post").drop();
db.createCollection("mg_forum_post");
db.getCollection("mg_forum_post").createIndex({
    forum_id: Int32("1")
}, {
    name: "forum_id_1"
});
db.getCollection("mg_forum_post").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_forum_post").createIndex({
    author_id: Int32("1")
}, {
    name: "author_id_1"
});
db.getCollection("mg_forum_post").createIndex({
    post_type: Int32("1")
}, {
    name: "post_type_1"
});
db.getCollection("mg_forum_post").createIndex({
    is_top: Int32("1"),
    create_time: Int32("-1")
}, {
    name: "is_top_1_create_time_-1"
});
db.getCollection("mg_forum_post").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_forum_post").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});
db.getCollection("mg_forum_post").createIndex({
    tags: Int32("1")
}, {
    name: "tags_1"
});

// ----------------------------
// Documents of mg_forum_post
// ----------------------------
db.getCollection("mg_forum_post").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    title: "极限计算中的常见错误分析",
    content: "在学习极限的过程中，我发现很多同学容易犯一些常见的错误。今天想和大家分享一下这些错误以及正确的解题方法。\n\n**常见错误1：直接代入法使用不当**\n很多同学在计算极限时，直接代入x的值，但忽略了某些情况下直接代入会导致分母为0的情况。\n\n**常见错误2：洛必达法则使用条件不满足**\n洛必达法则有严格的使用条件，必须是0/0型或∞/∞型的不定式，且分子分母都可导。\n\n**常见错误3：等价无穷小替换错误**\n在使用等价无穷小替换时，要注意替换的时机和条件。\n\n大家在学习过程中还遇到过哪些问题呢？欢迎分享！",
    post_type: Int32("1"),
    is_anonymous: Int32("0"),
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_common_errors.pdf"
    ],
    image_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/images/limit_error_example1.png",
        "http://127.0.0.1:31589/sapientiacloud-edupivot/images/limit_error_example2.png"
    ],
    tags: [
        "极限",
        "错误分析",
        "学习方法"
    ],
    view_count: Long("663"),
    like_count: Long("23"),
    reply_count: Long("53"),
    share_count: Long("14"),
    is_top: Int32("0"),
    is_essence: Int32("0"),
    is_locked: Int32("0"),
    last_reply_id: "78d44b4a-becd-4f65-9461-f2dcdda03b08",
    last_reply_time: "2024-01-20T14:30:00.000Z",
    last_reply_user_id: "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
    status: Int32("0"),
    chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    create_time: ISODate("2024-01-16T10:00:00.000Z"),
    update_time: ISODate("2025-10-15T07:35:04.629Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost"
}]);
db.getCollection("mg_forum_post").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03a02",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
    title: "导数与微分的几何意义理解",
    content: "最近在学习导数与微分这一章，对几何意义有些困惑。导数的几何意义是函数在某点的切线斜率，这个我能理解。但是微分的几何意义是什么呢？\n\n从几何上看，微分dy表示的是什么呢？是切线的增量吗？还是其他什么？\n\n另外，在实际应用中，什么时候用导数，什么时候用微分呢？\n\n希望有同学能帮忙解释一下，最好能结合图形来说明。",
    post_type: Int32("0"),
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    tags: [
        "导数",
        "微分",
        "几何意义"
    ],
    view_count: Long("170"),
    like_count: Long("12"),
    reply_count: Long("5"),
    share_count: Long("2"),
    is_top: Int32("1"),
    is_essence: Int32("0"),
    is_locked: Int32("0"),
    last_reply_id: "78d44b4a-becd-4f65-9461-f2dcdda03b12",
    last_reply_time: "2024-01-19T15:20:00.000Z",
    last_reply_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    status: Int32("0"),
    chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e05",
    create_time: ISODate("2024-01-17T14:30:00.000Z"),
    update_time: ISODate("2025-10-15T07:35:12.368Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost"
}]);
db.getCollection("mg_forum_post").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03a08",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f02",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    title: "如何计算这个极限？lim(x→0) (sin x - x) / x³",
    content: "题目：计算极限 lim(x→0) (sin x - x) / x³\n\n我尝试了几种方法：\n1. 直接代入：得到0/0型\n2. 洛必达法则：分子分母求导后还是0/0型\n3. 泰勒展开：sin x = x - x³/6 + x⁵/120 - ...\n\n用泰勒展开：\nsin x - x = -x³/6 + x⁵/120 - ...\n所以原式 = lim(x→0) (-x³/6 + x⁵/120 - ...) / x³\n= lim(x→0) (-1/6 + x²/120 - ...)\n= -1/6\n\n请问这个解法对吗？还有其他方法吗？",
    post_type: Int32("0"),
    is_anonymous: Int32("0"),
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/attachments/limit_problem.pdf"
    ],
    image_urls: [],
    tags: [
        "极限",
        "洛必达法则",
        "泰勒展开"
    ],
    view_count: Int32("237"),
    like_count: Int32("18"),
    reply_count: Int32("6"),
    share_count: Int32("3"),
    is_top: Int32("0"),
    is_essence: Int32("0"),
    is_locked: Int32("0"),
    last_reply_id: "78d44b4a-becd-4f65-9461-f2dcdda03b15",
    last_reply_time: "2024-01-19T16:45:00.000Z",
    last_reply_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    status: Int32("0"),
    chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    create_time: ISODate("2024-01-18T11:15:00.000Z"),
    update_time: ISODate("2024-01-19T16:45:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_forum_post").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03a12",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f03",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    title: "第一章作业：极限计算练习",
    content: "**作业内容：**\n请完成以下极限计算题：\n\n1. lim(x→2) (x² - 4) / (x - 2)\n2. lim(x→0) (1 - cos x) / x²\n3. lim(x→∞) (2x² + 3x + 1) / (3x² - 2x + 5)\n4. lim(x→0) (e^x - 1) / x\n5. lim(x→1) (x³ - 1) / (x² - 1)\n\n**要求：**\n- 每题都要写出详细的解题过程\n- 使用多种方法验证答案\n- 下周一前提交\n\n**提交方式：**\n将作业拍照或扫描后上传到课程平台",
    post_type: Int32("0"),
    is_anonymous: Int32("0"),
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903152605_5_110.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=1a33844050667bcb236c97d8b36b6c52609e4dc489da04700ee6e265617ef9fe"
    ],
    image_urls: [],
    tags: [
        "作业",
        "极限",
        "第一章"
    ],
    view_count: Int32("147"),
    like_count: Int32("8"),
    reply_count: Int32("4"),
    share_count: Int32("1"),
    is_top: Int32("0"),
    is_essence: Int32("0"),
    is_locked: Int32("0"),
    last_reply_id: "78d44b4a-becd-4f65-9461-f2dcdda03b18",
    last_reply_time: "2024-01-18T10:20:00.000Z",
    last_reply_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    status: Int32("0"),
    chapter_id: "78d44b4a-becd-4f65-9461-f2dcdda03e02",
    create_time: ISODate("2024-01-15T16:00:00.000Z"),
    update_time: ISODate("2024-01-18T10:20:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_forum_post").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03f15",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f04",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    title: "【重要通知】期中考试安排",
    content: "**期中考试通知**\n\n**考试时间：** 2024年2月15日 上午9:00-11:00\n**考试地点：** 教学楼A101、A102、A103\n**考试范围：** 第一章至第三章（函数与极限、导数与微分、积分）\n\n**注意事项：**\n1. 请携带学生证和身份证\n2. 考试期间禁止使用手机和计算器\n3. 提前15分钟到达考场\n4. 考试形式为闭卷笔试\n\n**复习建议：**\n- 重点复习基本概念和定理\n- 多做练习题，特别是历年真题\n- 注意解题步骤的规范性\n\n如有疑问，请及时联系老师。祝大家考试顺利！",
    post_type: Int32("0"),
    is_anonymous: Int32("1"),
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903152605_5_110.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=1a33844050667bcb236c97d8b36b6c52609e4dc489da04700ee6e265617ef9fe"
    ],
    image_urls: [],
    tags: [
        "通知",
        "期中考试",
        "重要"
    ],
    view_count: Long("327"),
    like_count: Long("15"),
    reply_count: Long("0"),
    share_count: Long("9"),
    is_top: Int32("1"),
    is_essence: Int32("1"),
    is_locked: Int32("0"),
    status: Int32("0"),
    create_time: ISODate("2024-01-17T09:00:00.000Z"),
    update_time: ISODate("2025-10-10T05:57:14.533Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost"
}]);

// ----------------------------
// Collection structure for mg_forum_reply
// ----------------------------
db.getCollection("mg_forum_reply").drop();
db.createCollection("mg_forum_reply");
db.getCollection("mg_forum_reply").createIndex({
    post_id: Int32("1")
}, {
    name: "post_id_1"
});
db.getCollection("mg_forum_reply").createIndex({
    forum_id: Int32("1")
}, {
    name: "forum_id_1"
});
db.getCollection("mg_forum_reply").createIndex({
    course_id: Int32("1")
}, {
    name: "course_id_1"
});
db.getCollection("mg_forum_reply").createIndex({
    author_id: Int32("1")
}, {
    name: "author_id_1"
});
db.getCollection("mg_forum_reply").createIndex({
    parent_reply_id: Int32("1")
}, {
    name: "parent_reply_id_1"
});
db.getCollection("mg_forum_reply").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_forum_reply").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});
db.getCollection("mg_forum_reply").createIndex({
    floor_number: Int32("1")
}, {
    name: "floor_number_1"
});

// ----------------------------
// Documents of mg_forum_reply
// ----------------------------
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b01",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    content: "<p>总结得很好！我补充一点，在计算极限时还要注意左右极限是否相等，特别是分段函数和含有绝对值的函数。</p>",
    reply_to_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Long("5"),
    reply_count: Long("2"),
    is_accepted: Int32("0"),
    floor_number: Int32("1"),
    status: Int32("0"),
    ip_address: "192.168.1.100",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-16T11:30:00.000Z"),
    update_time: ISODate("2025-10-10T06:02:46.062Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply"
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b02",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
    content: "老师说得对！我经常忘记考虑左右极限，导致答案错误。",
    parent_reply_id: "78d44b4a-becd-4f65-9461-f2dcdda03b01",
    reply_to_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Int32("3"),
    reply_count: Int32("0"),
    is_accepted: Int32("0"),
    floor_number: Int32("2"),
    status: Int32("0"),
    ip_address: "192.168.1.101",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-16T12:00:00.000Z"),
    update_time: ISODate("2024-01-16T12:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b03",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    content: "还有一个常见错误是混淆了无穷大和无穷小，比如把lim(x→0) 1/x当作无穷小，实际上当x→0⁺时，1/x→+∞。",
    reply_to_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Long("9"),
    reply_count: Long("1"),
    is_accepted: Int32("0"),
    floor_number: Int32("3"),
    status: Int32("0"),
    ip_address: "192.168.1.102",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-16T14:15:00.000Z"),
    update_time: ISODate("2025-10-10T05:42:59.494Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply"
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b12",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a02",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    content: "很好的问题！微分的几何意义可以这样理解：\n\n**微分的几何意义：**\n微分dy表示的是函数y=f(x)在点x处的切线在x方向上的增量dx对应的y方向上的增量。\n\n**具体来说：**\n- 当x有一个很小的增量Δx时，函数值的变化Δy ≈ dy = f'(x)Δx\n- 从几何上看，dy就是切线在x方向移动Δx时，y方向的变化量\n- 而Δy是函数曲线在x方向移动Δx时，y方向的实际变化量\n\n**应用区别：**\n- 导数主要用于求切线斜率、判断单调性、求极值等\n- 微分主要用于近似计算、误差估计等\n\n建议你画个图来理解，这样会更直观！",
    reply_to_user_id: "01992cb6-d9a7-7841-9e2b-9dcc1c9ea0a7",
    is_anonymous: Int32("0"),
    attachment_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/course-chapters/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250903152605_5_110.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=root%2F20251004%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20251004T103547Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=1a33844050667bcb236c97d8b36b6c52609e4dc489da04700ee6e265617ef9fe"
    ],
    image_urls: [
        "http://127.0.0.1:31589/sapientiacloud-edupivot/images/differential_geometry_diagram.png"
    ],
    like_count: Long("15"),
    reply_count: Long("2"),
    is_accepted: Int32("1"),
    floor_number: Int32("1"),
    status: Int32("0"),
    ip_address: "192.168.1.100",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-19T15:20:00.000Z"),
    update_time: ISODate("2025-10-10T04:59:01.181Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply"
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b15",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a08",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f02",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    content: "你的解法完全正确！泰勒展开是解决这类问题的标准方法。\n\n**验证过程：**\n使用洛必达法则也可以得到相同结果：\n\n第一次洛必达：\nlim(x→0) (cos x - 1) / (3x²) = 0/0\n\n第二次洛必达：\nlim(x→0) (-sin x) / (6x) = 0/0\n\n第三次洛必达：\nlim(x→0) (-cos x) / 6 = -1/6\n\n**其他方法：**\n还可以使用等价无穷小：\nsin x - x = -x³/6 + o(x³)\n所以原式 = lim(x→0) (-x³/6 + o(x³)) / x³ = -1/6\n\n你的泰勒展开方法是最直观的，很好！",
    parent_reply_id: null,
    reply_to_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Int32("12"),
    reply_count: Int32("1"),
    is_accepted: Int32("1"),
    floor_number: Int32("1"),
    status: Int32("0"),
    ip_address: "192.168.1.100",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-19T16:45:00.000Z"),
    update_time: ISODate("2024-01-19T16:45:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "78d44b4a-becd-4f65-9461-f2dcdda03b18",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a12",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f03",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    content: "老师，第2题我用了洛必达法则，但是分子分母求导后还是0/0型，应该怎么处理？",
    parent_reply_id: null,
    reply_to_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Int32("3"),
    reply_count: Int32("1"),
    is_accepted: Int32("0"),
    floor_number: Int32("1"),
    status: Int32("0"),
    ip_address: "192.168.1.102",
    user_agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    create_time: ISODate("2024-01-18T10:20:00.000Z"),
    update_time: ISODate("2024-01-18T10:20:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_forum_reply").insert([{
    _id: "0199cdf5-fa53-72ca-9db7-688043e7fa6d",
    post_id: "78d44b4a-becd-4f65-9461-f2dcdda03a01",
    forum_id: "78d44b4a-becd-4f65-9461-f2dcdda03f01",
    course_id: "78d44b4a-becd-4f65-9461-f2dcdda03e01",
    sys_user_id: "01983258-afb0-79b3-847b-0abf6a991c88",
    content: "<p>总结得很好！我补充一点，在计算极限时还要注意左右极限是否相等，特别是分段函数和含有绝对值的函数。</p>",
    reply_to_user_id: "01992cb7-735d-7857-9c8c-edbe566ad0d2",
    is_anonymous: Int32("0"),
    attachment_urls: [],
    image_urls: [],
    like_count: Long("0"),
    reply_count: Long("0"),
    is_accepted: Int32("0"),
    floor_number: Int32("46"),
    status: Int32("0"),
    create_time: ISODate("2025-10-10T11:51:13.749Z"),
    update_time: ISODate("2025-10-10T11:51:13.749Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply"
}]);

// ----------------------------
// Collection structure for mg_question
// ----------------------------
db.getCollection("mg_question").drop();
db.createCollection("mg_question");
db.getCollection("mg_question").createIndex({
    question_bank_id: Int32("1")
}, {
    name: "question_bank_id_1"
});
db.getCollection("mg_question").createIndex({
    sys_user_id: Int32("1")
}, {
    name: "sys_user_id_1"
});
db.getCollection("mg_question").createIndex({
    question_type: Int32("1")
}, {
    name: "question_type_1"
});
db.getCollection("mg_question").createIndex({
    difficulty: Int32("1")
}, {
    name: "difficulty_1"
});
db.getCollection("mg_question").createIndex({
    status: Int32("1")
}, {
    name: "status_1"
});
db.getCollection("mg_question").createIndex({
    tags: Int32("1")
}, {
    name: "tags_1"
});
db.getCollection("mg_question").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});
db.getCollection("mg_question").createIndex({
    view_count: Int32("-1")
}, {
    name: "view_count_-1"
});
db.getCollection("mg_question").createIndex({
    score: Int32("1")
}, {
    name: "score_1"
});

// ----------------------------
// Documents of mg_question
// ----------------------------
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440101",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440001",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "函数定义域计算",
    question_content: "求函数 f(x) = √(x²-4) 的定义域。",
    question_type: Int32("0"),
    difficulty: Int32("1"),
    score: Int32("5"),
    status: Int32("1"),
    tags: [
        "函数",
        "定义域",
        "根号函数"
    ],
    view_count: Int32("6"),
    create_time: ISODate("2024-01-10T10:30:00.000Z"),
    update_time: ISODate("2024-01-10T10:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440102",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440001",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "极限计算",
    question_content: "计算极限 lim(x→0) sin(x)/x",
    question_type: Int32("1"),
    difficulty: Int32("2"),
    score: Int32("8"),
    status: Int32("1"),
    tags: [
        "极限",
        "三角函数",
        "重要极限"
    ],
    view_count: Int32("5"),
    create_time: ISODate("2024-01-10T11:00:00.000Z"),
    update_time: ISODate("2024-01-10T11:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440103",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440001",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "导数计算",
    question_content: "求函数 f(x) = x³ + 2x² - 5x + 3 的导数。",
    question_type: Int32("2"),
    difficulty: Int32("1"),
    score: Int32("6"),
    status: Int32("1"),
    tags: [
        "导数",
        "多项式",
        "基本求导"
    ],
    view_count: Int32("11"),
    create_time: ISODate("2024-01-10T11:30:00.000Z"),
    update_time: ISODate("2024-01-10T11:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440104",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440002",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "不定积分计算",
    question_content: "计算不定积分 ∫(x² + 3x + 2)dx",
    question_type: Int32("2"),
    difficulty: Int32("2"),
    score: "10",
    tags: [
        "积分",
        "不定积分",
        "多项式积分"
    ],
    view_count: Long("180"),
    status: Int32("1"),
    create_time: ISODate("2024-01-15T15:00:00.000Z"),
    update_time: ISODate("2025-10-15T04:05:34.62Z"),
    is_deleted: Int32("0"),
    _class: "com.dayz.sapientiacloud_edupivot.course.entity.po.Question"
}]);
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440105",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440002",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "级数收敛性判断",
    question_content: "判断级数 ∑(n=1 to ∞) 1/n² 的收敛性。",
    question_type: Int32("2"),
    difficulty: Int32("3"),
    score: Int32("12"),
    status: Int32("1"),
    tags: [
        "级数",
        "收敛性",
        "p级数"
    ],
    view_count: Int32("1142"),
    create_time: ISODate("2024-01-15T15:30:00.000Z"),
    update_time: ISODate("2024-01-15T15:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440106",
    question_bank_id: "550e8400-e29b-41d4-a716-446655440003",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    question_title: "综合应用题",
    question_content: "已知函数 f(x) = x³ - 3x + 1，求：(1) 函数的单调区间；(2) 函数的极值；(3) 函数在区间[-2,2]上的最大值和最小值。",
    question_type: Int32("2"),
    difficulty: Int32("3"),
    score: Int32("20"),
    status: Int32("1"),
    tags: [
        "综合应用",
        "单调性",
        "极值",
        "最值"
    ],
    view_count: Int32("2"),
    create_time: ISODate("2024-01-20T10:00:00.000Z"),
    update_time: ISODate("2024-01-20T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_question_answer
// ----------------------------
db.getCollection("mg_question_answer").drop();
db.createCollection("mg_question_answer");
db.getCollection("mg_question_answer").createIndex({
    question_id: Int32("1")
}, {
    name: "question_id_1"
});
db.getCollection("mg_question_answer").createIndex({
    sys_user_id: Int32("1")
}, {
    name: "sys_user_id_1"
});
db.getCollection("mg_question_answer").createIndex({
    is_correct: Int32("1")
}, {
    name: "is_correct_1"
});
db.getCollection("mg_question_answer").createIndex({
    score: Int32("1")
}, {
    name: "score_1"
});
db.getCollection("mg_question_answer").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});

// ----------------------------
// Documents of mg_question_answer
// ----------------------------
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440301",
    question_id: "550e8400-e29b-41d4-a716-446655440101",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "A",
    is_correct: Int32("1"),
    score: Int32("5"),
    create_time: ISODate("2024-01-12T14:30:00.000Z"),
    update_time: ISODate("2024-01-12T14:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440302",
    question_id: "550e8400-e29b-41d4-a716-446655440102",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "1",
    is_correct: Int32("1"),
    score: Int32("8"),
    create_time: ISODate("2024-01-12T15:00:00.000Z"),
    update_time: ISODate("2024-01-12T15:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440303",
    question_id: "550e8400-e29b-41d4-a716-446655440103",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "f'(x) = 3x² + 4x - 5",
    is_correct: Int32("1"),
    score: Int32("6"),
    create_time: ISODate("2024-01-12T15:30:00.000Z"),
    update_time: ISODate("2024-01-12T15:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440304",
    question_id: "550e8400-e29b-41d4-a716-446655440104",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "∫(x² + 3x + 2)dx = x³/3 + 3x²/2 + 2x + C",
    is_correct: Int32("1"),
    score: Int32("10"),
    create_time: ISODate("2024-01-18T16:00:00.000Z"),
    update_time: ISODate("2024-01-18T16:00:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440305",
    question_id: "550e8400-e29b-41d4-a716-446655440105",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "该级数收敛，因为它是p级数，p=2>1，所以收敛。",
    is_correct: Int32("1"),
    score: Int32("12"),
    create_time: ISODate("2024-01-18T16:30:00.000Z"),
    update_time: ISODate("2024-01-18T16:30:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_answer").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440306",
    question_id: "550e8400-e29b-41d4-a716-446655440106",
    sys_user_id: "0198086c-fcd1-7a09-b3c3-537de3493332",
    answer_content: "(1) 单调递增区间：(-∞,-1)∪(1,+∞)，单调递减区间：(-1,1)；(2) 极大值：f(-1)=3，极小值：f(1)=-1；(3) 最大值：f(-2)=3，最小值：f(1)=-1",
    is_correct: Int32("1"),
    score: Int32("20"),
    create_time: ISODate("2024-01-22T10:00:00.000Z"),
    update_time: ISODate("2024-01-22T10:00:00.000Z"),
    is_deleted: Int32("0")
}]);

// ----------------------------
// Collection structure for mg_question_option
// ----------------------------
db.getCollection("mg_question_option").drop();
db.createCollection("mg_question_option");
db.getCollection("mg_question_option").createIndex({
    question_id: Int32("1")
}, {
    name: "question_id_1"
});
db.getCollection("mg_question_option").createIndex({
    is_correct: Int32("1")
}, {
    name: "is_correct_1"
});
db.getCollection("mg_question_option").createIndex({
    option_label: Int32("1")
}, {
    name: "option_label_1"
});
db.getCollection("mg_question_option").createIndex({
    create_time: Int32("-1")
}, {
    name: "create_time_-1"
});

// ----------------------------
// Documents of mg_question_option
// ----------------------------
db.getCollection("mg_question_option").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440201",
    question_id: "550e8400-e29b-41d4-a716-446655440101",
    option_content: "x ∈ (-∞, -2] ∪ [2, +∞)",
    option_label: "A",
    is_correct: Int32("1"),
    create_time: ISODate("2024-01-10T10:35:00.000Z"),
    update_time: ISODate("2024-01-10T10:35:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_option").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440202",
    question_id: "550e8400-e29b-41d4-a716-446655440101",
    option_content: "x ∈ [-2, 2]",
    option_label: "B",
    is_correct: Int32("0"),
    create_time: ISODate("2024-01-10T10:35:00.000Z"),
    update_time: ISODate("2024-01-10T10:35:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_option").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440203",
    question_id: "550e8400-e29b-41d4-a716-446655440101",
    option_content: "x ∈ (-∞, -2) ∪ (2, +∞)",
    option_label: "C",
    is_correct: Int32("0"),
    create_time: ISODate("2024-01-10T10:35:00.000Z"),
    update_time: ISODate("2024-01-10T10:35:00.000Z"),
    is_deleted: Int32("0")
}]);
db.getCollection("mg_question_option").insert([{
    _id: "550e8400-e29b-41d4-a716-446655440204",
    question_id: "550e8400-e29b-41d4-a716-446655440101",
    option_content: "x ∈ R",
    option_label: "D",
    is_correct: Int32("0"),
    create_time: ISODate("2024-01-10T10:35:00.000Z"),
    update_time: ISODate("2024-01-10T10:35:00.000Z"),
    is_deleted: Int32("0")
}]);
