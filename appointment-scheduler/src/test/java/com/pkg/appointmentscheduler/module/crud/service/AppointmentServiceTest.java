package com.pkg.appointmentscheduler.module.crud.service;

import static com.pkg.appointmentscheduler.module.voicebot.service.VoiceBotService.APPOINTMENT_DURATION;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.List;

import lombok.experimental.FieldDefaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pkg.appointmentscheduler.AppointmentSchedulerTest;
import com.pkg.appointmentscheduler.module.crud.entity.AppointmentRequest;
import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;

@AppointmentSchedulerTest
@FieldDefaults(level = PRIVATE)
public class AppointmentServiceTest {
    public static final String PATIENT_ID = randomUUID().toString();
    private static final String DOCTOR_ID = randomUUID().toString();
    public static final long NOW_MILLIS = Clock.systemDefaultZone().millis();

    @Autowired
    AppointmentService appointmentService;

    @Test
    public void shouldScheduleAppointment() {
        PatientAppointment appointment= appointmentService.scheduleAppointment(appointmentRequest(NOW_MILLIS));
        assertThat(appointment.getStartTime()).isEqualTo(NOW_MILLIS);
    }

    @Test
    public void shouldReScheduleAppointment() {
        appointmentService.scheduleAppointment(appointmentRequest(NOW_MILLIS));
        AppointmentRequest rescheduleAppointmentRequest = appointmentRequest(NOW_MILLIS+APPOINTMENT_DURATION);
        PatientAppointment appointment = appointmentService.scheduleAppointment(rescheduleAppointmentRequest);
        assertThat(appointment.getStartTime()).isEqualTo(rescheduleAppointmentRequest.startTime());
    }

    @Test
    public void shouldFetchAppointment() {
        PatientAppointment appointment= appointmentService.scheduleAppointment(appointmentRequest(NOW_MILLIS));
        assertThat(appointmentService.getAppointmentId(appointmentRequest(NOW_MILLIS))).isEqualTo(appointment.getId());
    }

    @Test
    public void shouldFetchAppointmentByDoctorId() {
        PatientAppointment appointment1 = appointmentService.scheduleAppointment(appointmentRequest(DOCTOR_ID, randomUUID().toString(), NOW_MILLIS));
        PatientAppointment appointment2 = appointmentService.scheduleAppointment(appointmentRequest(DOCTOR_ID, randomUUID().toString(), NOW_MILLIS + APPOINTMENT_DURATION));
        assertThat(appointmentService.fetchAppointments(DOCTOR_ID)).isEqualTo(List.of(appointment1, appointment2));
    }

    @Test
    public void shouldFetchAppointmentByDoctorIdAndPatientId() {
        PatientAppointment appointment1 = appointmentService.scheduleAppointment(appointmentRequest(NOW_MILLIS));
        assertThat(appointmentService.fetchAppointments(DOCTOR_ID, PATIENT_ID).get()).isEqualTo(appointment1);
    }

    @AfterEach
    public void tearDown() {
        appointmentService.deleteAll();
    }

    private AppointmentRequest appointmentRequest(long startTime) {
        return appointmentRequest(DOCTOR_ID, PATIENT_ID, startTime);
    }

    private AppointmentRequest appointmentRequest(String doctorId, String patientId, long startTime) {
        return AppointmentRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .startTime(startTime)
                .endTime(startTime + APPOINTMENT_DURATION).build();
    }

}
