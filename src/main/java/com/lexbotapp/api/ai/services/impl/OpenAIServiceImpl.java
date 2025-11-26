package com.lexbotapp.api.ai.services.impl;

import com.lexbotapp.api.ai.dto.request.AIChatRequest;
import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import com.lexbotapp.api.ai.services.AIService;
import com.lexbotapp.api.utils.web.WebClientFactory;
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
        request.setModel("gpt-4o-mini");
        return openAiRequest(request).bodyToMono(AIChatResponse.class);
    }

    @Override
    public Flux<AIChatResponse> chatStream(AIChatRequest request) {
        request.setStream(true);
        request.setModel("gpt-4o-mini");
        return openAiRequest(request)
            .bodyToFlux(AIChatResponse.class)
            .takeUntil(response -> response.getChoices().getFirst().getFinish_reason() != null);
    }

}
