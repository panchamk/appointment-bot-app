package com.pkg.appointmentscheduler.module.voicebot.openai;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeignHeaderInterceptor implements RequestInterceptor {

    @Value("${openai_api_key}")
    private String apiKey;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + apiKey);
    }
}
