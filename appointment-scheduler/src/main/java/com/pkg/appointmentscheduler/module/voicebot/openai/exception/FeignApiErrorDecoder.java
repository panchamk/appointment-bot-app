package com.pkg.appointmentscheduler.module.voicebot.openai.exception;

import static lombok.AccessLevel.PRIVATE;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import com.google.common.io.CharStreams;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class FeignApiErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        String requestUrl = response.request().url();
        try (Reader bodyReader = response.body().asReader(Charset.defaultCharset())) {
            String message = CharStreams.toString(bodyReader);
            log.error("API {}, returned {} response {} with headers: {}", requestUrl, response.status(), message, response.request().headers());
            return new FeignApiClientException(requestUrl, response.status(), message);
        } catch (IOException e) {
            log.error("Could not retrieve error response - API :{} - {}, header:{}", response.status(), requestUrl, response.request().headers(), e);
            return new FeignApiClientException(requestUrl, response.status(), e.getMessage());
        }

    }
}
