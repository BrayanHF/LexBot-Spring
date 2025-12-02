package com.lexbotapp.api.ai.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexbotapp.api.ai.dto.request.AIChatRequest;
import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import com.lexbotapp.api.ai.services.AIService;
import com.lexbotapp.api.utils.web.WebClientFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DeepSeekServiceImpl implements AIService {

    private final WebClient dsWebClient;

    public DeepSeekServiceImpl(WebClientFactory webClientFactory, ObjectMapper objectMapper) {
        String DEED_SEEK_API_KEY = System.getenv("DEED_SEEK_API_KEY");
        this.dsWebClient = webClientFactory
            .createWebClient(
                "https://api.deepseek.com/v1",
                DEED_SEEK_API_KEY
            );
    }

    private WebClient.ResponseSpec dsRequest(AIChatRequest request) {
        return dsWebClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve();
    }

    @Override
    public Mono<AIChatResponse> chat(AIChatRequest request) {
        request.setStream(false);
        return dsRequest(request).bodyToMono(AIChatResponse.class);
    }

    @Override
    public Flux<AIChatResponse> chatStream(AIChatRequest request) {
        request.setStream(true);
        return dsRequest(request)
            .bodyToFlux(AIChatResponse.class)
            .takeUntil(response -> response.getChoices().getFirst().getFinish_reason() != null);
    }

}
