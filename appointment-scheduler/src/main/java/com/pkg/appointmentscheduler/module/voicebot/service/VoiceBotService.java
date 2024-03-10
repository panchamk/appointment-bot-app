package com.pkg.appointmentscheduler.module.voicebot.service;

import static com.pkg.appointmentscheduler.common.Constants.APPOINTMENT_ALREADY_EXISTS;
import static com.pkg.appointmentscheduler.common.Constants.CHAT_SESSION_HEADER;
import static com.pkg.appointmentscheduler.common.Constants.DOCTOR_NOT_AVAILABLE;
import static com.pkg.appointmentscheduler.common.Constants.INVALID_INPUT;
import static com.pkg.appointmentscheduler.module.rest.provider.ChatSessionProvider.CHAT_SESSION_ID;
import static com.pkg.appointmentscheduler.module.rest.provider.HttpRequestIdProvider.REQUEST_ID;
import static com.pkg.appointmentscheduler.module.voicebot.entity.FunctionEnum.appointment_booking;
import static com.pkg.appointmentscheduler.module.voicebot.entity.FunctionEnum.appointment_cancel;
import static com.pkg.appointmentscheduler.module.voicebot.entity.FunctionEnum.appointment_checking;
import static com.pkg.appointmentscheduler.module.voicebot.entity.FunctionEnum.appointment_reschedule;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Provider;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.pkg.appointmentscheduler.module.crud.entity.AppointmentRequest;
import com.pkg.appointmentscheduler.module.crud.entity.Doctor;
import com.pkg.appointmentscheduler.module.crud.entity.DoctorSlot;
import com.pkg.appointmentscheduler.module.crud.entity.Patient;
import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;
import com.pkg.appointmentscheduler.module.crud.entity.PatientRequest;
import com.pkg.appointmentscheduler.module.crud.service.AppointmentService;
import com.pkg.appointmentscheduler.module.crud.service.DoctorService;
import com.pkg.appointmentscheduler.module.crud.service.DoctorSlotService;
import com.pkg.appointmentscheduler.module.crud.service.PatientService;
import com.pkg.appointmentscheduler.module.rest.context.AppointmentContext;
import com.pkg.appointmentscheduler.module.rest.entity.VoiceBotInitRequest;
import com.pkg.appointmentscheduler.module.rest.exception.VoiceBotException;
import com.pkg.appointmentscheduler.module.voicebot.entity.FunctionEnum;
import com.pkg.appointmentscheduler.module.voicebot.openai.UserQueryProcessor;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatFunction;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatFunctionCall;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatMessage;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatRequest;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.Choice;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = PRIVATE)
public class VoiceBotService {
    public static final int APPOINTMENT_DURATION = 1 * 60 * 60 * 1000;
    ObjectMapper objectMapper;
    ResourceLoader resourceLoader;
    PatientService patientService;
    DoctorService doctorService;
    DoctorSlotService doctorSlotService;
    AppointmentService appointmentService;
    UserQueryProcessor userQueryProcessor;
    Provider<String> chatSessionProvider;
    Provider<String> requestIdProvider;
    String model;
    ChatSessionCache chatSessionCache;

    List<ChatFunction> chatFunctions = new ArrayList<>();

    public VoiceBotService(ObjectMapper objectMapper,
            ResourceLoader resourceLoader,
            PatientService patientService,
            DoctorService doctorService,
            DoctorSlotService doctorSlotService, AppointmentService appointmentService,
            UserQueryProcessor userQueryProcessor,
            @Qualifier(CHAT_SESSION_ID) Provider<String> chatSessionProvider,
            @Qualifier(REQUEST_ID) Provider<String> requestIdProvider,
            @Value("${openai.model}") String model, ChatSessionCache chatSessionCache) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.doctorSlotService = doctorSlotService;
        this.appointmentService = appointmentService;
        this.userQueryProcessor = userQueryProcessor;
        this.chatSessionProvider = chatSessionProvider;
        this.requestIdProvider = requestIdProvider;
        this.model = model;
        this.chatSessionCache = chatSessionCache;
    }

    @SneakyThrows
    @PostConstruct
    public void init() {
        InputStream functionStream = resourceLoader.getResource("classpath:openai/functions.json").getInputStream();
        String functions = IOUtils.toString(functionStream);
        chatFunctions.addAll(objectMapper.readValue(functions, new TypeReference<List<ChatFunction>>() {
        }));
    }

    /**
     * initiate chat session and return unique session Id using doctorId.
     * subsequent user query request must contain chat session id in request headers.
     *
     * @param voiceBotInitRequest
     * @return
     */
    public Map<String, String> initiateChatWithPatient(VoiceBotInitRequest voiceBotInitRequest) {
        Doctor doctor = doctorService.fetchDoctorInfo(voiceBotInitRequest.doctorName(), voiceBotInitRequest.doctorEmail())
                .orElseGet(() -> doctorService.saveDoctorInfo(voiceBotInitRequest.doctorName(), voiceBotInitRequest.doctorEmail(),
                        voiceBotInitRequest.specialization()));
        doctorSlotService.initDoctorSlotsForTheWeekIfNotAvailable(doctor);
        String sessionId = chatSessionCache.initUserSession(doctor.getId());
        return Map.of("doctorId", doctor.getId(), CHAT_SESSION_HEADER, sessionId);
    }

    public void closeUserSession() {
        chatSessionCache.invalidateCache(chatSessionProvider.get());
    }

    public ChatMessage userQuery(AppointmentContext appointmentContext, String userQuery) {
        ChatRequest chatRequest = chatRequest(userQuery);
        Choice choice = userQueryProcessor.understandUserInput(chatRequest);
        log.info("User query response: {}", choice);
        ChatMessage assistantMessage = choice.message();
        chatSessionCache.updateUserChatSession(chatSessionProvider.get(), assistantMessage);
        if (assistantMessage.functionCall() != null) {
            return userQueryProcessor
                    .understandUserInput(
                            summarizeResponse(chatRequest,
                                    callAppointmentService(appointmentContext, assistantMessage.functionCall())))
                    .message();
        }
        return assistantMessage;
    }

    @SneakyThrows
    private ChatRequest summarizeResponse(ChatRequest chatRequest, Map<String, String> response) {
        chatRequest.messages()
                .add(ChatMessage.builder()
                        .role("function")
                        .name(response.get("name"))
                        .content(objectMapper.writeValueAsString(response))
                        .build());
        return chatRequest;
    }

    @VisibleForTesting
    <T> T handleExceptions(Callable<T> routine) {
        try {
            return routine.call();
        } catch (Exception e) {
            log.error("Error occurred while handling appointment flow", e);
            return (T) Map.of("requestId", requestIdProvider.get(), "message", e.getMessage());
        }
    }

    @VisibleForTesting
    Map<String, String> callAppointmentService(AppointmentContext appointmentContext, ChatFunctionCall functionCall) {
        FunctionEnum userAction = FunctionEnum.valueOf(functionCall.name());
        Map<String, String> arguments = functionCall.getArguments(objectMapper);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("name", userAction.name());
        try {
            if (appointment_booking == userAction) {
                scheduleAppointment(appointmentContext, arguments);
                responseMap.put("message", "Appointment scheduled.");
            } else if (appointment_reschedule == userAction) {
                scheduleAppointment(appointmentContext, arguments);
                responseMap.put("message", "Appointment rescheduled.");
            } else if (appointment_checking == userAction) {
                responseMap.putAll(checkAppointmentAvailability(appointmentContext, arguments));
            } else if (appointment_cancel == userAction) {
                responseMap.putAll(cancelAppointment(appointmentContext, arguments));
            }
            return responseMap;
        } catch (Exception e) {
            log.error("Error occurred while handling appointment flow", e);
            return Map.of("name", userAction.name(), "requestId", requestIdProvider.get(), "message", e.getMessage());
        }
    }

    private PatientAppointment scheduleAppointment(AppointmentContext appointmentContext, Map<String, String> arguments) {
        Long contact = Long.parseLong(arguments.get("contact"));
        Patient patient = patientService.fetchPatientInfo(contact)
                .orElseGet(() -> patientService.savePatientInfo(new PatientRequest(arguments.get("name"), contact, 0, "")));
        validatePatientAppointment(appointmentContext, patient);
        checkAppointmentAvailability(appointmentContext, arguments);
        return appointmentService.scheduleAppointment(appointmentRequest(appointmentContext, patient.getId(), arguments));
    }

    private Map<String, String> cancelAppointment(AppointmentContext appointmentContext, Map<String, String> arguments) {
        Long contact = Long.parseLong(arguments.get("contact"));
        Patient patient = patientService.fetchPatientInfo(contact)
                .orElseThrow(() -> new VoiceBotException(BAD_REQUEST.value(), APPOINTMENT_ALREADY_EXISTS,
                        "Patient does not have any appointment. Please check Patient name and contact."));
        appointmentService.cancelAppointment(appointmentContext.doctorId(), patient.getId());
        return Map.of("message", "Appointment cancelled.");
    }

    private Map<String, String> checkAppointmentAvailability(AppointmentContext appointmentContext, Map<String, String> arguments) {
        List<DoctorSlot> doctorSlots = validateDoctorAvailability(appointmentContext);
        long requestedTimeInMillis = startTimeInMillis(arguments);
        Map<Long, String> hoursOfTheDay = hoursOfTheDay();

        Optional<PatientAppointment> bookedAppointment =
                appointmentService.fetchAppointments(appointmentContext.doctorId()).stream()
                        .filter(patientAppointment -> requestedTimeInMillis == patientAppointment.getStartTime())
                        .findFirst();
        if (bookedAppointment.isPresent()) {
            throw new VoiceBotException(
                    BAD_REQUEST.value(),
                    DOCTOR_NOT_AVAILABLE,
                    """
                            Doctor is not available at that time.  I have the following availability: %s
                            """
                            .formatted(doctorSlots.stream()
                                    .map(DoctorSlot::getStartTime)
                                    .filter(time -> time != requestedTimeInMillis)
                                    .map(hoursOfTheDay::get)
                                    .reduce(", ", String::concat)));
        } else {
            return Map.of("message", "Slot is available for appointment. Would you like to proceed?");
        }
    }

    @NotNull
    private List<DoctorSlot> validateDoctorAvailability(AppointmentContext appointmentContext) {
        List<DoctorSlot> doctorSlots = doctorSlotService.fetchDoctorSlots(appointmentContext.doctorId());
        if (isEmpty(doctorSlots)) {
            throw new VoiceBotException(BAD_REQUEST.value(), DOCTOR_NOT_AVAILABLE, "Doctor is not available");
        }
        return doctorSlots;
    }

    private Map<Long, String> hoursOfTheDay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hha");
        LocalDate now = LocalDate.now();
        return IntStream.range(0, 24)
                .mapToObj(i -> LocalDateTime.of(now, LocalTime.of(i, 0)))
                .collect(toMap(
                        dateTime -> dateTime.toInstant(UTC).toEpochMilli(),
                        dateTime -> dateTime.format(formatter)));
    }

    private void validatePatientAppointment(AppointmentContext appointmentContext, Patient patient) {
        appointmentService.fetchAppointments(appointmentContext.doctorId(), patient.getId())
                .ifPresent((appointment) -> new VoiceBotException(BAD_REQUEST.value(), APPOINTMENT_ALREADY_EXISTS, """
                        You've already booked your appointment at %s
                        """.formatted(appointmentTimeInReadableFormat(appointment))));
    }

    private String appointmentTimeInReadableFormat(PatientAppointment appointment) {
        return Instant.ofEpochMilli(appointment.getStartTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("YYYY-MM-DD at HHa"));
    }

    private AppointmentRequest appointmentRequest(AppointmentContext appointmentContext, String patientId,
            Map<String, String> arguments) {
        long startTime = startTimeInMillis(arguments);
        //taking default one hour for appointment
        long endTime = startTime + APPOINTMENT_DURATION;
        return AppointmentRequest.builder()
                .doctorId(appointmentContext.doctorId())
                .patientId(patientId)
                .startTime(startTime)
                .endTime(endTime).build();
    }

    private long startTimeInMillis(Map<String, String> arguments) {
        LocalDate startDay = LocalDate.parse(arguments.get("date"), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDateTime startDateTime = startDay.atTime(parseTime(arguments));
        long startTime = startDateTime.toInstant(UTC).toEpochMilli();
        return startTime;
    }

    @VisibleForTesting
    LocalTime parseTime(Map<String, String> arguments) {
        String time = arguments.get("time");
        try {
            if (isBlank(time)) {
                throw new VoiceBotException(BAD_REQUEST.value(), INVALID_INPUT, "Please provide time.");
            }
            time = time.strip().toLowerCase();
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("hh[ ][a]"));
        } catch (DateTimeParseException e) {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH[:MM][ ][a]"));
        }
    }

    private ChatRequest chatRequest(String userQuery) {
        List<ChatMessage> messages =
                new ArrayList<>(chatSessionCache.getUserSession(chatSessionProvider.get()).get()
                        .stream()
                        .collect(toSet()));
        messages.add(queryIntentMessage(userQuery));
        return ChatRequest.builder()
                .model(model)
                .functions(chatFunctions)
                .messages(messages)
                .build();
    }

    private static ChatMessage queryIntentMessage(String userQuery) {
        return ChatMessage.builder()
                .role("user")
                .content(userQuery)
                .build();
    }
}
