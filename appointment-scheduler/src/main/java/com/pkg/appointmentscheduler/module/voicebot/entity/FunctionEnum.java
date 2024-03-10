package com.pkg.appointmentscheduler.module.voicebot.entity;

import static lombok.AccessLevel.PRIVATE;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
public enum FunctionEnum {
    appointment_booking,
    appointment_reschedule,
    appointment_cancel,
    appointment_checking;

}
