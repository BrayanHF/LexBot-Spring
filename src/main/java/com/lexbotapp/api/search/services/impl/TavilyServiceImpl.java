package com.lexbotapp.api.search.services.impl;

import com.lexbotapp.api.search.dto.tavily.TVLSearchRequest;
import com.lexbotapp.api.search.dto.tavily.TVLSearchResponse;
import com.lexbotapp.api.search.services.WebSearchService;
import com.lexbotapp.api.utils.web.WebClientFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TavilyServiceImpl implements WebSearchService {

    private final WebClient tvlSearchWebClient;

    public TavilyServiceImpl(WebClientFactory webClientFactory) {
        String TAVILY_API_KEY = System.getenv("TAVILY_API_KEY");
        this.tvlSearchWebClient = webClientFactory
            .createWebClient(
                "https://api.tavily.com",
                TAVILY_API_KEY
            );
    }

    @Override
    public Mono<TVLSearchResponse> search(TVLSearchRequest request) {
        return this.tvlSearchWebClient
            .post()
            .uri("/search")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TVLSearchResponse.class);
    }
}
