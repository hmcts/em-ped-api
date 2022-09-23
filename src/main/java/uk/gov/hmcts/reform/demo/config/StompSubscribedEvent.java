package uk.gov.hmcts.reform.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import uk.gov.hmcts.reform.demo.domain.SubscriptionStatus;
import uk.gov.hmcts.reform.demo.services.ParticipantsStatusService;

import java.util.Objects;

import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.DESTINATION_HEADER;

@Component
public class StompSubscribedEvent implements ApplicationListener<SessionSubscribeEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StompSubscribedEvent.class);

    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public StompSubscribedEvent(ParticipantsStatusService participantsStatusService) {
        this.participantsStatusService = participantsStatusService;
    }

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        LOG.info(sessionId);
        String destinationTopic = Objects.requireNonNull(sha.getHeader(DESTINATION_HEADER)).toString();
        String hearingSessionId = destinationTopic.replaceAll("/.*/.*/", "");
        participantsStatusService.updateStatus(hearingSessionId, sessionId, SubscriptionStatus.FOLLOWING);
    }
}
