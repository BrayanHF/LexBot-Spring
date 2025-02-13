package com.lexbot.ia.services;

import com.lexbot.ia.dto.IAProvider;
import com.lexbot.ia.services.impl.DeepSeekServiceImpl;
import com.lexbot.ia.services.impl.OpenAIServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class IAServiceFactory {

    private final OpenAIServiceImpl openAIService;
    private final DeepSeekServiceImpl deepSeekService;

    public IAServiceFactory(OpenAIServiceImpl openAIService, DeepSeekServiceImpl deepSeekService) {
        this.openAIService = openAIService;
        this.deepSeekService = deepSeekService;
    }

    public IAService getIAService(IAProvider provider) {
        return switch (provider) {
            case OPEN_AI -> openAIService;
            case DEEP_SEEK -> deepSeekService;
        };
    }

}
