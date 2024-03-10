package com.pkg.appointmentscheduler.module.rest.exception.entity;

import lombok.Builder;

@Builder
public record ErrorResponse(String requestId, String errorCode, String message) {
}
