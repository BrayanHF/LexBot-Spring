package com.lexbot.search.services.impl;

import com.lexbot.search.dto.tavily.TVLSearchRequest;
import com.lexbot.search.dto.tavily.TVLSearchResponse;
import com.lexbot.search.services.WebSearchService;
import com.lexbot.utils.web.WebClientFactory;
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
