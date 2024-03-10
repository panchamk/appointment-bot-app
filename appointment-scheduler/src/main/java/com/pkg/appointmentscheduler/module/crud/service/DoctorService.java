package com.pkg.appointmentscheduler.module.crud.service;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.pkg.appointmentscheduler.module.crud.entity.Doctor;
import com.pkg.appointmentscheduler.module.crud.storage.DoctorStorage;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class DoctorService {
    DoctorStorage doctorStorage;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Optional<Doctor> fetchDoctorInfo(String doctorId) {
        return doctorStorage.findById(doctorId);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Optional<Doctor> fetchDoctorInfo(String name, String email) {
        return doctorStorage.findByNameAndEmail(name, email);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Doctor saveDoctorInfo(String name, String email, String specialization) {
        return doctorStorage.save(new Doctor(randomUUID().toString(), name, email, specialization));
    }

    @VisibleForTesting
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteAll() {
        doctorStorage.deleteAll();
    }
}
