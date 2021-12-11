package com.ferdisonmezay.websocket.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientDto {

    private String clientId;
    private boolean active = false;

    public ClientDto(String clientId) {
        this.clientId = clientId;
    }
}
