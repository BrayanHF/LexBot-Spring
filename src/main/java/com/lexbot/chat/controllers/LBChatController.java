package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.ApiResponse;
import com.lexbot.chat.dto.ChatRequest;
import com.lexbot.chat.services.ChatWithOpenAIService;
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

    private final ChatWithOpenAIService chatService;

    public LBChatController(ChatWithOpenAIService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<String>>> chat(@RequestBody ChatRequest request) {
        // Todo: Get by JWT
        String userId = "test";

        return chatService.chat(userId, request.getChat_id(), request.getMessage())
            .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error en el chat: " + e.getMessage()))));
    }

}
