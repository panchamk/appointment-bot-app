package com.pkg.appointmentscheduler.module.rest.exception;

import static com.pkg.appointmentscheduler.module.rest.provider.HttpRequestIdProvider.REQUEST_ID;

import jakarta.inject.Provider;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.pkg.appointmentscheduler.module.rest.exception.entity.ErrorResponse;

@Component
@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    @Qualifier(REQUEST_ID)
    Provider<String> requestIdProvider;

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @Override
    public ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        return ResponseEntity.status(statusCode)
                .body( buildDefaultErrorResponse(ex));
    }

    public ErrorResponse buildDefaultErrorResponse(Exception ex) {
        return ErrorResponse.builder()
                .requestId(requestIdProvider.get())
                .message(ex.getMessage()).build();
    }

    @ResponseBody
    @ExceptionHandler(value = VoiceBotException.class)
    public ResponseEntity<ErrorResponse> handleVoiceBotException(VoiceBotException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(buildErrorResponse(ex));
    }

    public ErrorResponse buildErrorResponse(VoiceBotException ex) {
        return ErrorResponse.builder()
                .requestId(requestIdProvider.get())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage()).build();
    }
}
