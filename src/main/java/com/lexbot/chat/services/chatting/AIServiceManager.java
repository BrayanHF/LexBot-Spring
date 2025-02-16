package com.lexbot.chat.services.chatting;

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

    private static final String DEFAULT_PROMPT = "Eres un abogado";
    private static final String TITLE_PROMPT_PREFIX = "Dame un título muy corto del siguiente mensaje (no uses comillas): ";
    private static final int DEFAULT_TEMPERATURE = 0;
    private static final int MAX_TOKENS_CHAT = 200;
    private static final int MAX_TOKENS_TITLE = 100;

    private final ChatService chatService;

    public AIServiceManager(ChatService chatService) {
        this.chatService = chatService;
    }

    @Setter
    private AIService aiService;

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

    private AIChatRequest getAIChatRequest(String message) {

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = AIMessageRequest.builder()
            .role(Role.DEVELOPER)
            .content(DEFAULT_PROMPT)
            .build();

        return AIChatRequest.builder()
            .model(getModel())
            .messages(List.of(systemMessage, userMessage))
            .temperature(DEFAULT_TEMPERATURE)
            .max_tokens(MAX_TOKENS_CHAT)
            .build();
    }

    public Mono<AIChatResponse> generateAIMessage(String message) {
        var chatRequest = getAIChatRequest(message);
        return aiService.chat(chatRequest);
    }

    public Flux<AIChatResponse> generateStreamAIMessage(String message) {
        var chatRequest = getAIChatRequest(message);
        return aiService.chatStream(chatRequest);
    }

    public void generateTitleForChat(String userId, String chatId, String message) {

        String prompt = TITLE_PROMPT_PREFIX + message;

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(prompt)
            .build();

        var aiChatRequest = AIChatRequest.builder()
            .model(getModel())
            .messages(List.of(userMessage))
            .temperature(DEFAULT_TEMPERATURE)
            .max_tokens(MAX_TOKENS_TITLE)
            .build();

        aiService.chat(aiChatRequest)
            .map(AIChatResponse -> AIChatResponse.getChoices().getFirst().getResponse().getContent())
            .flatMap(title -> chatService.updateChatByFields(userId, chatId, Map.of("title", title)))
            .onErrorResume(e -> Mono.empty())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

}