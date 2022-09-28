package uk.gov.hmcts.reform.em.ped.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.em.ped.domain.HearingSession;
import uk.gov.hmcts.reform.em.ped.domain.ParticipantStatus;
import uk.gov.hmcts.reform.em.ped.services.HearingSessionService;
import uk.gov.hmcts.reform.em.ped.services.ParticipantsStatusService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

@RestController()
@RequestMapping("icp/sessions")
public class HearingSessionController {

    private final HearingSessionService hearingSessionService;
    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public HearingSessionController(HearingSessionService hearingSessionService,
                                    ParticipantsStatusService participantsStatusService) {
        this.hearingSessionService = hearingSessionService;
        this.participantsStatusService = participantsStatusService;
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HearingSession> newSession(
            @RequestBody final HearingSession hearingSession) throws URISyntaxException {
        String location = "/icp/sessions/" + this.hearingSessionService.newSession(hearingSession).getId();
        return ResponseEntity.created(new URI(location)).body(hearingSession);
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HearingSession> getSession(@PathVariable UUID id) {
        return ResponseEntity.ok(this.hearingSessionService.getSession(id));
    }


    @GetMapping(value = "{id}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ParticipantStatus> statusChange(@PathVariable UUID id) {
        return participantsStatusService.getStatus(id.toString());
    }
}
