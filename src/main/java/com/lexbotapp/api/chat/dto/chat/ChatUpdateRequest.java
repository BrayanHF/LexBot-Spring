package com.lexbotapp.api.chat.dto.chat;

import lombok.Data;

import java.util.Map;

@Data
public class ChatUpdateRequest {

    private String chatId;
    private Map<String, Object> updates;

}