package com.lexbotapp.api.chat.dto.chat;

import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChattingResponse {

    private String chatId;
    private AIChatResponse aiChatResponse;

}
