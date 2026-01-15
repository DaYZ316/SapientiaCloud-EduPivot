package com.dayz.sapientiacloud_edupivot.classroom.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
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
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_record_student")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教学学生参与持久化对象 (PO)")
public class CourseRecordStudent extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1246892565502037842L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private UUID id;

    @Schema(description = "课程记录ID")
    @TableField(value = "record_id")
    private UUID recordId;

    @Schema(description = "学生ID")
    @TableField(value = "student_id")
    private UUID studentId;

    @Schema(description = "课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(description = "座位编号 (从0开始)")
    @TableField("seat_index")
    private Integer seatIndex;

    @Schema(description = "学生座位的x坐标")
    @TableField("location_x")
    private Integer locationX;

    @Schema(description = "学生座位的y坐标")
    @TableField("location_y")
    private Integer locationY;

    @Schema(description = "座位状态 (0=正常, 2=已预留, 3=已占用)")
    @TableField("seat_status")
    private Integer seatStatus;

    @TableField(exist = false)
    private Integer deleted;
}
