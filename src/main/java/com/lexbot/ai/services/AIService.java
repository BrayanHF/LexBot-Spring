package com.lexbot.ai.services;

import com.lexbot.ai.dto.request.AIChatRequest;
import com.lexbot.ai.dto.response.AIChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AIService {

    Mono<AIChatResponse> chat(AIChatRequest request);

    Flux<AIChatResponse> chatStream(AIChatRequest request);

}
