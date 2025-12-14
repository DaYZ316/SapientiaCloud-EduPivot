package com.dayz.sapientiacloud_edupivot.student.entity.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("mg_question_student")
@Schema(description = "学生课堂练习作答记录")
public class QuestionStudent implements Serializable {

    @Serial
    private static final long serialVersionUID = -450112345987612340L;

    @Id
    private UUID id;

    @Field("classroom_id")
    private UUID classroomId;

    @Field("student_id")
    private UUID studentId;

    @Field("question_id")
    private UUID questionId;

    @Field("answer")
    private AnswerPayload answer;

    @Field("is_correct")
    private Boolean isCorrect;

    @Field("score")
    private Float score;

    @Field("submit_time")
    private LocalDateTime submitTime;

    @Field("create_time")
    private LocalDateTime createTime;

    @Field("update_time")
    private LocalDateTime updateTime;

    @Field("is_deleted")
    private Integer isDeleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerPayload implements Serializable {

        @Serial
        private static final long serialVersionUID = 2184572433235517333L;

        @Field("content")
        private String content;

        @Field("options")
        private List<String> options;

        @Field("blanks")
        private List<String> blanks;
    }
}