package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.ApiResponse;
import com.lexbot.chat.dto.ChatUpdateRequest;
import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.services.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("chat")
@AllArgsConstructor
public class ChatController {

    // Todo: Get by JWT
    private static final String USER_ID = "test";

    private final ChatService chatService;

    @GetMapping
    public Mono<ApiResponse<List<Chat>>> getChats() {
        return chatService
            .userChats(USER_ID)
            .map(userChats -> ApiResponse.success(userChats, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error(e.getMessage(), false)));
    }

    @PatchMapping
    public Mono<ApiResponse<Map<String, Object>>> updateChat(@RequestBody ChatUpdateRequest request) {
        return chatService
            .updateChatByFields(USER_ID, request.getChatId(), request.getUpdates())
            .map(response -> ApiResponse.success(response, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Error updating chat: " + e.getMessage(), false)));
    }

    @DeleteMapping
    public Mono<ApiResponse<String>> deleteChat(@RequestBody String chatId) {
        return chatService
            .deleteChatById(USER_ID, chatId)
            .thenReturn(ApiResponse.success("Deleted chat", false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Error deleting chat: " + e.getMessage(), false)));
    }

}
