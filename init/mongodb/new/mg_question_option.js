// mg_question_option.js - 题目选项数据（约30条题目的选项，每个题目4个选项）
db.getCollection("mg_question_option").insertMany([
    {
      _id: "550e8400-e29b-41d4-a716-446655440201",
      question_id: "550e8400-e29b-41d4-a716-446655440101",
      option_content: "x ∈ (-∞, -2] ∪ [2, +∞)",
      option_label: "A",
      is_correct: 1,
      create_time: ISODate("2024-01-10T10:35:00.000Z"),
      update_time: ISODate("2024-01-10T10:35:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440202",
      question_id: "550e8400-e29b-41d4-a716-446655440101",
      option_content: "x ∈ [-2, 2]",
      option_label: "B",
      is_correct: 0,
      create_time: ISODate("2024-01-10T10:35:00.000Z"),
      update_time: ISODate("2024-01-10T10:35:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440203",
      question_id: "550e8400-e29b-41d4-a716-446655440101",
      option_content: "x ∈ (-∞, -2) ∪ (2, +∞)",
      option_label: "C",
      is_correct: 0,
      create_time: ISODate("2024-01-10T10:35:00.000Z"),
      update_time: ISODate("2024-01-10T10:35:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440204",
      question_id: "550e8400-e29b-41d4-a716-446655440101",
      option_content: "x ∈ R",
      option_label: "D",
      is_correct: 0,
      create_time: ISODate("2024-01-10T10:35:00.000Z"),
      update_time: ISODate("2024-01-10T10:35:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440205",
      question_id: "550e8400-e29b-41d4-a716-446655440107",
      option_content: "RSA",
      option_label: "A",
      is_correct: 0,
      create_time: ISODate("2024-02-01T09:05:00.000Z"),
      update_time: ISODate("2024-02-01T09:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440206",
      question_id: "550e8400-e29b-41d4-a716-446655440107",
      option_content: "AES",
      option_label: "B",
      is_correct: 1,
      create_time: ISODate("2024-02-01T09:05:00.000Z"),
      update_time: ISODate("2024-02-01T09:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440207",
      question_id: "550e8400-e29b-41d4-a716-446655440107",
      option_content: "SHA-256",
      option_label: "C",
      is_correct: 0,
      create_time: ISODate("2024-02-01T09:05:00.000Z"),
      update_time: ISODate("2024-02-01T09:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440208",
      question_id: "550e8400-e29b-41d4-a716-446655440107",
      option_content: "MD5",
      option_label: "D",
      is_correct: 0,
      create_time: ISODate("2024-02-01T09:05:00.000Z"),
      update_time: ISODate("2024-02-01T09:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440209",
      question_id: "550e8400-e29b-41d4-a716-446655440108",
      option_content: "数据加密",
      option_label: "A",
      is_correct: 0,
      create_time: ISODate("2024-02-01T10:05:00.000Z"),
      update_time: ISODate("2024-02-01T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440210",
      question_id: "550e8400-e29b-41d4-a716-446655440108",
      option_content: "身份认证和数据完整性验证",
      option_label: "B",
      is_correct: 1,
      create_time: ISODate("2024-02-01T10:05:00.000Z"),
      update_time: ISODate("2024-02-01T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440211",
      question_id: "550e8400-e29b-41d4-a716-446655440108",
      option_content: "数据压缩",
      option_label: "C",
      is_correct: 0,
      create_time: ISODate("2024-02-01T10:05:00.000Z"),
      update_time: ISODate("2024-02-01T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440212",
      question_id: "550e8400-e29b-41d4-a716-446655440108",
      option_content: "数据备份",
      option_label: "D",
      is_correct: 0,
      create_time: ISODate("2024-02-01T10:05:00.000Z"),
      update_time: ISODate("2024-02-01T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440213",
      question_id: "550e8400-e29b-41d4-a716-446655440109",
      option_content: "先来先服务（FCFS）",
      option_label: "A",
      is_correct: 0,
      create_time: ISODate("2024-02-05T10:05:00.000Z"),
      update_time: ISODate("2024-02-05T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440214",
      question_id: "550e8400-e29b-41d4-a716-446655440109",
      option_content: "最短作业优先（SJF）",
      option_label: "B",
      is_correct: 0,
      create_time: ISODate("2024-02-05T10:05:00.000Z"),
      update_time: ISODate("2024-02-05T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440215",
      question_id: "550e8400-e29b-41d4-a716-446655440109",
      option_content: "时间片轮转（RR）",
      option_label: "C",
      is_correct: 0,
      create_time: ISODate("2024-02-05T10:05:00.000Z"),
      update_time: ISODate("2024-02-05T10:05:00.000Z"),
      is_deleted: 0
    },
    {
      _id: "550e8400-e29b-41d4-a716-446655440216",
      question_id: "550e8400-e29b-41d4-a716-446655440109",
      option_content: "随机调度",
      option_label: "D",
      is_correct: 1,
      create_time: ISODate("2024-02-05T10:05:00.000Z"),
      update_time: ISODate("2024-02-05T10:05:00.000Z"),
      is_deleted: 0
    }
  ]);