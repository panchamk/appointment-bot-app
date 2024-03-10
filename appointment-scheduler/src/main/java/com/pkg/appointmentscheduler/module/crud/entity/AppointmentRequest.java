package com.pkg.appointmentscheduler.module.crud.entity;

import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AppointmentRequest(String patientId, String doctorId, long startTime, long endTime) {
}
