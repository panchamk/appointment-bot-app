package com.pkg.appointmentscheduler.module.rest;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot;
import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;
import com.pkg.appointmentscheduler.module.crud.service.AppointmentService;
import com.pkg.appointmentscheduler.module.crud.service.DoctorSlotService;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@RequestMapping(path = "/v1/doctor", produces = MediaType.APPLICATION_JSON_VALUE)
public class DoctorAdminController {

    DoctorService doctorService;
    AppointmentService appointmentService;
    DoctorSlotService doctorSlotService;

    @GetMapping("list")
    public List<Doctor> fetchDoctors() {
        return doctorService.fetchDoctorList();
    }
    @GetMapping("/{doctorId}/booked")
    public List<PatientAppointment> fetchAppointments(@PathVariable("doctorId") String doctorId) {
        return appointmentService.fetchAppointments(doctorId);
    }

    @GetMapping("/{doctorId}/slots")
    public List<DoctorSlot> fetchSlots(@PathVariable("doctorId") String doctorId) {
        return doctorSlotService.fetchDoctorSlots(doctorId);
    }

}
