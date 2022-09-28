package uk.gov.hmcts.reform.em.ped.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ParticipantStatus {
    private final String name;
    private final SubscriptionStatus status;
    private final String sessionId;

    @JsonCreator
    public ParticipantStatus(@JsonProperty("name") String name,
                             @JsonProperty("sessionId") String sessionId,
                             @JsonProperty("status") SubscriptionStatus status) {
        this.name = name;
        this.status = status;
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ParticipantStatus updateName(String name) {
        return new ParticipantStatus(name, this.sessionId, this.status);
    }

    public ParticipantStatus updateStatus(SubscriptionStatus status) {
        return new ParticipantStatus(name, this.sessionId, status);
    }
}
