package com.ferdisonmezay.websocket.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferdisonmezay.websocket.server.dto.ClientDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ClientService {
    private final Map<String, ClientDto> clientRegistry = new HashMap<>();

    private final SimpMessagingTemplate messageTemplate;

    private final ObjectMapper objectMapper;

    public ClientService(SimpMessagingTemplate messageTemplate, ObjectMapper objectMapper) {
        this.messageTemplate = messageTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishToClient(String client, String data) {
        messageTemplate.convertAndSend(createDestination(client), data);
    }

    public void registerNewClient(String connectionSessionId, String clientId) {
        clientRegistry.put(connectionSessionId, new ClientDto(clientId));
        log.info("Client registered, total number of clients: {}", clientRegistry.size());
    }

    public void removeClient(String connectionSessionId) {
        log.info("Removing client with id {}", clientRegistry.get(connectionSessionId).getClientId());
        clientRegistry.remove(connectionSessionId);
    }

    public void publishClientList() {
        String data;
        try {
            data = objectMapper.writeValueAsString(new ArrayList<>(clientRegistry.values()));
            publishToClient("manager-ui", data);
        } catch (Exception e) {
            log.error("Error creating json!");
        }
    }

    public ClientDto getClientBySessionId(String sessionId) {
        if (!clientRegistry.containsKey(sessionId)) {
            throw new NoSuchElementException();
        }
        return clientRegistry.get(sessionId);
    }

    public ClientDto getClientByClientId(String clientId) {
        return clientRegistry.values().stream()
                .filter(i -> i.getClientId().equals(clientId))
                .findAny().orElseThrow(NoSuchElementException::new);
    }

    public String createDestination(String client) {
        return String.join("/", "", "client", client);
    }
}
