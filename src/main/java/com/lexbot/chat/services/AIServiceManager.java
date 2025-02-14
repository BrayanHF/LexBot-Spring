package com.lexbot.chat.services;

import com.lexbot.ai.dto.Role;
import com.lexbot.ai.dto.request.AIChatRequest;
import com.lexbot.ai.dto.request.AIMessageRequest;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIService;
import com.lexbot.ai.services.impl.DeepSeekServiceImpl;
import com.lexbot.ai.services.impl.OpenAIServiceImpl;
import com.lexbot.data.services.ChatService;
import lombok.Setter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
public class AIServiceManager {

    @Setter
    private AIService aiService;

    private final ChatService chatService;

    public AIServiceManager(ChatService chatService) {
        this.chatService = chatService;
    }

    private String getModel() {
        switch (aiService) {
            case null -> throw new NullPointerException("AI service is null");

            case OpenAIServiceImpl ignored -> {
                return "gpt-4o-mini";
            }
            case DeepSeekServiceImpl ignored -> {
                return "deepseek-chat";
            }

            default -> throw new IllegalArgumentException("AI service not implemented yet");
        }
    }

    public Mono<AIChatResponse> generateAIMessage(String message) {

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = AIMessageRequest.builder()
            .role(Role.DEVELOPER)
            .content("Eres un abogado")
            .build();

        var chatRequest = AIChatRequest.builder()
            .model(getModel())
            .messages(List.of(systemMessage, userMessage))
            .temperature(0)
            .max_tokens(200)
            .build();

        return aiService.chat(chatRequest);
    }

    // Todo
    public Flux<AIChatResponse> generateStreamAIMessage(String message) {
        return Flux.empty();
    }

    public void generateTitleForChat(String userId, String chatId, String message) {

        String prompt = "Dame un titulo muy corto del siguiente mensaje (no uses comillas): " + message;

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(prompt)
            .build();

        var aiChatRequest = AIChatRequest.builder()
            .model(getModel())
            .messages(List.of(userMessage))
            .temperature(0)
            .max_tokens(100)
            .build();

        aiService.chat(aiChatRequest)
            .map(AIChatResponse -> AIChatResponse.getChoices().getFirst().getResponse().getContent())
            .flatMap(title -> chatService.updateChatByFields(userId, chatId, Map.of("title", title)))
            .onErrorResume(e -> Mono.empty())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

}