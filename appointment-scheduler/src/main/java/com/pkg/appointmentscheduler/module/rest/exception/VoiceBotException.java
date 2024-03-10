package com.pkg.appointmentscheduler.module.rest.exception;

import lombok.Getter;

@Getter
public class VoiceBotException extends RuntimeException {
    public String errorCode;
    public int statusCode;
    public String requestId;
    public VoiceBotException(int statusCode, String errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public VoiceBotException(int statusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
