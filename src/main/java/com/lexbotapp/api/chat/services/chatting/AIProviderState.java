package com.lexbotapp.api.chat.services.chatting;

import com.lexbotapp.api.ai.dto.AIProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class AIProviderState {
    @Getter
    @Setter
    private AIProvider currentProvider = AIProvider.OPEN_AI;
}
