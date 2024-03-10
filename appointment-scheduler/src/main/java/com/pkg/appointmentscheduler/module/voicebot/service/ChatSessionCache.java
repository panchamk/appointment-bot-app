package com.pkg.appointmentscheduler.module.voicebot.service;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.NonNull;

import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatMessage;

@Component
public class ChatSessionCache {
    private static final long MAX_NUMBER_OF_CACHED_STATS = 100;
    LoadingCache<String, Optional<List<ChatMessage>>> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_NUMBER_OF_CACHED_STATS)
            .expireAfterWrite(10 * 60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Optional<List<ChatMessage>>>() {
                @Override
                public Optional<List<ChatMessage>> load(@NonNull String key) {
                    return ofNullable(List.of(ChatMessage.DEFAULT_SYSTEM_INTENT));
                }
            });

    public Optional<List<ChatMessage>> getUserSession(String sessionId) {
        return cache.getUnchecked(sessionId);
    }

    public void updateUserChatSession(String sessionId, ChatMessage assistantMessage) {
        Set<ChatMessage> sessionMessages = new HashSet<>(cache.getUnchecked(sessionId).get());
        sessionMessages.add(assistantMessage);
        cache.put(sessionId, ofNullable(sessionMessages.stream().toList()));
    }

    public void invalidateCache(String sessionId) {
        cache.invalidate(sessionId);
    }

    public String initUserSession(String doctorId) {
        String sessionId = """
                %s:%s:%s""".formatted(doctorId, randomUUID(), Clock.systemDefaultZone().millis());
        cache.refresh(sessionId);
        return sessionId;
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }
}
