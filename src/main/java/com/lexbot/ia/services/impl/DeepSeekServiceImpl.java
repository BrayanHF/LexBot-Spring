package com.lexbot.ia.services.impl;

import com.lexbot.ia.dto.request.IAChatRequest;
import com.lexbot.ia.dto.response.IAChatResponse;
import com.lexbot.ia.services.IAService;
import com.lexbot.ia.web_client.WebClientFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeepSeekServiceImpl implements IAService {

    private final WebClientFactory webClientFactory;

    public DeepSeekServiceImpl(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }

    @Override
    public Mono<IAChatResponse> chat(IAChatRequest request) {
        String DEED_SEEK_API_KEY = Dotenv.load().get("DEED_SEEK_API_KEY");
        var openAIWebClient = webClientFactory
            .createWebClient(
                "https://api.deepseek.com",
                DEED_SEEK_API_KEY
            );

        return openAIWebClient
            .post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(IAChatResponse.class);
    }

}
