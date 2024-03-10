package com.pkg.appointmentscheduler.module.crud.storage;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;

public interface AppointmentStorage extends JpaRepository<PatientAppointment, String>,
        JpaSpecificationExecutor<PatientAppointment> {
    List<PatientAppointment> findByDoctorId(String doctorId);
    Optional<PatientAppointment> findByDoctorIdAndPatientId(String doctorId, String patientId);
}
