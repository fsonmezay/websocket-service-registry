package com.ferdisonmezay.websocket.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WebsocketClient {

    private static final String SERVER_URL = "ws://localhost:8080/server";
    private static boolean connectedToServer = false;

    private static boolean enabled = false; //we will change value of this variable using management UI

    @Scheduled(fixedDelay = (5 * 1000)) // 5 seconds
    public void scheduledConnectionCheck() {
        log.info("Scheduled check for connection! Current value of 'enabled' flag is {}", enabled);
        if(!connectedToServer) {
            connectToServer();
        }
    }

    public void connectToServer() {
        log.info("Trying to connect to the server!");
        String clientId = UUID.randomUUID().toString();
        StompSession stompSession;
        try {
            stompSession = connect().get();
            subscribe(stompSession, clientId);
            connectedToServer = true;
            log.info("Connection established to {}", SERVER_URL);
        } catch (Exception e) {
            connectedToServer = false;
            log.error("Failed to connect to {}", SERVER_URL);
        }
    }

    private void subscribe(StompSession stompSession, String clientId) {
        stompSession.subscribe("/client/" + clientId, new StompFrameHandler() {

            public Type getPayloadType(StompHeaders stompHeaders) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders stompHeaders, Object o) {
                String command = new String((byte[]) o);
                log.info("Received: {}", command);
                if (command.equals("INVERT")) {
                    enabled = !enabled;
                    notifyServer(stompSession);
                }
            }
        });
    }

    private void notifyServer(StompSession stompSession) {
        stompSession.send("/app/update", (enabled ? "true" : "false").getBytes(StandardCharsets.UTF_8));
    }

    private ListenableFuture<StompSession> connect() {

        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        return stompClient.connect(SERVER_URL, new WebSocketHttpHeaders(), new CustomHandler());
    }

    private static class CustomHandler extends StompSessionHandlerAdapter {
        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            log.warn("Connection closed!");
            connectedToServer = false;
            super.handleTransportError(session, exception);
        }
    }
}

