package com.pkg.appointmentscheduler.module.crud.service;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.experimental.FieldDefaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pkg.appointmentscheduler.AppointmentSchedulerTest;
import com.pkg.appointmentscheduler.module.crud.entity.Doctor;

@AppointmentSchedulerTest
@FieldDefaults(level = PRIVATE)
public class DoctorServiceTest {
    @Autowired
    DoctorService doctorService;

    @Test
    public void shouldSaveDoctorInfo() {
        Doctor doctor = doctorService.saveDoctorInfo("pancham", "pancham@walnut.com", "specialist");
        assertThat(doctorService.fetchDoctorInfo(doctor.getId()).get()).isEqualTo(doctor);
    }


    @Test
    public void shouldFetchDoctorInfoByNameAndEmail() {
        Doctor doctor = doctorService.saveDoctorInfo("pancham", "pancham@walnut.com", "specialist");
        assertThat(doctorService.fetchDoctorInfo(doctor.getName(), doctor.getEmail()).get()).isEqualTo(doctor);
    }

    @AfterEach
    public void tearDown() {
        doctorService.deleteAll();
    }
}
