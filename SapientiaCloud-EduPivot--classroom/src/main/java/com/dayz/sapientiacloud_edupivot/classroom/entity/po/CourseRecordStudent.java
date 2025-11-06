package com.dayz.sapientiacloud_edupivot.classroom.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
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

    @Schema(description = "3D坐标X (横向)")
    @TableField("location_x")
    private Float locationX;

    @Schema(description = "3D坐标Y (高度)")
    @TableField("location_y")
    private Float locationY;

    @Schema(description = "3D坐标Z (纵深)")
    @TableField("location_z")
    private Float locationZ;

    @Schema(description = "朝向角度 (弧度制)")
    @TableField("rotation_y")
    private Float rotationY;

    @Schema(description = "座位状态 (normal, marked, reserved, occupied)")
    @TableField("seat_status")
    private String seatStatus;

    @Schema(description = "出勤状态 (0=未签到, 1=已签到, 2=缺席)")
    @TableField("attendance_status")
    private Integer attendanceStatus;

    @Schema(description = "课堂互动得分 (可选)")
    @TableField("participation_score")
    private Float participationScore;

    @TableField(exist = false)
    private Integer deleted;
}
