package com.lexbotapp.api.chat.controllers;

import com.lexbotapp.api.chat.dto.chat.ApiResponse;
import com.lexbotapp.api.data.firestore_dao.Message;
import com.lexbotapp.api.data.services.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
            .onErrorResume(e -> {
                if ("chat not found".equals(e.getMessage())) {
                    return Mono.just(ApiResponse.error(
                        "Este chat no existe o fue eliminado",
                        false
                    ));
                }
                return Mono.just(ApiResponse.error(
                    "No se pudieron obtener los mensajes en este momento. Por favor intenta nuevamente",
                    false
                ));
            });
    }

}
