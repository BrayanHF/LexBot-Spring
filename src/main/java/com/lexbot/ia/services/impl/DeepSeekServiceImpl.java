package com.lexbot.ia.services.impl;

import com.lexbot.ia.models.IAChatRequest;
import com.lexbot.ia.services.IAService;
import com.lexbot.ia.web_client.WebClientFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeepSeekServiceImpl implements IAService {

    private final WebClientFactory webClientFactory;

    public DeepSeekServiceImpl(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }


    @Override
    public Mono<String> chat(IAChatRequest request) {
        return null;
    }
}
