package com.lexbotapp.api.chat.controllers;

import com.lexbotapp.api.chat.dto.chat.ApiResponse;
import com.lexbotapp.api.chat.dto.chat.ChatRequest;
import com.lexbotapp.api.chat.dto.chat.ChattingResponse;
import com.lexbotapp.api.chat.services.chatting.ChatOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("chatting")
@RequiredArgsConstructor
public class LBChattingController {

    private final ChatOrchestratorService chatService;

    @PostMapping
    public Mono<ApiResponse<ChattingResponse>> chat(@RequestBody ChatRequest request, Authentication authentication) {
        return chatService
            .chat(authentication.getName(), request.getChatId(), request.getMessage())
            .map(response -> ApiResponse.success(response, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Ocurrió un error al procesar el mensaje. Por favor intenta nuevamente.", false)));
    }

    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ApiResponse<ChattingResponse>> chatStream(@RequestBody ChatRequest request, Authentication authentication) {
        return chatService
            .chatStream(authentication.getName(), request.getChatId(), request.getMessage())
            .map(response -> ApiResponse.success(response, true))
            .onErrorResume(e -> Flux.just(ApiResponse.error("Ocurrió un error en la conexión al procesar el mensaje. Intenta de nuevo.", true)));
    }

    @PostMapping(value = "newChat")
    public Mono<ApiResponse<String>> newChat(@RequestBody ChatRequest request, Authentication authentication) {
        return chatService
            .newChat(authentication.getName(), request.getChatId(), request.getMessage())
            .map(chatId -> ApiResponse.success(chatId, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("No fue posible crear el chat en este momento. Por favor inténtalo nuevamente.", false)));
    }

}
