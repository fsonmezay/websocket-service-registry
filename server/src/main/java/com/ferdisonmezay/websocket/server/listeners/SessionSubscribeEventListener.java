package com.ferdisonmezay.websocket.server.listeners;

import com.ferdisonmezay.websocket.server.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SessionSubscribeEventListener implements ApplicationListener<SessionSubscribeEvent> {

    private final ClientService clientService;

    public SessionSubscribeEventListener(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {

        MessageHeaders headers = event.getMessage().getHeaders();
        String sessionId = (String) headers.get("simpSessionId");
        Map<String, Object> nativeHeaders = (Map<String, Object>) headers.get("nativeHeaders");
        List<String> destinations = (List<String>) nativeHeaders.get("destination");
        String clientId = destinations.get(0).substring(8);

        log.info("Received subscribe event, clientId: {}, sessionId: {}", clientId, sessionId);
        clientService.registerNewClient(sessionId, clientId);
        clientService.publishClientList();
    }
}
