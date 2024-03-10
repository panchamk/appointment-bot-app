package com.pkg.appointmentscheduler.module.rest;

import static com.pkg.appointmentscheduler.common.Constants.CHAT_SESSION_HEADER;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pkg.appointmentscheduler.module.crud.entity.PatientAppointment;
import com.pkg.appointmentscheduler.module.crud.service.AppointmentService;
import com.pkg.appointmentscheduler.module.rest.context.AppointmentContext;
import com.pkg.appointmentscheduler.module.rest.entity.VoiceBotInitRequest;
import com.pkg.appointmentscheduler.module.rest.entity.VoiceBotUserQuery;
import com.pkg.appointmentscheduler.module.voicebot.openai.entity.ChatMessage;
import com.pkg.appointmentscheduler.module.voicebot.service.VoiceBotService;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = PRIVATE)
@RequestMapping(path = "/v1/appointment", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentController {

    VoiceBotService voiceBotService;

    @PostMapping("/init")
    public ResponseEntity<Map<String, String>> initiateChatBot(@RequestBody VoiceBotInitRequest voiceBotInitRequest) {
        Map<String, String> response = voiceBotService.initiateChatWithPatient(voiceBotInitRequest);
        return ResponseEntity.ok()
                .header(CHAT_SESSION_HEADER, response.get(CHAT_SESSION_HEADER))
                .body(response);
    }

    @PostMapping("/{doctorId}/query")
    public ChatMessage generateAnswer(@PathVariable("doctorId") String doctorId, @RequestBody VoiceBotUserQuery userQuery) {
        ChatMessage response = voiceBotService.userQuery(new AppointmentContext(doctorId), userQuery.message());
        log.info("user query: {}, resulted in response; {}", userQuery, response);
        return response;
    }

    @DeleteMapping("/{doctorId}/query/close")
    public void closeUserSession(@PathVariable("doctorId") String doctorId) {
        voiceBotService.closeUserSession();
    }

}
