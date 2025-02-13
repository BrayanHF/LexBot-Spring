package com.lexbot.chat.services;

import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.services.ChatService;
import com.lexbot.data.services.MessageService;
import com.lexbot.ia.dto.IAProvider;
import com.lexbot.ia.dto.Role;
import com.lexbot.ia.dto.request.IAChatRequest;
import com.lexbot.ia.dto.request.IAMessageRequest;
import com.lexbot.ia.dto.response.IAChatResponse;
import com.lexbot.ia.services.IAService;
import com.lexbot.ia.services.IAServiceFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

// Todo: layer of laws
@Service
public class ChatWithOpenAIService {

    private final String MINIMAL_AI_MODEL = "gpt-4o-mini";

    private final ChatService chatService;
    private final MessageService messageService;
    private final IAService openAIService;

    public ChatWithOpenAIService(ChatService chatService, MessageService messageService, IAServiceFactory iaServiceFactory) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.openAIService = iaServiceFactory.getIAService(IAProvider.OPEN_AI);
    }

    private Mono<IAChatResponse> generateAIMessage(String message) {

        var userMessage = IAMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = IAMessageRequest.builder()
            .role(Role.DEVELOPER)
            .content("Eres un abogado")
            .build();

        var chatRequest = IAChatRequest.builder()
            .model(MINIMAL_AI_MODEL)
            .messages(List.of(systemMessage, userMessage))
            .temperature(0)
            .max_tokens(200)
            .build();

        return openAIService.chat(chatRequest);
    }

    private void generateTitleForChat(String userId, String chatId, String message) {

        String prompt = "Dame un titulo muy corto del siguiente mensaje (no uses comillas): " + message;

        var userMessage = IAMessageRequest.builder()
            .role(Role.USER)
            .content(prompt)
            .build();

        var chatRequest = IAChatRequest.builder()
            .model(MINIMAL_AI_MODEL)
            .messages(List.of(userMessage))
            .temperature(0)
            .max_tokens(100)
            .build();

        openAIService.chat(chatRequest)
            .map(iaChatResponse -> iaChatResponse.getChoices().getFirst().getMessage().getContent())
            .flatMap(title -> chatService.updateChatByFields(userId, chatId, Map.of("title", title)))
            .onErrorResume(e -> Mono.empty())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    private Mono<Chat> getOrCreateChat(String userId, String chatId, String message) {
        if (chatId != null && !chatId.isEmpty()) return Mono.just(Chat.builder().id(chatId).build());

        return chatService.addChat(userId)
            .flatMap(
                chat -> {
                    generateTitleForChat(userId, chat.getId(), message);
                    return Mono.just(chat);
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Error creating a new chat", e)));
    }

    private Mono<Void> saveMessages(String userId, String chatId, String userMessageText, String assistantMessageText) {
        var userMessage = Message.builder()
            .role(Role.USER)
            .text(userMessageText)
            .build();

        var assistantMessage = Message.builder()
            .role(Role.ASSISTANT)
            .text(assistantMessageText)
            .build();

        return Mono.when(
            messageService.addMessage(userId, chatId, userMessage)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error saving user message", e))),

            messageService.addMessage(userId, chatId, assistantMessage)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error saving assistant message", e)))
        );
    }

    public Mono<String> chat(String userId, String chatId, String userMessage) {
        return Mono.zip(
                getOrCreateChat(userId, chatId, userMessage),
                generateAIMessage(userMessage)
            )
            .flatMap(
                tuple -> {
                    String chat_id = tuple.getT1().getId();
                    String assistantMessage = tuple.getT2().getChoices().getFirst().getMessage().getContent();

                    return saveMessages(userId, chat_id, userMessage, assistantMessage)
                        .thenReturn(assistantMessage);
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Chat process error", e)));
    }

}
