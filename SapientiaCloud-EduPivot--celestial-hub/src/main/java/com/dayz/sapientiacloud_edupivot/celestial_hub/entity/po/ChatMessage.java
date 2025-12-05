package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.base.BaseEntity;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "mg_chat_message")
@CompoundIndexes({
        @CompoundIndex(name = "uniq_session_role_request", def = "{'session_id': 1, 'role': 1, 'request_id': 1}", unique = true, sparse = true)
})
@Schema(description = "AI对话消息")
public class ChatMessage extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -783381367279593664L;

    @Id
    @Schema(description = "消息ID")
    private UUID id;

    @Field("session_id")
    @Schema(description = "会话ID")
    private UUID sessionId;

    @Field("role")
    @Schema(description = "角色: 0-用户, 1-AI助手, 2-系统")
    private Integer role;

    @Field("content")
    @Schema(description = "消息内容")
    private String content;

    @Field("message_type")
    @Schema(description = "消息类型: 0-文本, 1-代码, 2-图片, 3-文件")
    private Integer messageType;

    @Field("token_count")
    @Schema(description = "token数量")
    private Integer tokenCount;

    @Field("model_name")
    @Schema(description = "使用的模型名称")
    private String modelName;

    @Field("references")
    @Schema(description = "引用的参考内容")
    private List<Map<String, Object>> references;

    @Field("attachments")
    @Schema(description = "附件URL列表")
    private List<String> attachments;

    @Field("file_references")
    @Schema(description = "引用文件数组（用于索引文件向量数据）")
    private List<FileReference> fileReferences;

    @Field("request_id")
    @Schema(description = "请求ID（用于幂等），Kafka传入或HTTP生成")
    private String requestId;

    @Field("is_feedback")
    @Schema(description = "用户反馈: 0-无, 1-有用, -1-无用")
    private Integer isFeedback;

    @Field("metadata")
    @Schema(description = "元数据")
    private Map<String, Object> metadata;

    @Field("question_request")
    @Schema(description = "出题请求参数（JSON），当角色为出题请求者时使用")
    private String questionRequest;

    @Field("question_response")
    @Schema(description = "AI出题生成结果（JSON），当角色为出题者时使用")
    private String questionResponse;

    @BsonIgnore
    @TableField(exist = false)
    private Integer deleted;
}

