package uk.gov.hmcts.reform.demo.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.demo.domain.HearingSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HearingSessionService {

    private final Map<UUID, HearingSession> sessions = new ConcurrentHashMap<>();

    public HearingSession newSession(final HearingSession hearingSession) {
        final UUID uuid = UUID.randomUUID();
        hearingSession.setId(uuid);
        sessions.put(hearingSession.getId(), hearingSession);
        return hearingSession;
    }

    public HearingSession getSession(UUID id) {
        return sessions.get(id);
    }
}
