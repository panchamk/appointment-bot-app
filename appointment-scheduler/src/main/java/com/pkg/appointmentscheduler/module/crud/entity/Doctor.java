package com.pkg.appointmentscheduler.module.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

@Entity(name = "doctor")
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Doctor {
    @Id
    @Column(nullable = false)
    String id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    String email;
    @Column(nullable = false)
    String specialization;
}
