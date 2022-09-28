package uk.gov.hmcts.reform.em.ped.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import uk.gov.hmcts.reform.em.ped.services.ParticipantsStatusService;

@Component
public class StompDisconnectedEvent implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StompDisconnectedEvent.class);

    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public StompDisconnectedEvent(ParticipantsStatusService participantsStatusService) {
        this.participantsStatusService = participantsStatusService;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        LOG.info(sessionId);
        participantsStatusService.removeParticipant(sessionId);
    }
}
