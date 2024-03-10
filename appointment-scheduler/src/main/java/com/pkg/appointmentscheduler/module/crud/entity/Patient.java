package com.pkg.appointmentscheduler.module.crud.entity;

import static java.util.UUID.randomUUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Entity(name = "patient")
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Patient {
    @Id
    @Column(nullable = false)
    String id;
    @Column(nullable = false)
    String name;
    @Column(nullable = false)
    Long contact;
    @Column(nullable = false)
    int age;
    @Column(nullable = false)
    String details;

    public static Patient from(PatientRequest patientRequest) {
        return new Patient(randomUUID().toString(), patientRequest.name(), patientRequest.contact(), patientRequest.age(),
                patientRequest.details());
    }
}
