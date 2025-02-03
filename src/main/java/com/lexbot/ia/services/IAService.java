package com.lexbot.ia.services;

import com.lexbot.ia.dto.request.IAChatRequest;
import com.lexbot.ia.dto.response.IAChatResponse;
import reactor.core.publisher.Mono;

public interface IAService {

    Mono<IAChatResponse> chat(IAChatRequest request);

}
