package com.lexbotapp.api.ai.services;

import com.lexbotapp.api.ai.dto.request.AIChatRequest;
import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AIService {

    Mono<AIChatResponse> chat(AIChatRequest request);

    Flux<AIChatResponse> chatStream(AIChatRequest request);

}
