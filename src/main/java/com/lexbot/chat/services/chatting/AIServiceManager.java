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

    private static final int DEFAULT_TEMPERATURE = 0;
    private static final int TEMPERATURE_FOR_UNLIMITED = 1;
    private static final int MAX_TOKENS_CHAT_LIMITED = 200;
    private static final int MAX_TOKENS_CHAT_UNLIMITED = 2000;
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

    private AIChatRequest getAIChatRequest(String message, String prompt, boolean limited, int temperature) {

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = AIMessageRequest.builder()
            .role(Role.DEVELOPER)
            .content(prompt)
            .build();

        return AIChatRequest.builder()
            .model(getModel())
            .messages(List.of(systemMessage, userMessage))
            .temperature(temperature)
            .max_tokens(limited ? MAX_TOKENS_CHAT_LIMITED : MAX_TOKENS_CHAT_UNLIMITED)
            .build();
    }

    public Mono<AIChatResponse> generateAIMessageLimited(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, true, DEFAULT_TEMPERATURE);
        return aiService.chat(chatRequest);
    }

    public Mono<AIChatResponse> generateAIMessageUnlimited(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, false, TEMPERATURE_FOR_UNLIMITED);
        return aiService.chat(chatRequest);
    }

    public Flux<AIChatResponse> generateStreamAIMessage(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, false, DEFAULT_TEMPERATURE);
        return aiService.chatStream(chatRequest);
    }

    public void generateTitleForChat(String userId, String chatId, String message) {

        String prompt = "Dame un título muy corto del siguiente mensaje (no uses comillas): " + message;


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