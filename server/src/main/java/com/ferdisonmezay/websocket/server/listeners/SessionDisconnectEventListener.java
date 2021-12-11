package com.ferdisonmezay.websocket.server.listeners;

import com.ferdisonmezay.websocket.server.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class SessionDisconnectEventListener implements ApplicationListener<SessionDisconnectEvent> {

    private final ClientService clientService;

    public SessionDisconnectEventListener(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        log.info("Received disconnect event for session {}", event.getSessionId());
        clientService.removeClient(event.getSessionId());
        clientService.publishClientList();
    }
}
