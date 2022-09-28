package uk.gov.hmcts.reform.rsecheck.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import uk.gov.hmcts.reform.em.ped.Application;
import uk.gov.hmcts.reform.em.ped.domain.ScreenChange;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class)
class UpdateControllerTest {

    @Value("${local.server.port}")
    private int port;
    private String url;
    private static final String SCREEN_CHANGE_PUBLISH = "/icp/screen-change/123";
    private static final String SUBSCRIBE_SCREEN_CHANGE = "/topic/screen-change/123";

    private CompletableFuture<ScreenChange> completableFuture;

    @BeforeEach
    public void setup() {
        completableFuture = new CompletableFuture<>();
        url = "ws://localhost:" + port + "/icp/ws";
    }

    @Test
    void shouldSharePageChanges() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSession stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);

        stompSession.subscribe(SUBSCRIBE_SCREEN_CHANGE, new CreateScreenChangeStompFrameHandler());
        stompSession.send(SCREEN_CHANGE_PUBLISH, new ScreenChange(2, "http://doc.com/documents/123"));

        ScreenChange screenChange = completableFuture.get(10, SECONDS);

        assertEquals(screenChange.getPage(), 2, "Page not equal to 2");
        assertEquals(screenChange.getDocument(), "http://doc.com/documents/123",
                     "Document not equal to expected");
    }



    private List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private class CreateScreenChangeStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return ScreenChange.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object payload) {
            completableFuture.complete((ScreenChange) payload);
        }
    }
}
