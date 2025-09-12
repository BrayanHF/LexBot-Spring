package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.chat.ApiResponse;
import com.lexbot.chat.dto.chat.ChatRequest;
import com.lexbot.chat.dto.chat.ChattingResponse;
import com.lexbot.chat.services.chatting.ChatOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("chatting")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LBChattingController {

    // Todo: Get by JWT
    private static final String USER_ID = "test";

    private final ChatOrchestratorService chatService;

    @PostMapping
    public Mono<ApiResponse<ChattingResponse>> chat(@RequestBody ChatRequest request) {
        return chatService
            .chat(USER_ID, request.getChatId(), request.getMessage())
            .map(response -> ApiResponse.success(response, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Error in chat: " + e.getMessage(), false)));
    }

    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ApiResponse<ChattingResponse>> chatStream(@RequestBody ChatRequest request) {
        return chatService
            .chatStream(USER_ID, request.getChatId(), request.getMessage())
            .map(response -> ApiResponse.success(response, true))
            .onErrorResume(e -> Flux.just(ApiResponse.error("Error in chat stream: " + e.getMessage(), true)));
    }

}
