package com.lexbot.search.services;

import com.lexbot.search.dto.tavily.TVLSearchRequest;
import com.lexbot.search.dto.tavily.TVLSearchResponse;
import reactor.core.publisher.Mono;

public interface WebSearchService {

    Mono<TVLSearchResponse> search(TVLSearchRequest request);

}
