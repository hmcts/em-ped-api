package uk.gov.hmcts.reform.em.ped.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class HearingSession {

    private UUID id;
    private String description;
    private LocalDateTime dateOfHearing;
    private List<String> participants;
    private List<String> documents;

    public HearingSession(UUID id,
                          String description,
                          LocalDateTime dateOfHearing,
                          List<String> documents,
                          List<String> participants) {
        this.id = id;
        this.description = description;
        this.dateOfHearing = dateOfHearing;
        this.documents = documents;
        this.participants = participants;
    }

    public HearingSession(String description, LocalDateTime dateOfHearing,
                          List<String> documents, List<String> participants) {
        this.description = description;
        this.dateOfHearing = dateOfHearing;
        this.documents = documents;
        this.participants = participants;
    }

    public HearingSession() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }


    public LocalDateTime getDateOfHearing() {
        return dateOfHearing;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateOfHearing(LocalDateTime dateOfHearing) {
        this.dateOfHearing = dateOfHearing;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }
}
