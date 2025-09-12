package com.lexbot.chat.dto.chat;

import com.lexbot.ai.dto.response.AIChatResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChattingResponse {

    private String chatId;
    private AIChatResponse aiChatResponse;

}
