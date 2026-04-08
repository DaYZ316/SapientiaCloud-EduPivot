package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;

import java.util.UUID;

public interface TtsAudioService {

    ChatMessage initializeAudioGeneration(ChatMessage message);

    ChatMessage generateAudioForMessage(UUID messageId);

    void generateAudioAsync(String messageId);
}
