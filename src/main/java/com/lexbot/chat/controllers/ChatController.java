package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.chat.ApiResponse;
import com.lexbot.chat.dto.chat.ChatUpdateRequest;
import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.services.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public Mono<ApiResponse<List<Chat>>> getChats(Authentication authentication) {
        return chatService
            .userChats(authentication.getName())
            .map(userChats -> ApiResponse.success(userChats, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("No se pudo obtener la información de los chats en este momento.", false)));
    }

    @GetMapping("chat-id/{chatId}")
    public Mono<ApiResponse<Chat>> getChatById(@PathVariable String chatId, Authentication authentication) {
        return chatService
            .getChatById(authentication.getName(), chatId)
            .map(chat -> ApiResponse.success(chat, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("No fue posible cargar los datos del chat solicitado.", false)));
    }

    @PatchMapping
    public Mono<ApiResponse<Map<String, Object>>> updateChat(@RequestBody ChatUpdateRequest request, Authentication authentication) {
        return chatService
            .updateChatByFields(authentication.getName(), request.getChatId(), request.getUpdates())
            .map(response -> ApiResponse.success(response, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Ocurrió un error al actualizar el chat. Inténtalo nuevamente.", false)));
    }

    @DeleteMapping("chat-id/{chatId}")
    public Mono<ApiResponse<Boolean>> deleteChat(@PathVariable String chatId, Authentication authentication) {
        return chatService
            .deleteChatById(authentication.getName(), chatId)
            .thenReturn(ApiResponse.success(true, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("No se pudo eliminar el chat. Por favor intenta de nuevo.", false)));
    }

}
