package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "mg_file_document")
@CompoundIndexes({
        @CompoundIndex(name = "idx_session_user", def = "{'session_id': 1, 'sys_user_id': 1}"),
        @CompoundIndex(name = "idx_course_user", def = "{'course_id': 1, 'sys_user_id': 1}")
})
@Schema(description = "文件文档")
public class FileDocument extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -783381367279593664L;

    @Id
    @Schema(description = "文件ID")
    private UUID id;

    @Field("file_name")
    @Indexed
    @Schema(description = "文件名")
    private String fileName;

    @Field("file_type")
    @Schema(description = "文件类型: 0-PDF, 1-DOC, 2-DOCX, 3-XLS, 4-XLSX, 5-TXT, 6-MD, 7-RTF")
    private Integer fileType;

    @Field("file_size")
    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Field("mime_type")
    @Schema(description = "MIME类型")
    private String mimeType;

    @Field("storage_path")
    @Schema(description = "存储路径（MinIO对象名）")
    private String storagePath;

    @Field("bucket_code")
    @Schema(description = "存储桶代码")
    private String bucketCode;

    @Field("sys_user_id")
    @Indexed
    @Schema(description = "上传用户ID")
    private UUID sysUserId;

    @Field("course_id")
    @Indexed
    @Schema(description = "课程ID（可选）")
    private UUID courseId;

    @Field("session_id")
    @Indexed
    @Schema(description = "会话ID（可选，关联会话）")
    private UUID sessionId;

    @Field("status")
    @Schema(description = "状态: 0-正常, 1-已删除, 2-处理中, 3-处理失败")
    private Integer status;

    @Field("is_vectorized")
    @Schema(description = "是否已向量化")
    private Boolean isVectorized;

    @Field("vector_count")
    @Schema(description = "向量块数量")
    private Integer vectorCount;

    @Field("parse_error")
    @Schema(description = "解析错误信息")
    private String parseError;

    @BsonIgnore
    @TableField(exist = false)
    private Integer deleted;
}

