package com.pkg.appointmentscheduler.module.rest.provider;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_REQUEST_ID_HEADER;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HttpRequestIdProvider implements Provider<String> {
    public static final String REQUEST_ID = "REQUEST_ID";

    @Autowired
    Provider<HttpServletRequest> requestProvider;

    @Override
    @Bean(REQUEST_ID)
    @Scope(SCOPE_PROTOTYPE)
    public String get() {
        return ofNullable(requestProvider.get().getHeader(CHAT_REQUEST_ID_HEADER))
                .orElseGet(() -> {
                    log.warn("Request id is not present in http request. creating it.");
                    return randomUUID().toString();
                });
    }
}
