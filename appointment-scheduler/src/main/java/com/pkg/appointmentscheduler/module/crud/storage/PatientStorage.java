package com.pkg.appointmentscheduler.module.crud.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkg.appointmentscheduler.module.crud.entity.Patient;

public interface PatientStorage extends JpaRepository<Patient, String> {
    Optional<Patient> findByContact(Long contact);
}
