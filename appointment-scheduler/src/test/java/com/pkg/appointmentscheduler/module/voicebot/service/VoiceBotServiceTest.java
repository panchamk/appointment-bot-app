package com.pkg.appointmentscheduler.module.voicebot.service;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_SESSION_HEADER;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.pkg.appointmentscheduler.AppointmentSchedulerTest;
import com.pkg.appointmentscheduler.module.crud.service.AppointmentService;
import com.pkg.appointmentscheduler.module.crud.service.DoctorService;
import com.pkg.appointmentscheduler.module.crud.service.DoctorSlotService;
import com.pkg.appointmentscheduler.module.crud.service.PatientService;
import com.pkg.appointmentscheduler.module.rest.context.AppointmentContext;
import com.pkg.appointmentscheduler.module.rest.entity.VoiceBotInitRequest;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatFunctionCall;

@AppointmentSchedulerTest
public class VoiceBotServiceTest {
    public static final String DOCTOR_NAME = "pancham";
    public static final String DOCTOR_EMAIL = "pancham@walnut.com";

    @Autowired
    VoiceBotService voiceBotService;
    @Autowired
    ChatSessionCache chatSessionCache;

    @Autowired
    DoctorService doctorService;
    @Autowired
    DoctorSlotService doctorSlotService;
    @Autowired
    PatientService patientService;
    @Autowired
    AppointmentService appointmentService;

    @Test
    public void shouldInitiateChatSession() {
        Map<String, String> session =
                voiceBotService.initiateChatWithPatient(new VoiceBotInitRequest(DOCTOR_NAME, DOCTOR_EMAIL, "specialist"));
        assertThat(session).hasSize(2);
        assertThat(chatSessionCache.getUserSession(session.get(CHAT_SESSION_HEADER))).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"22", "10am", "10 am", "11PM", "11 PM", "11:00", "11:00AM", "10:00am", "10:00 am"  })
    public void shouldParseTime(String time) {
        LocalTime localTime =voiceBotService.parseTime(Map.of("time", time));
        assertThat(localTime).isNotNull();
    }
    @ParameterizedTest
    @MethodSource("provideParameters")
    public void shouldCheckForAppointment(ChatFunctionCall functionCall, String expectedResponse) {
        Map<String, String> session =
                voiceBotService.initiateChatWithPatient(new VoiceBotInitRequest(DOCTOR_NAME, DOCTOR_EMAIL, "specialist"));
        Map<String, String> response = voiceBotService.callAppointmentService(new AppointmentContext(session.get("doctorId")), functionCall);
        assertThat(response.get("message")).isEqualTo(expectedResponse);

    }

    public static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(new ChatFunctionCall("appointment_checking", "{\"date\":\"2024-03-07\",\"time\":\"22\"}"), "Slot is available for appointment. Would you like to proceed?"),
                Arguments.of(new ChatFunctionCall("appointment_checking", "{\"date\":\"2024-03-07\",\"time\":\"10 PM\"}"), "Slot is available for appointment. Would you like to proceed?"),
                Arguments.of(new ChatFunctionCall("appointment_checking", "{\"date\":\"2024-03-07\",\"time\":\"10AM\"}"), "Slot is available for appointment. Would you like to proceed?"),
                Arguments.of(new ChatFunctionCall("appointment_checking", "{\"date\":\"2024-03-07\",\"time\":\"\"}"), "Please provide time."),
                Arguments.of(
                        new ChatFunctionCall("appointment_booking",
                                "{\"date\":\"2024-03-07\",\"time\":\"22\", \"name\":\"patient1\",\"contact\":\"1234567890\"}"),
                        "Appointment scheduled."),
                Arguments.of(
                        new ChatFunctionCall("appointment_booking",
                                "{\"date\":\"2024-03-07\",\"time\":\"05pm\", \"name\":\"patient1\",\"contact\":\"1234567890\"}"),
                        "Appointment scheduled."),
                Arguments.of(new ChatFunctionCall("appointment_reschedule",
                        "{\"date\":\"2024-03-07\",\"time\":\"10PM\", \"name\":\"patient1\",\"contact\":\"1234567890\"}"), "Appointment rescheduled."),
                Arguments.of(new ChatFunctionCall("appointment_reschedule",
                        "{\"date\":\"2024-03-07\",\"time\":\"00AM\", \"name\":\"patient1\",\"contact\":\"1234567890\"}"), "Appointment rescheduled."),
                Arguments.of(new ChatFunctionCall("appointment_cancel", "{\"name\":\"patient1\",\"contact\":\"1234567890\"}"),
                        "Patient does not have any appointment. Please check Patient name and contact.")
        );
    }

    @AfterEach
    public void tearDown() {
        chatSessionCache.invalidateCache();
        appointmentService.deleteAll();
        doctorSlotService.deleteAll();
        doctorService.deleteAll();
        patientService.deleteAll();
    }
}
