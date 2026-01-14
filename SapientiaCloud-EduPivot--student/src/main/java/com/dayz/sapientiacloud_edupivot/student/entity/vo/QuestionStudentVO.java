package com.dayz.sapientiacloud_edupivot.student.entity.vo;

import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent.AnswerPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课堂练习作答VO")
public class QuestionStudentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -450112345987612341L;

    @Schema(description = "作答记录ID")
    private UUID id;

    @Schema(description = "课堂记录ID")
    private UUID classroomId;

    @Schema(description = "学生ID")
    private UUID studentId;

    @Schema(description = "学生姓名")
    private String studentRealName;

    @Schema(description = "题目ID")
    private UUID questionId;

    @Schema(description = "练习ID")
    private UUID practiceId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "作答内容")
    private AnswerPayload answer;

    @Schema(description = "是否正确(0-错误,1-正确,2-半对)")
    private Integer isCorrect;

    @Schema(description = "得分")
    private Float score;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

