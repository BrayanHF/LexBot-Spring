package com.lexbot.ai.services;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.services.impl.DeepSeekServiceImpl;
import com.lexbot.ai.services.impl.OpenAIServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class AIServiceFactory {

    private final OpenAIServiceImpl openAIService;
    private final DeepSeekServiceImpl deepSeekService;

    public AIServiceFactory(OpenAIServiceImpl openAIService, DeepSeekServiceImpl deepSeekService) {
        this.openAIService = openAIService;
        this.deepSeekService = deepSeekService;
    }

    public AIService getAIService(AIProvider provider) {
        return switch (provider) {
            case OPEN_AI -> openAIService;
            case DEEP_SEEK -> deepSeekService;
        };
    }

}
