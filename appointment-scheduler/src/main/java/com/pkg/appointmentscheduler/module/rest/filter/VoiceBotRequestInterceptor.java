package com.pkg.appointmentscheduler.module.rest.filter;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_REQUEST_ID_HEADER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.UUID;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pkg.appointmentscheduler.common.MdcUtils;

@Component
@Slf4j
public class VoiceBotRequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MdcUtils.clearContext();
        insertIntoMDC(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MdcUtils.clearContext();
    }

    private void insertIntoMDC(ServletRequest request) {
        if (request instanceof HttpServletRequest servletRequest) {
            MDC.put(CHAT_REQUEST_ID_HEADER, determineRequestId(servletRequest));
        }
    }

    private String determineRequestId(HttpServletRequest servletRequest) {
        String requestId = servletRequest.getHeader(CHAT_REQUEST_ID_HEADER);
        if (isEmpty(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }
}
