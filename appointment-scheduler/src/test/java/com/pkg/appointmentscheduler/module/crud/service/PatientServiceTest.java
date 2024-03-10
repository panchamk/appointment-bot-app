package com.pkg.appointmentscheduler.module.crud.service;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.experimental.FieldDefaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pkg.appointmentscheduler.AppointmentSchedulerTest;
import com.pkg.appointmentscheduler.module.crud.entity.Patient;
import com.pkg.appointmentscheduler.module.crud.entity.PatientRequest;

@AppointmentSchedulerTest
@FieldDefaults(level = PRIVATE)
public class PatientServiceTest {

    @Autowired
    PatientService patientService;

    @Test
    public void shouldSavePatientInfo() {
        Patient patient = patientService.savePatientInfo(patientRequest());
        assertThat(patientService.fetchPatientInfo(patient.getContact()).get()).isEqualTo(patient);
    }

    private  PatientRequest patientRequest() {
        return new PatientRequest("pancham", 1234567890L, 30, "suffering with fever");
    }

    @Test
    public void shouldFetchPatientInfoByNameAndEmail() {
        Patient patient = patientService.savePatientInfo(patientRequest());
        assertThat(patientService.fetchPatientInfo(patient.getContact()).get()).isEqualTo(patient);
    }

    @AfterEach
    public void tearDown() {
        patientService.deleteAll();
    }
}
