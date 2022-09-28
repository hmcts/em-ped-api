package uk.gov.hmcts.reform.em.ped.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.em.ped.Application;
import uk.gov.hmcts.reform.em.ped.domain.HearingSession;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class)
@AutoConfigureMockMvc
class HearingSessionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPersistASession() throws Exception {

        HearingSession session =
                new HearingSession("My hearing",
                        LocalDateTime.now(),
                                   List.of("http://docs.com/documents/123"),
                                   List.of("lh@test.com")
                );
        String locationHeader = mvc.perform(post("/icp/sessions")
                .content(objectMapper.writeValueAsString(session))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/icp/sessions/")))
                .andReturn()
                .getResponse()
                .getHeader("location");

        assertNotNull(locationHeader, "Location header is null");
        mvc.perform(get(locationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", equalTo(session.getDescription())));
    }
}
