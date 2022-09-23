package uk.gov.hmcts.reform.rsecheck.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import uk.gov.hmcts.reform.demo.Application;
import uk.gov.hmcts.reform.demo.domain.ParticipantStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static uk.gov.hmcts.reform.demo.domain.SubscriptionStatus.CONNECTED;
import static uk.gov.hmcts.reform.demo.domain.SubscriptionStatus.FOLLOWING;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class)
@AutoConfigureMockMvc
class ParticipantsControllerTest {

    private static final String SESSION_PATH = "/icp/sessions/";
    private static final String PARTICIPANTS_PATH = "/participants";
    private static final String FIRST_STATUS_JSON_PATH = "$[0].status";
    @Autowired
    private MockMvc mvc;

    @Value("${local.server.port}")
    private int port;
    private String url;

    private static final String PARTICIPANTS_PUBLISH = "/icp/participants/123";
    private static final String SUBSCRIBE_PARTICIPANTS = "/topic/participants/123";

    private CompletableFuture<Collection<Map<String, Object>>> completableFuture;

    @BeforeEach
    public void setup() {
        completableFuture = new CompletableFuture<>();
        url = "ws://localhost:" + port + "/icp/ws";
    }

    @Test
    void shouldShareStatusChanges() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = getStompSession(stompClient, "123");

        stompSession.subscribe(SUBSCRIBE_PARTICIPANTS, new CreateScreenChangeStompFrameHandler());
        stompSession.send(PARTICIPANTS_PUBLISH, new ParticipantStatus("Louis", "123", CONNECTED));

        Collection<Map<String, Object>> participantStatuses = completableFuture.get(10, SECONDS);

        assertEquals(participantStatuses.size(), 1, "Number of participant statuses not equal to one");
        Map<String, Object> participantStatus = participantStatuses.stream().toList().get(0);
        assertEquals(participantStatus.get("status"), FOLLOWING.toString(),
                     "Participant status not equal to following");
    }

    private StompSession getStompSession(WebSocketStompClient stompClient, String sessionId)
        throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        HttpHeaders headers = new HttpHeaders();
        headers.put("sessionId", Collections.singletonList(sessionId));
        return stompClient.connect(url, new WebSocketHttpHeaders(headers), new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void shouldSetFollowingWhenSubscribed() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String sessionId = UUID.randomUUID().toString();
        StompSession stompSession = getStompSession(stompClient, sessionId);

        stompSession.subscribe("/topic/participants/" + sessionId, new CreateScreenChangeStompFrameHandler());
        //      stompSession.send("/icp/participants/" + sessionId,
        //      new ParticipantStatus("Louis", sessionId, CONNECTED));
        Thread.sleep(100);
        mvc.perform(get(SESSION_PATH + sessionId + PARTICIPANTS_PATH))
                .andExpect(jsonPath(FIRST_STATUS_JSON_PATH, equalTo(FOLLOWING.toString())));
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void shouldSetConnectedWhenUnsubscribed() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String sessionId = UUID.randomUUID().toString();
        StompSession stompSession = getStompSession(stompClient, sessionId);

        StompSession.Subscription subscription =
                stompSession.subscribe("/topic/participants/" + sessionId, new CreateScreenChangeStompFrameHandler());

        Thread.sleep(100);

        mvc.perform(get(SESSION_PATH + sessionId + PARTICIPANTS_PATH))
                .andExpect(jsonPath(FIRST_STATUS_JSON_PATH, equalTo(FOLLOWING.toString())));

        subscription.unsubscribe();

        Thread.sleep(100); // Unsubscribe handler happens in another thread

        mvc.perform(get(SESSION_PATH + sessionId + PARTICIPANTS_PATH))
                .andExpect(jsonPath(FIRST_STATUS_JSON_PATH, equalTo(CONNECTED.toString())));
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void shouldRemoveParticipantWhenDisconnected() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String sessionId = UUID.randomUUID().toString();
        StompSession stompSession = getStompSession(stompClient, sessionId);

        stompSession.subscribe("/topic/participants/" + sessionId, new CreateScreenChangeStompFrameHandler());

        Thread.sleep(100);

        mvc.perform(get(SESSION_PATH + sessionId + PARTICIPANTS_PATH))
                .andExpect(jsonPath(FIRST_STATUS_JSON_PATH, equalTo(FOLLOWING.toString())));

        stompSession.disconnect();

        Thread.sleep(100); // Disconnect handler happens in another thread

        mvc.perform(get(SESSION_PATH + sessionId + PARTICIPANTS_PATH))
                .andExpect(jsonPath("$.length()", equalTo(0)));
    }

    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class CreateScreenChangeStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return Collection.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object payload) {
            @SuppressWarnings("unchecked")
            Collection<Map<String, Object>> collection = (Collection<Map<String, Object>>) payload;
            completableFuture.complete(collection);
        }
    }
}
