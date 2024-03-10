package com.pkg.appointmentscheduler.common;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_REQUEST_ID_HEADER;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;

import org.slf4j.MDC;

public class MdcUtils {

    public static String getRequestId() {
        String requestId = MDC.get(CHAT_REQUEST_ID_HEADER);
        if (isBlank(requestId)) {
            requestId = generateNewRequestId();
            MDC.put(CHAT_REQUEST_ID_HEADER, requestId);
        }
        return requestId;
    }

    public static void clearContext() {
        MDC.clear();
    }

    public static Runnable withMdcContext(Runnable runnable) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            MDC.setContextMap(mdc);
            try {
                runnable.run();
            } finally {
                clearContext();
            }
        };
    }

    private static String generateNewRequestId() {
        return randomUUID().toString();
    }

}
