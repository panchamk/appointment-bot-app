package com.pkg.appointmentscheduler.module.voicebot.openai.entity;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatFunctionParams(String type, Map<String, ArgsProps> properties, List<String> required) {
}
