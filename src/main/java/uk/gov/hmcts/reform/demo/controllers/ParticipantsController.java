package uk.gov.hmcts.reform.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import uk.gov.hmcts.reform.demo.domain.ParticipantStatus;
import uk.gov.hmcts.reform.demo.services.ParticipantsStatusService;

import java.util.Collection;

@Controller
public class ParticipantsController {

    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public ParticipantsController(ParticipantsStatusService participantsStatusService) {
        this.participantsStatusService = participantsStatusService;
    }

    @MessageMapping("/participants/{sessionId}")
    @SendTo("/topic/participants/{sessionId}")
    public Collection<ParticipantStatus> statusChange(Message<ParticipantStatus> statusMessage) {
        String simpSessionid = statusMessage.getHeaders().get("simpSessionId").toString();
        return participantsStatusService.updateName(simpSessionid,
                statusMessage.getPayload().getSessionId(), statusMessage.getPayload().getName());
    }

}
