package com.pkg.appointmentscheduler;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_REQUEST_ID_HEADER;
import static java.util.UUID.randomUUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;

public class AppointmentSchedulerTestCtx {
    @Bean
    HttpServletRequest httpServletRequest() {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader(CHAT_REQUEST_ID_HEADER, randomUUID().toString());
        return new MockHttpServletRequest();
    }
}
