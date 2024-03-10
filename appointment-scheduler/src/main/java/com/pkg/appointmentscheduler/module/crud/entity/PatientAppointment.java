package com.pkg.appointmentscheduler.module.crud.entity;

import static java.util.UUID.randomUUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Entity(name = "appointment")
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatientAppointment {
    @Id
    @Column(nullable = false)
    String id;
    @Column(nullable = false)
    String patientId;
    @Column(nullable = false)
    String doctorId;
    @Column(nullable = false)
    long startTime;
    @Column(nullable = false)
    long endTime;
    @Column(nullable = false)
    boolean deleted;

    public static PatientAppointment from(AppointmentRequest appointmentRequest) {
        return from(randomUUID().toString(), appointmentRequest);
    }

    public static PatientAppointment from(String id, AppointmentRequest appointmentRequest) {
        return new PatientAppointment(id, appointmentRequest.patientId(), appointmentRequest.doctorId(),
                appointmentRequest.startTime(), appointmentRequest.endTime(), false);
    }
}
