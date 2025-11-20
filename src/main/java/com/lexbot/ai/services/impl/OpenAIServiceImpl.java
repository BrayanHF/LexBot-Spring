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
public class OpenAIServiceImpl implements AIService {

    private final WebClient openAIWebClient;

    public OpenAIServiceImpl(WebClientFactory webClientFactory) {
        String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
        this.openAIWebClient = webClientFactory
            .createWebClient(
                "https://api.openai.com/v1",
                OPENAI_API_KEY
            );
    }

    private WebClient.ResponseSpec openAiRequest(AIChatRequest request) {
        return openAIWebClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve();
    }

    @Override
    public Mono<AIChatResponse> chat(AIChatRequest request) {
        request.setStream(false);
        return openAiRequest(request).bodyToMono(AIChatResponse.class);
    }

    @Override
    public Flux<AIChatResponse> chatStream(AIChatRequest request) {
        request.setStream(true);
        return openAiRequest(request)
            .bodyToFlux(AIChatResponse.class)
            .takeUntil(response -> response.getChoices().getFirst().getFinish_reason() != null);
    }

}
