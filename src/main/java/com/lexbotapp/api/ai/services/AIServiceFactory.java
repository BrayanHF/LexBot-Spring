package com.lexbotapp.api.ai.services;

import com.lexbotapp.api.ai.dto.AIProvider;
import com.lexbotapp.api.ai.services.impl.DeepSeekServiceImpl;
import com.lexbotapp.api.ai.services.impl.OpenAIServiceImpl;
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
