package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.chat.ApiResponse;
import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.services.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("messages")
@AllArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("chat-id/{chatId}")
    public Mono<ApiResponse<List<Message>>> getMessages(@PathVariable String chatId, Authentication authentication) {
        return messageService
            .chatMessages(authentication.getName(), chatId)
            .map(messages -> ApiResponse.success(messages, false))
            .switchIfEmpty(Mono.just(ApiResponse.error("No messages found", false)))
            .onErrorResume(e -> Mono.just(ApiResponse.error("Error fetching messages: " + e.getMessage(), false)));
    }

}
