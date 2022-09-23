package uk.gov.hmcts.reform.demo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScreenChange {
    private final int page;
    private final String document;

    @JsonCreator
    public ScreenChange(@JsonProperty("page") int page,
                        @JsonProperty("document") String document) {
        this.page = page;
        this.document = document;
    }

    public int getPage() {
        return page;
    }

    public String getDocument() {
        return document;
    }
}
