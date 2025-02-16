package com.lexbot.chat.controllers;

import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.chat.dto.ApiResponse;
import com.lexbot.chat.dto.ChatRequest;
import com.lexbot.chat.services.chatting.ChatOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("chatting")
@RequiredArgsConstructor
public class LBChattingController {

    // Todo: Get by JWT
    private static final String USER_ID = "test";

    private final ChatOrchestratorService chatService;

    @PostMapping
    public Mono<ApiResponse<AIChatResponse>> chat(@RequestBody ChatRequest request) {
        return chatService
            .chat(USER_ID, request.getChat_id(), request.getMessage())
            .map(response -> ApiResponse.success(response, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Error in chat: " + e.getMessage(), false)));
    }

    @PostMapping("stream")
    public Flux<ApiResponse<AIChatResponse>> chatStream(@RequestBody ChatRequest request) {
        return chatService
            .chatStream(USER_ID, request.getChat_id(), request.getMessage())
            .map(response -> ApiResponse.success(response, true))
            .onErrorResume(e -> Flux.just(ApiResponse.error("Error in chat stream: " + e.getMessage(), true)));
    }

}
