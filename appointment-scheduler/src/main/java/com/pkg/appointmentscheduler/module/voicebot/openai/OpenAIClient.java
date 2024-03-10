package com.pkg.appointmentscheduler.module.voicebot.openai;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pkg.appointmentscheduler.module.voicebot.openai.exception.FeignApiErrorDecoder;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatRequest;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatResponse;

@FeignClient(name = "openai-client", url = "${openai.api.url}/v1/chat", configuration = { FeignApiErrorDecoder.class })
public interface OpenAIClient {
    @RequestMapping(method = POST, value = "/completions", produces = APPLICATION_JSON_VALUE)
    ChatResponse parseUserInput(@RequestBody ChatRequest chatRequest);
}
