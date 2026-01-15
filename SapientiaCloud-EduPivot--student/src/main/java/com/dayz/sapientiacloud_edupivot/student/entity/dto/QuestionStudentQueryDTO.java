package com.dayz.sapientiacloud_edupivot.student.entity.dto;

import com.dayz.sapientiacloud_edupivot.student.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课堂练习作答查询DTO")
public class QuestionStudentQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = 120112345987612343L;

    @Schema(description = "课堂记录ID")
    private UUID classroomId;

    @Schema(description = "学生ID")
    private UUID studentId;

    @Schema(description = "题目ID")
    private UUID questionId;

    @Schema(description = "练习ID")
    private UUID practiceId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "是否正确（0-错误，1-正确，2-半对，3-待批阅）")
    private Integer isCorrect;

}

