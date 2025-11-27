package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatResponseVO;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface IChatMessageService {

    ChatResponseVO chat(ChatRequestDTO request);

    Flux<String> chatStream(ChatRequestDTO request);

    Flux<String> chatStreamKafka(KafkaChatRequestDTO request);

    void cancelKafkaChat(String requestId, String reason);

    List<ChatMessage> listMessagesBySessionId(UUID sessionId, Integer limit);

    ChatMessage getChatMessageById(UUID id);

    Boolean feedbackMessage(UUID messageId, Integer feedback);
}

