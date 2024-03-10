package com.pkg.appointmentscheduler.module.voicebot.openai.exception;

import static lombok.AccessLevel.PRIVATE;

import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = PRIVATE)
public class FeignApiClientException extends RuntimeException {
    @Getter
    int statusCode;
    String requestUrl;
    String responseBody;

    public FeignApiClientException(String requestUrl, int statusCode, String responseBody) {
        super(responseBody);
        this.requestUrl = requestUrl;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public FeignApiClientException(String requestUrl, int statusCode, String responseBody, Throwable e) {
        super(responseBody, e);
        this.requestUrl = requestUrl;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public FeignApiClientException(Throwable e) {
        super(e);
    }
}
