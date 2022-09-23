package uk.gov.hmcts.reform.demo.config;

import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import uk.gov.hmcts.reform.demo.domain.SubscriptionStatus;
import uk.gov.hmcts.reform.demo.services.ParticipantsStatusService;

import java.util.List;
import java.util.Optional;

import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER;

@Component
@Slf4j
public class StompConnectedEvent implements ApplicationListener<SessionConnectedEvent> {

    private final ParticipantsStatusService participantsStatusService;

    @Autowired
    public StompConnectedEvent(ParticipantsStatusService participantsStatusService) {
        this.participantsStatusService = participantsStatusService;
    }

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String httpSessionId = sha.getSessionId();
        Optional<String> hearingSessionId = extractHearingSessionId(sha);
        hearingSessionId.ifPresent(
            s -> participantsStatusService.updateStatus(s, httpSessionId, SubscriptionStatus.CONNECTED));
    }

    private Optional<String> extractHearingSessionId(StompHeaderAccessor sha) {
        StompHeaderAccessor simpConnectMessage =
                StompHeaderAccessor.wrap((Message<?>) sha.getHeader(CONNECT_MESSAGE_HEADER));
        List<String> sessionId = simpConnectMessage.getNativeHeader("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessionId.get(0));
    }
}
