package com.pkg.appointmentscheduler.module.crud.service;

import static com.pkg.appointmentscheduler.module.voicebot.service.VoiceBotService.APPOINTMENT_DURATION;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.pkg.appointmentscheduler.AppointmentSchedulerTest;
import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot;

@AppointmentSchedulerTest
public class DoctorSlotsServiceTest {
    public static final String DOCTOR_ID = randomUUID().toString();
    @Autowired
    DoctorSlotService doctorSlotService;

    @Test
    public void shouldSaveDoctorSlotsInfo() {
        List<DoctorSlot> doctorSlots = doctorSlotService.addSlots(doctorSlots(DOCTOR_ID));
        assertThat(doctorSlotService.fetchDoctorSlots(DOCTOR_ID)).isEqualTo(doctorSlots);
    }

    @Test
    public void shouldRemoveAllSlots() {
        List<DoctorSlot> doctorSlots = doctorSlotService.addSlots(doctorSlots(DOCTOR_ID));
        doctorSlotService.removeAllSlots(DOCTOR_ID);
        assertThat(doctorSlotService.fetchDoctorSlots(DOCTOR_ID)).isEqualTo(List.of());
    }

    private List<DoctorSlot> doctorSlots(String doctorId) {
        long startTime = Clock.systemUTC().millis();
        return IntStream.range(0, 10).mapToObj(i -> {
            long slotStartTime = startTime + i * APPOINTMENT_DURATION;
            return new DoctorSlot(randomUUID().toString(), doctorId, slotStartTime, slotStartTime + APPOINTMENT_DURATION, false);
        }).toList();
    }

    @AfterEach
    public void tearDown() {
        doctorSlotService.deleteAll();
    }
}
