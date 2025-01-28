package com.lexbot.ia.services;

import com.lexbot.ia.models.IAProvider;
import com.lexbot.ia.services.impl.DeepSeekServiceImpl;
import com.lexbot.ia.services.impl.OpenAIServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IAServiceFactory {

    private final OpenAIServiceImpl openAIService;
    private final DeepSeekServiceImpl deepSeekService;

    public IAService getIAService(IAProvider provider) {
        return switch (provider) {
            case OPEN_AI -> openAIService;
            case DEEP_SEEK -> deepSeekService;
        };
    }

}
