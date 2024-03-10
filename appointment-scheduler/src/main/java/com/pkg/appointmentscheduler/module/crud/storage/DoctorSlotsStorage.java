package com.pkg.appointmentscheduler.module.crud.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot;

public interface DoctorSlotsStorage extends JpaRepository<DoctorSlot, String>, JpaSpecificationExecutor<DoctorSlot> {
    List<DoctorSlot> findByDoctorId(String doctorId);
}
