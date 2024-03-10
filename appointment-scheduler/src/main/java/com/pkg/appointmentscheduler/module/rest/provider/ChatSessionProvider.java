package com.pkg.appointmentscheduler.module.rest.provider;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_SESSION_HEADER;
import static com.pkg.appointmentscheduler.common.Constants.INVALID_REQUEST_ID;
import static com.pkg.appointmentscheduler.common.Constants.INVALID_SESSION_ID;
import static java.util.Optional.ofNullable;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.pkg.appointmentscheduler.module.rest.exception.VoiceBotException;

@Slf4j
@Component
public class ChatSessionProvider implements Provider<String> {
    public static final String CHAT_SESSION_ID = "CHAT_SESSION_ID";

    @Autowired
    Provider<HttpServletRequest> requestProvider;

    @Override
    @Bean(CHAT_SESSION_ID)
    @Scope(SCOPE_PROTOTYPE)
    public String get() {
        return ofNullable(requestProvider.get().getHeader(CHAT_SESSION_HEADER))
                .orElseThrow(() -> new VoiceBotException(UNAUTHORIZED.value(), INVALID_REQUEST_ID, "Fatal error: Session request Id does not exist."));
    }
}
