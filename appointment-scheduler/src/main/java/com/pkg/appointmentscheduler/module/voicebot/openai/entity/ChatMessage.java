package com.pkg.appointmentscheduler.module.voicebot.openai.entity;

import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ISO_DATE;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessage(String role, String content, String name, @JsonProperty("function_call") ChatFunctionCall functionCall) {
    public static final ChatMessage DEFAULT_SYSTEM_INTENT = new ChatMessage("system",
            """
                You are an expert in booking appointments. You need to ask the user for the appointment date, appointment time, and email ID. 
                The user can book the appointment from 10 AM to 7 PM from Monday to Friday, and from 10 AM to 2 PM on Saturdays. 
                You need to remember that today's date is %s and day is %s.
                Check if the time provided by the user is within the working hours then only you will proceed.
                Instructions: 
                - Don"t make assumptions about what values to plug into functions, if the user does not provide any of the required parameters then you must need to ask for clarification.
                - Make sure the email Id is valid and not empty.
                - If a user request is ambiguous, then also you need to ask for clarification.
                - When a user asks for a rescheduling date or time of the current appointment, then you must ask for the new appointment details only.
                - If a user didn't specify "ante meridiem (AM)" or "post meridiem (PM)" while providing the time, then you must have to ask for clarification.
                  If the user didn't provide day, month, and year while giving the time then you must have to ask for clarification.
    
                Make sure to follow the instructions carefully while processing the request. 
            """.formatted(now().format(ISO_DATE), now().getDayOfWeek()), null,  null);
}
