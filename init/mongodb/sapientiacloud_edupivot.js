/*
 Navicat Premium Dump Script

 Source Server         : 117.72.194.197
 Source Server Type    : MongoDB
 Source Server Version : 60005 (6.0.5)
 Source Host           : 117.72.194.197:27017
 Source Schema         : sapientiacloud_edupivot

 Target Server Type    : MongoDB
 Target Server Version : 60005 (6.0.5)
 File Encoding         : 65001

 Date: 22/04/2026 20:18:41
*/


// ----------------------------
// Collection structure for live_room_message
// ----------------------------
db.getCollection("live_room_message").drop();
db.createCollection("live_room_message");

// ----------------------------
// Collection structure for mg_chat_message
// ----------------------------
db.getCollection("mg_chat_message").drop();
db.createCollection("mg_chat_message");

// ----------------------------
// Collection structure for mg_chat_session
// ----------------------------
db.getCollection("mg_chat_session").drop();
db.createCollection("mg_chat_session");

// ----------------------------
// Collection structure for mg_course_chapter
// ----------------------------
db.getCollection("mg_course_chapter").drop();
db.createCollection("mg_course_chapter");

// ----------------------------
// Collection structure for mg_course_forum
// ----------------------------
db.getCollection("mg_course_forum").drop();
db.createCollection("mg_course_forum");

// ----------------------------
// Collection structure for mg_course_question_bank
// ----------------------------
db.getCollection("mg_course_question_bank").drop();
db.createCollection("mg_course_question_bank");

// ----------------------------
// Collection structure for mg_course_task
// ----------------------------
db.getCollection("mg_course_task").drop();
db.createCollection("mg_course_task");

// ----------------------------
// Collection structure for mg_file_document
// ----------------------------
db.getCollection("mg_file_document").drop();
db.createCollection("mg_file_document");

// ----------------------------
// Collection structure for mg_forum_post
// ----------------------------
db.getCollection("mg_forum_post").drop();
db.createCollection("mg_forum_post");

// ----------------------------
// Collection structure for mg_forum_reply
// ----------------------------
db.getCollection("mg_forum_reply").drop();
db.createCollection("mg_forum_reply");

// ----------------------------
// Collection structure for mg_knowledge_vector
// ----------------------------
db.getCollection("mg_knowledge_vector").drop();
db.createCollection("mg_knowledge_vector");

// ----------------------------
// Collection structure for mg_question
// ----------------------------
db.getCollection("mg_question").drop();
db.createCollection("mg_question");

// ----------------------------
// Collection structure for mg_question_answer
// ----------------------------
db.getCollection("mg_question_answer").drop();
db.createCollection("mg_question_answer");

// ----------------------------
// Collection structure for mg_question_option
// ----------------------------
db.getCollection("mg_question_option").drop();
db.createCollection("mg_question_option");

// ----------------------------
// Collection structure for mg_question_student
// ----------------------------
db.getCollection("mg_question_student").drop();
db.createCollection("mg_question_student");
