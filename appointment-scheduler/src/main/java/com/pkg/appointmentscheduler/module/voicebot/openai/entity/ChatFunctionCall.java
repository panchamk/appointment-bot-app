package com.pkg.appointmentscheduler.module.voicebot.openai.entity;

import java.util.Map;

import lombok.SneakyThrows;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatFunctionCall(String name, String arguments) {
    @SneakyThrows
    public Map<String, String> getArguments(ObjectMapper objectMapper) {
        return objectMapper.readValue(arguments, new TypeReference<Map<String, String>>() {
        });
    }
}
