package com.lexbotapp.api.search.services;

import com.lexbotapp.api.search.dto.tavily.TVLSearchRequest;
import com.lexbotapp.api.search.dto.tavily.TVLSearchResponse;
import reactor.core.publisher.Mono;

public interface WebSearchService {

    Mono<TVLSearchResponse> search(TVLSearchRequest request);

}
