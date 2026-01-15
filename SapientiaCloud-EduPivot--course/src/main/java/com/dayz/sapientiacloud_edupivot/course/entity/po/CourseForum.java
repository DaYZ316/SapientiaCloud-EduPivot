package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_course_forum")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程论坛持久化对象 (PO)")
public class CourseForum extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1199790299170401140L;

    @Schema(name = "id", description = "论坛ID")
    @Id
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "forumName", description = "论坛名称")
    @Field("forum_name")
    private String forumName;

    @Schema(name = "description", description = "论坛描述")
    @Field("description")
    private String description;

    @Schema(name = "forumType", description = "论坛类型 (0=讨论区, 1=问答区, 2=作业区, 3=公告区)")
    @Field("forum_type")
    private Integer forumType;

    @Schema(name = "allowAnonymous", description = "是否允许匿名发帖 (0=不允许, 1=允许)")
    @Field("allow_anonymous")
    private Integer allowAnonymous;

    @Schema(name = "postCount", description = "帖子总数")
    @Field("post_count")
    private Long postCount;

    @Schema(name = "replyCount", description = "回复总数")
    @Field("reply_count")
    private Long replyCount;

    @Schema(name = "status", description = "论坛状态 (0=正常, 1=关闭, 2=维护)")
    @Field("status")
    private Integer status;

    @Schema(name = "tags", description = "标签列表")
    @Field("tags")
    private List<String> tags;
}
