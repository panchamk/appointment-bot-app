package com.pkg.appointmentscheduler.module.rest.filter;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_SESSION_HEADER;
import static com.pkg.appointmentscheduler.common.Constants.INVALID_SESSION_ID;
import static com.pkg.appointmentscheduler.common.Constants.SESSION_EXPIRED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.time.Clock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkg.appointmentscheduler.module.rest.exception.RestResponseEntityExceptionHandler;
import com.pkg.appointmentscheduler.module.rest.exception.VoiceBotException;
import com.pkg.appointmentscheduler.module.rest.exception.entity.ErrorResponse;
import com.pkg.appointmentscheduler.module.voicebot.service.ChatSessionCache;

@Slf4j
@Component
public class VoiceBotRequestFilter extends OncePerRequestFilter {

    public static final int SESSION_EXPIRATION_TIME = 10 * 60 * 1000;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ChatSessionCache chatSessionCache;
    @Autowired
    RestResponseEntityExceptionHandler restResponseEntityExceptionHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            if (path.contains("/init") == true) {
                chain.doFilter(request, response);
                return;
            }
            if (isBlank(request.getHeader(CHAT_SESSION_HEADER))) {
                throw new VoiceBotException(UNAUTHORIZED.value(), INVALID_SESSION_ID, "Invalid session");
            }
            validateSessionId(request);
            chain.doFilter(request, response);
        } catch (VoiceBotException exception) {
            response.setStatus(exception.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(convertObjectToJson(restResponseEntityExceptionHandler.buildErrorResponse(exception)));
        }
    }

    public String convertObjectToJson(ErrorResponse errorResponse) throws JsonProcessingException {
        if (errorResponse == null) {
            return null;
        }
        return objectMapper.writeValueAsString(errorResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.contains("/v1/appointment/");
    }

    private void validateSessionId(HttpServletRequest req) {
        String sessionId = req.getHeader(CHAT_SESSION_HEADER);
        String[] sessionAttrs = sessionId.split(":");
        if (sessionAttrs.length != 3) {
            throw new VoiceBotException(UNAUTHORIZED.value(), INVALID_SESSION_ID, "Invalid session id.");
        }
        try {
            long sessionCreationTime = Long.parseLong(sessionId.split(":")[2]);
            if (Clock.systemDefaultZone().millis() - sessionCreationTime > SESSION_EXPIRATION_TIME) {
                chatSessionCache.invalidateCache(sessionId);
                throw new VoiceBotException(UNAUTHORIZED.value(), SESSION_EXPIRED, "Session expired");
            }
        } catch (NumberFormatException ex) {
            throw new VoiceBotException(UNAUTHORIZED.value(), INVALID_SESSION_ID, "Invalid session id.");
        }
    }
}
