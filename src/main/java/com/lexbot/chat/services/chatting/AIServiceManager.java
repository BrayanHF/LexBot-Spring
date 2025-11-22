package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.Role;
import com.lexbot.ai.dto.request.AIChatRequest;
import com.lexbot.ai.dto.request.AIMessageRequest;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIService;
import com.lexbot.data.services.ChatService;
import com.lexbot.utils.prompts.chat.ChatPrompt;
import lombok.Setter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
public class AIServiceManager {

    private static final double DEFAULT_TEMPERATURE = 1.0;
    private static final int MAX_TOKENS_CHAT_LIMITED = 5000;

    private final ChatService chatService;

    public AIServiceManager(ChatService chatService) {
        this.chatService = chatService;
    }

    @Setter
    private AIService aiService;

    private AIChatRequest getAIChatRequest(String message, String prompt, boolean limited) {

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = AIMessageRequest.builder()
            .role(Role.SYSTEM)
            .content(prompt)
            .build();

        AIChatRequest request = AIChatRequest.builder()
            .messages(List.of(systemMessage, userMessage))
            .build();

        if (limited) {
            request.setTemperature(DEFAULT_TEMPERATURE);
            request.setMax_tokens(MAX_TOKENS_CHAT_LIMITED);
        }

        return request;
    }

    public Mono<AIChatResponse> generateAIMessageLimited(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, true);
        return aiService.chat(chatRequest);
    }

    public Mono<AIChatResponse> generateAIMessageUnlimited(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, false);
        return aiService.chat(chatRequest);
    }

    public Flux<AIChatResponse> generateStreamAIMessage(String message, String prompt) {
        var chatRequest = getAIChatRequest(message, prompt, false);
        return aiService.chatStream(chatRequest);
    }

    public void generateTitleForChat(String userId, String chatId, String message) {

        var aiChatRequest = getAIChatRequest(message, ChatPrompt.CHAT_TITLE_PROMPT, true);

        aiService.chat(aiChatRequest)
            .map(AIChatResponse -> AIChatResponse.getChoices().getFirst().getResponse().getContent())
            .flatMap(title -> chatService.updateChatByFields(userId, chatId, Map.of("title", title)))
            .onErrorResume(e -> Mono.empty())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

}