package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
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
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程论坛回复查询数据传输对象 (QueryDTO)")
public class ThreadReplyQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "threadId", description = "所属主贴ID")
    private UUID threadId;

    @Schema(name = "userId", description = "回复用户ID")
    private UUID userId;

    @Schema(name = "parentReplyId", description = "父回复ID")
    private UUID parentReplyId;
}
