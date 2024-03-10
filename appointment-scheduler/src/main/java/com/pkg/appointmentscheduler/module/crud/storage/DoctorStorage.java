package com.pkg.appointmentscheduler.module.crud.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pkg.appointmentscheduler.module.crud.entity.Doctor;

public interface DoctorStorage extends JpaRepository<Doctor, String> {
    Optional<Doctor> findByNameAndEmail(String name, String email);
}
