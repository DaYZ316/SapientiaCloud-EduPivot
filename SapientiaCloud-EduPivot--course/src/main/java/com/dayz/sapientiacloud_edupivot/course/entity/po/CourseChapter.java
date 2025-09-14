package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_chapter")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程章节信息持久化对象 (PO)")
public class CourseChapter extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "章节ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(name = "parentId", description = "父章节ID (用于支持多级章节结构, NULL表示为一级章节)")
    @TableField("parent_id")
    private UUID parentId;

    @Schema(name = "chapterName", description = "章节名称")
    @TableField("chapter_name")
    private String chapterName;

    @Schema(name = "chapterContent", description = "章节内容 (例如: 详细的文本、富文本标记等)")
    @TableField("chapter_content")
    private String chapterContent;

    @Schema(name = "sort", description = "章节排序 (值越小越靠前)")
    @TableField("sort")
    private Integer sort;
}
