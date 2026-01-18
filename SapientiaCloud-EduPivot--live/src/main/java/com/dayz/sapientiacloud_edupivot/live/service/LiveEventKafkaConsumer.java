package com.dayz.sapientiacloud_edupivot.live.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.live.event.LiveEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 直播事件 Kafka 消费者 - 转发直播房间状态变更事件到 SSE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LiveEventKafkaConsumer {

    private final LiveEventPublisher liveEventPublisher;

    @KafkaListener(
            topics = "${spring.kafka.topic.live-events:live-events-topic}",
            groupId = "live-live-events-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLiveEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            String message = record.value();
            String key = record.key();

            log.debug("Received live event: key={}, message={}", key, message);

            Map<String, Object> payload = JSON.parseObject(message, Map.class);
            String event = (String) payload.get("event");
            String classroomId = (String) payload.get("classroomId");

            if (event == null || classroomId == null) {
                log.warn("Invalid live event message: missing event or classroomId");
                acknowledgment.acknowledge();
                return;
            }

            switch (event) {
                case "start":
                    log.info("Live room started: classroomId={}", classroomId);
                    liveEventPublisher.publishToClassroom(classroomId, payload);
                    break;
                case "end":
                    log.info("Live room ended: classroomId={}", classroomId);
                    liveEventPublisher.publishToClassroom(classroomId, payload);
                    break;
                case "close":
                    log.info("Live room closed: classroomId={}", classroomId);
                    liveEventPublisher.publishToClassroom(classroomId, payload);
                    break;
                default:
                    log.warn("Unknown live event type: {}", event);
                    break;
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing live event: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
