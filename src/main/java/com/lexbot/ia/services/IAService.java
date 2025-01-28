package com.lexbot.ia.services;

import com.lexbot.ia.models.IAChatRequest;
import reactor.core.publisher.Mono;

public interface IAService {

    Mono<String> chat(IAChatRequest request);

}
