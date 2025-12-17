package com.dayz.sapientiacloud_edupivot.classroom.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("mg_classroom_question")
@Schema(description = "课堂-题目发布记录持久化对象 (PO)")
public class ClassroomQuestion extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 5932842378423146571L;

    @TableId("id")
    private UUID id;

    @Schema(description = "课堂记录ID")
    @TableField("classroom_id")
    private UUID classroomId;

    @Schema(description = "题目ID")
    @TableField("question_id")
    private UUID questionId;

    @Schema(description = "题目标题")
    @TableField("question_title")
    private String questionTitle;

    @Schema(description = "发布顺序")
    @TableField("publish_order")
    private Integer publishOrder;

    @Schema(description = "是否必答 (0=选答,1=必答)")
    @TableField("is_required")
    private Integer isRequired;

    @Schema(description = "题目可作答开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "题目作答截止时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField(exist = false)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer deleted;
}