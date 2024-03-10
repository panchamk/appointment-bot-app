package com.pkg.appointmentscheduler.module.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Entity(name = "doctor_slots")
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DoctorSlot {
    @Id
    @Column(nullable = false)
    String slotId;
    @Column(nullable = false)
    String doctorId;
    @Column(nullable = false)
    long startTime;
    @Column(nullable = false)
    long endTime;
    @Column(nullable = false)
    boolean deleted;
}
