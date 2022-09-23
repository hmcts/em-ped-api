package uk.gov.hmcts.reform.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import uk.gov.hmcts.reform.demo.services.ParticipantsStatusService;

@Component
public class StompUnsubscribedEvent implements ApplicationListener<SessionUnsubscribeEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StompUnsubscribedEvent.class);

    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public StompUnsubscribedEvent(ParticipantsStatusService participantsStatusService) {
        this.participantsStatusService = participantsStatusService;
    }

    @Override
    public void onApplicationEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        LOG.info(sessionId);
        participantsStatusService.unsubscribeParticipant(sessionId);
    }
}
