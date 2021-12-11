package com.ferdisonmezay.websocket.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {
    @InjectMocks
    private ClientService clientService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldPublishEvent() {
        clientService.publishToClient("myClient", "myEvent");
        verify(simpMessagingTemplate).convertAndSend("/client/myClient", "myEvent");
    }

    @Test
    void shouldCreateCorrectDestination() {
        assertEquals("/client/sample", clientService.createDestination("sample"));
    }

    @Test
    void getClientByIdShouldReturnExistingClient() {
        clientService.registerNewClient("sessionId", "clientId");
        assertEquals("clientId", clientService.getClientByClientId("clientId").getClientId());
        clientService.removeClient("sessionId");
    }

    @Test
    void getClientByIdShouldThrowWhenClientDoesNotExist() {
        assertThrows(NoSuchElementException.class, () -> clientService.getClientByClientId("invalidClientId"));
    }

    @Test
    void getClientBySessionIdShouldReturnExistingClient() {
        clientService.registerNewClient("sessionId1", "clientId1");
        assertEquals("clientId1", clientService.getClientBySessionId("sessionId1").getClientId());
        clientService.removeClient("sessionId1");
    }

    @Test
    void getClientBySessionIdShouldThrowWhenClientDoesNotExist() {
        assertThrows(NoSuchElementException.class, () -> clientService.getClientBySessionId("invalidSessionId"));
    }

    @Test
    void registerNewClientAddsItemToRegistry() {
        clientService.registerNewClient("sessionId2", "clientId2");
        assertNotNull(clientService.getClientByClientId("clientId2"));
        assertNotNull(clientService.getClientBySessionId("sessionId2"));
        clientService.removeClient("sessionId2");
    }

    @Test
    void removeClientRemovesFromRegistry() {
        clientService.registerNewClient("sessionId3", "clientId3");
        clientService.removeClient("sessionId3");
        assertThrows(NoSuchElementException.class, () -> clientService.getClientByClientId("clientId3"));
        assertThrows(NoSuchElementException.class, () -> clientService.getClientBySessionId("sessionId3"));
    }

    @Test
    void publishClientListSendsListToManagerUi() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("dataToSend");
        clientService.publishClientList();
        verify(simpMessagingTemplate).convertAndSend("/client/manager-ui", "dataToSend");
    }

    @Test
    void publishClientListDoesNotCallMessageTempleateOnError() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException());
        clientService.publishClientList();
        verify(simpMessagingTemplate, never()).convertAndSend("/client/manager-ui", "dataToSend");
    }
}