package com.lexbot.ai.services.impl;

import com.lexbot.ai.dto.request.AIChatRequest;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIService;
import com.lexbot.utils.web.WebClientFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DeepSeekServiceImpl implements AIService {

    private final WebClient dsWebClient;

    public DeepSeekServiceImpl(WebClientFactory webClientFactory) {
        String DEED_SEEK_API_KEY = System.getenv("DEED_SEEK_API_KEY");
        this.dsWebClient = webClientFactory
            .createWebClient(
                "https://api.deepseek.com",
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
        return dsRequest(request).bodyToFlux(AIChatResponse.class);
    }

}
