package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.LocalTtsClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.MinIOClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.LocalTtsRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.LocalTtsAudioVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.TtsAudioService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsAudioServiceImpl implements TtsAudioService {

    private final ChatMessageRepository chatMessageRepository;
    private final LocalTtsClient localTtsClient;
    private final MinIOClient minIOClient;
    private final ApplicationContext applicationContext;

    @Value("${avatar.tts.enabled:false}")
    private boolean ttsEnabled;

    @Value("${avatar.tts.auto-generate:true}")
    private boolean autoGenerate;

    @Value("${avatar.tts.default-voice-code:teacher-default}")
    private String defaultVoiceCode;

    @Value("${avatar.tts.default-audio-format:wav}")
    private String defaultAudioFormat;

    @Value("${avatar.tts.max-text-length:2000}")
    private int maxTextLength;

    @Value("${avatar.tts.upload-directory:avatar-audio/}")
    private String uploadDirectory;

    @Value("${avatar.tts.bucket-code:AI_QA_ASSET}")
    private String bucketCode;

    @Override
    public ChatMessage initializeAudioGeneration(ChatMessage message) {
        return initializeAudioGeneration(message, true);
    }

    private ChatMessage initializeAudioGeneration(ChatMessage message, boolean respectAutoGenerate) {
        if (message == null || !AIChatConstants.ROLE_ASSISTANT.equals(message.getRole())) {
            return message;
        }

        if (!ttsEnabled || (respectAutoGenerate && !autoGenerate) || !StringUtils.hasText(message.getContent())) {
            message.setAudioStatus(AIChatConstants.AUDIO_STATUS_NONE);
            return chatMessageRepository.save(message);
        }

        if (message.getContent().length() > maxTextLength) {
            message.setAudioStatus(AIChatConstants.AUDIO_STATUS_FAILED);
            message.setAudioErrorMessage("消息内容过长，超过语音生成限制");
            message.setUpdateTime(LocalDateTime.now());
            return chatMessageRepository.save(message);
        }

        message.setAudioStatus(AIChatConstants.AUDIO_STATUS_PENDING);
        message.setAudioFormat(defaultAudioFormat);
        message.setAudioVoiceCode(defaultVoiceCode);
        message.setAudioTaskId(UUID.randomUUID().toString());
        message.setAudioErrorMessage(null);
        message.setAudioUrl(null);
        message.setAudioDurationMs(null);
        message.setAudioGenerateTime(null);
        message.setUpdateTime(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(message);
        applicationContext.getBean(TtsAudioService.class).generateAudioAsync(savedMessage.getId().toString());
        return savedMessage;
    }

    @Override
    public ChatMessage generateAudioForMessage(UUID messageId) {
        if (messageId == null) {
            throw new BusinessException(AIChatEnum.MESSAGE_ID_REQUIRED);
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(AIChatEnum.MESSAGE_NOT_EXISTS));

        if (!AIChatConstants.ROLE_ASSISTANT.equals(message.getRole())) {
            throw new BusinessException(AIChatEnum.MESSAGE_ROLE_INVALID);
        }

        if (!StringUtils.hasText(message.getContent())) {
            throw new BusinessException(AIChatEnum.MESSAGE_CONTENT_REQUIRED);
        }

        if (Integer.valueOf(AIChatConstants.AUDIO_STATUS_READY).equals(message.getAudioStatus())
                && StringUtils.hasText(message.getAudioUrl())) {
            return message;
        }

        if (Integer.valueOf(AIChatConstants.AUDIO_STATUS_PENDING).equals(message.getAudioStatus())
                || Integer.valueOf(AIChatConstants.AUDIO_STATUS_PROCESSING).equals(message.getAudioStatus())) {
            return message;
        }

        if (!ttsEnabled) {
            message.setAudioStatus(AIChatConstants.AUDIO_STATUS_NONE);
            message.setAudioErrorMessage("语音生成未开启");
            message.setUpdateTime(LocalDateTime.now());
            return chatMessageRepository.save(message);
        }

        return initializeAudioGeneration(message, false);
    }

    @Override
    @Async
    public void generateAudioAsync(String messageId) {
        try {
            UUID id = UUID.fromString(messageId);
            ChatMessage message = chatMessageRepository.findById(id).orElse(null);
            if (message == null) {
                return;
            }
            if (!Integer.valueOf(AIChatConstants.AUDIO_STATUS_PENDING).equals(message.getAudioStatus())) {
                return;
            }

            message.setAudioStatus(AIChatConstants.AUDIO_STATUS_PROCESSING);
            message.setUpdateTime(LocalDateTime.now());
            chatMessageRepository.save(message);

            LocalTtsRequestDTO requestDTO = new LocalTtsRequestDTO();
            requestDTO.setTaskId(message.getAudioTaskId());
            requestDTO.setText(message.getContent());
            requestDTO.setVoiceCode(message.getAudioVoiceCode());
            requestDTO.setAudioFormat(message.getAudioFormat());

            LocalTtsAudioVO audioVO = localTtsClient.synthesize(requestDTO);
            String objectName = buildObjectName(message);
            MultipartFile multipartFile = new ByteArrayMultipartFile(
                    "file",
                    extractFilename(objectName),
                    "audio/wav",
                    audioVO.getAudioBytes()
            );

            Result<Map<String, String>> uploadResult = minIOClient.uploadFile(
                    multipartFile,
                    extractDirectory(objectName),
                    bucketCode
            );

            if (uploadResult == null || !uploadResult.isSuccess() || uploadResult.getData() == null) {
                throw new IllegalStateException("上传音频到 MinIO 失败");
            }

            Map<String, String> uploaded = uploadResult.getData();
            message.setAudioStatus(AIChatConstants.AUDIO_STATUS_READY);
            message.setAudioUrl(uploaded.get("url"));
            message.setAudioDurationMs(audioVO.getDurationMs());
            message.setAudioGenerateTime(LocalDateTime.now());
            message.setAudioErrorMessage(null);
            message.setUpdateTime(LocalDateTime.now());
            chatMessageRepository.save(message);
        } catch (Exception ex) {
            handleAudioFailure(messageId, ex);
        }
    }

    private void handleAudioFailure(String messageId, Exception ex) {
        try {
            UUID id = UUID.fromString(messageId);
            chatMessageRepository.findById(id).ifPresent(message -> {
                message.setAudioStatus(AIChatConstants.AUDIO_STATUS_FAILED);
                message.setAudioErrorMessage(ex.getMessage());
                message.setUpdateTime(LocalDateTime.now());
                chatMessageRepository.save(message);
            });
        } catch (Exception nestedEx) {
            log.warn("更新音频失败状态时发生异常: messageId={}, error={}", messageId, nestedEx.getMessage());
        }
        log.warn("异步生成音频失败: messageId={}, error={}", messageId, ex.getMessage());
    }

    private String buildObjectName(ChatMessage message) {
        String extension = StringUtils.hasText(message.getAudioFormat()) ? message.getAudioFormat() : AIChatConstants.AUDIO_FORMAT_WAV;
        return normalizeDirectory(uploadDirectory) + LocalDateTime.now().toLocalDate() + "/" + message.getAudioTaskId() + "." + extension;
    }

    private String normalizeDirectory(String directory) {
        if (!StringUtils.hasText(directory)) {
            return "";
        }
        return directory.endsWith("/") ? directory : directory + "/";
    }

    private String extractDirectory(String objectName) {
        int idx = objectName.lastIndexOf('/');
        if (idx < 0) {
            return null;
        }
        return objectName.substring(0, idx + 1);
    }

    private String extractFilename(String objectName) {
        int idx = objectName.lastIndexOf('/');
        if (idx < 0) {
            return objectName;
        }
        return objectName.substring(idx + 1);
    }
}
