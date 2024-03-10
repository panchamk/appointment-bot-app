package com.pkg.appointmentscheduler.module.crud.service;

import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.pkg.appointmentscheduler.module.crud.entity.Patient;
import com.pkg.appointmentscheduler.module.crud.entity.PatientRequest;
import com.pkg.appointmentscheduler.module.crud.storage.PatientStorage;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class PatientService {
    PatientStorage patientStorage;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Optional<Patient> fetchPatientInfo(Long contact) {
        return patientStorage.findByContact(contact);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Patient savePatientInfo(PatientRequest patientRequest) {
        return patientStorage.save(Patient.from(patientRequest));
    }

    @VisibleForTesting
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteAll() {
        patientStorage.deleteAll();
    }
}
