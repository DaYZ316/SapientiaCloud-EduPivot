package com.dayz.sapientiacloud_edupivot.student.entity.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private UUID classroomId;

    private UUID studentId;

    private UUID questionId;

    private AnswerPayload answer;

    private Integer status;

    private Boolean isCorrect;

    private Float score;

    private LocalDateTime submitTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerPayload implements Serializable {
        private String content;
        private List<String> options;
        private List<String> blanks;
    }
}