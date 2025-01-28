package com.lexbot.ia.services.impl;

import com.lexbot.ia.models.IAChatRequest;
import com.lexbot.ia.services.IAService;
import com.lexbot.ia.web_client.WebClientFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OpenAIServiceImpl implements IAService {

    private final WebClientFactory webClientFactory;

    public OpenAIServiceImpl(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    @Override
    public Mono<String> chat(IAChatRequest request) {
        String OPENAI_API_KEY = Dotenv.load().get("OPENAI_API_KEY");
        var openAIWebClient = webClientFactory
            .createWebClient(
                "https://api.openai.com/v1",
                OPENAI_API_KEY
            );

        return openAIWebClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class);
    }

}
