package com.pkg.appointmentscheduler.module.crud.service;

import static com.pkg.appointmentscheduler.module.voicebot.service.VoiceBotService.APPOINTMENT_DURATION;
import static java.time.ZoneOffset.UTC;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.pkg.appointmentscheduler.module.crud.entity.Doctor;
import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot;
import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot.Fields;
import com.pkg.appointmentscheduler.module.crud.storage.DoctorSlotsStorage;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class DoctorSlotService {

    DoctorSlotsStorage doctorSlotsStorage;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public List<DoctorSlot> fetchDoctorSlots(String doctorId) {
        return doctorSlotsStorage.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public List<DoctorSlot> addSlots(List<DoctorSlot> doctorSlots) {
        return doctorSlotsStorage.saveAllAndFlush(doctorSlots);
    }

    public void initDoctorSlotsForTheWeekIfNotAvailable(Doctor doctor) {
        List<DoctorSlot> slots = fetchDoctorSlots(doctor.getId());
        if (isEmpty(slots)) {
            LocalDate now = LocalDate.now();
            List<Long> slotsTiming = IntStream.range(0, 24)
                    .mapToObj(i -> LocalDateTime.of(now, LocalTime.of(i, 0)))
                    .map(dateTime -> dateTime.toInstant(UTC).toEpochMilli())
                    .toList();
            List<DoctorSlot> slotsForTheWeek = IntStream.range(0, 7)
                    .mapToObj(i -> i * APPOINTMENT_DURATION)
                    .flatMap(d -> slotsTiming.stream().map(slot -> slot + d))
                    .map(slotTime -> new DoctorSlot(randomUUID().toString(), doctor.getId(), slotTime,
                            slotTime + APPOINTMENT_DURATION, false))
                    .toList();
            addSlots(slotsForTheWeek);

        }
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public List<Long> removeAllSlots(String doctorId) {
        return ofNullable(fetchDoctorSlots(doctorId))
                .orElse(List.of())
                .stream()
                .map(this::removeSlot)
                .toList();
    }

    public long removeSlots(String doctorId, long startTime) {
        return ofNullable(fetchDoctorSlots(doctorId))
                .orElse(List.of())
                .stream()
                .filter(slot -> confirmSlot(startTime, slot))
                .map(this::removeSlot)
                .findFirst()
                .get();

    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public long removeSlot(DoctorSlot slot) {
        return doctorSlotsStorage.delete(
                slotIdEquals(slot.getSlotId())
                        .and(doctorIdEquals(slot.getDoctorId())));
    }

    private static boolean confirmSlot(long startTime, DoctorSlot slot) {
        return slot.getStartTime() == startTime;
    }

    private static Specification<DoctorSlot> doctorIdEquals(final String doctorId) {
        return (doctorSlot, query, cb) -> cb.equal(doctorSlot.get(Fields.doctorId), doctorId);
    }

    private static Specification<DoctorSlot> slotIdEquals(final String slotId) {
        return (doctorSlot, query, cb) -> cb.equal(doctorSlot.get(Fields.slotId), slotId);
    }

    @VisibleForTesting
    public void deleteAll() {
        doctorSlotsStorage.deleteAll();
    }
}
