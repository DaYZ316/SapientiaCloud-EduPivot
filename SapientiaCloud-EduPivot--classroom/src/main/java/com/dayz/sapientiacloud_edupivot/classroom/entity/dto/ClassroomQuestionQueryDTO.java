package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课堂练习发布记录查询DTO")
public class ClassroomQuestionQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5932842378423146571L;

    @Schema(description = "课堂记录ID")
    private UUID classroomId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "题目标题（模糊匹配）")
    private String title;

    @Schema(description = "是否必答（0-否，1-是）")
    private Integer isRequired;

    @TableField(exist = false)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private LocalDateTime updateTime;
}