package com.pkg.appointmentscheduler.module.voicebot.openai;

import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatRequest;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.Choice;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class UserQueryProcessor {

    OpenAIClient openAIClient;

    public Choice understandUserInput(ChatRequest userQuery) {
        return ofNullable(openAIClient.parseUserInput(userQuery).choices())
                .orElse(List.of())
                .get(0);

    }
}
