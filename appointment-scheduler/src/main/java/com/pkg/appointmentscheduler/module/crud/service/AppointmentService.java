package com.pkg.appointmentscheduler.module.crud.service;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.pkg.appointmentscheduler.module.crud.entity.AppointmentRequest;
import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;
import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment.Fields;
import com.pkg.appointmentscheduler.module.crud.storage.AppointmentStorage;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class AppointmentService {
    AppointmentStorage appointmentStorage;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public PatientAppointment scheduleAppointment(AppointmentRequest appointmentRequest) {
        return appointmentStorage.save(
                PatientAppointment.from(getAppointmentId(appointmentRequest), appointmentRequest));
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public String getAppointmentId(AppointmentRequest appointmentRequest) {
        return fetchAppointments(appointmentRequest.doctorId(), appointmentRequest.patientId())
                .map(PatientAppointment::getId)
                .orElse(randomUUID().toString());
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<PatientAppointment> fetchAppointments(String doctorId) {
        return appointmentStorage.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public Optional<PatientAppointment> fetchAppointments(String doctorId, String patientId) {
        return appointmentStorage.findByDoctorIdAndPatientId(doctorId, patientId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public long cancelAppointment(String doctorId, String patientId) {
        return appointmentStorage.delete(doctorIdEquals(doctorId).and(patientIdEquals(patientId)));
    }

    private static Specification<PatientAppointment> doctorIdEquals(final String doctorId) {
        return (appointment, query, cb) -> cb.equal(appointment.get(Fields.doctorId), doctorId);
    }

    private static Specification<PatientAppointment> patientIdEquals(final String patientId) {
        return (appointment, query, cb) -> cb.equal(appointment.get(Fields.patientId), patientId);
    }

    @VisibleForTesting
    public void deleteAll() {
        appointmentStorage.deleteAll();
    }
}
