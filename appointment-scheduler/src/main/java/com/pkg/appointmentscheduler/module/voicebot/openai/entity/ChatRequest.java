package com.pkg.appointmentscheduler.module.voicebot.openai.entity;

import java.util.List;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatRequest(String model, List<ChatMessage> messages, List<ChatFunction> functions) {
}
