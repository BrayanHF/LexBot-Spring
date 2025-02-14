package com.lexbot.chat.controllers;

import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.chat.dto.ApiResponse;
import com.lexbot.chat.dto.ChatRequest;
import com.lexbot.chat.services.ChatOrchestratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("chat")
public class LBChatController {

    private final ChatOrchestratorService chatService;

    public LBChatController(ChatOrchestratorService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<AIChatResponse>>> chat(@RequestBody ChatRequest request) {
        // Todo: Get by JWT
        String userId = "test";

        return chatService
            .chat(userId, request.getChat_id(), request.getMessage())
            .map(
                response -> ResponseEntity.ok(
                    ApiResponse.success(response, false)
                )
            )
            .onErrorResume(
                e -> Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(
                            ApiResponse.error("Error in chat: " + e.getMessage(), false)
                        )
                )
            );
    }

}
