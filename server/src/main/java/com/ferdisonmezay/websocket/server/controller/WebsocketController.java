package com.ferdisonmezay.websocket.server.controller;

import com.ferdisonmezay.websocket.server.dto.ClientDto;
import com.ferdisonmezay.websocket.server.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.NoSuchElementException;

@Slf4j
@Controller
public class WebsocketController {
    private final ClientService clientService;

    public WebsocketController(ClientService clientService) {
        this.clientService = clientService;
    }

    @MessageMapping("/{clientId}")
    public void processMessageFromClient(@Payload String message, @DestinationVariable String clientId) {
        log.info("Received: {} for client: {}", message, clientId);
        try {
            ClientDto clientDto = clientService.getClientByClientId(clientId);
            clientService.publishToClient(clientDto.getClientId(), message);
        }
        catch (NoSuchElementException e) {
            log.error("Client {} could not be found!", clientId);
        }
    }

    @MessageMapping("/update")
    public void updateClientStatus(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = (String) headerAccessor.getHeader("simpSessionId");
        try {
            ClientDto clientDto = clientService.getClientBySessionId(sessionId);
            clientDto.setActive(Boolean.parseBoolean(message));
            clientService.publishClientList();
            log.info("Received: {} from client: {}", message, clientDto.getClientId());
        }
        catch (NoSuchElementException e) {
            log.error("Session {} could not be found!", sessionId);
        }
    }
}
