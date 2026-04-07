package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;

public interface TtsAudioService {

    ChatMessage initializeAudioGeneration(ChatMessage message);

    void generateAudioAsync(String messageId);
}
