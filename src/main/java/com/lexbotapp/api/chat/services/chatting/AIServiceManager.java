package com.lexbotapp.api.chat.services.chatting;

import com.lexbotapp.api.ai.dto.AIProvider;
import com.lexbotapp.api.ai.dto.Role;
import com.lexbotapp.api.ai.dto.request.AIChatRequest;
import com.lexbotapp.api.ai.dto.request.AIMessageRequest;
import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import com.lexbotapp.api.ai.services.AIService;
import com.lexbotapp.api.data.services.ChatService;
import com.lexbotapp.api.utils.prompts.chat.ChatPrompt;
import lombok.Setter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
public class AIServiceManager {


    private final ChatService chatService;
    private final AIProviderState aiProviderState;

    @Setter
    private AIService aiService;

    public AIServiceManager(
        ChatService chatService,
        AIProviderState aiProviderState
    ) {
        this.chatService = chatService;
        this.aiProviderState = aiProviderState;
    }

    public String getAiModel(AIProvider aiProvider, boolean limited) {
        if (limited) {
            return switch (aiProvider) {
                case OPEN_AI -> "gpt-5-nano";
                case DEEP_SEEK -> "deepseek-chat";
            };
        }

        return switch (aiProvider) {
            case OPEN_AI -> "gpt-5.1";
            case DEEP_SEEK -> "deepseek-reasoner";
        };
    }

    private AIChatRequest getAIChatRequest(String message, String prompt, boolean limited) {

        var userMessage = AIMessageRequest.builder()
            .role(Role.USER)
            .content(message)
            .build();

        var systemMessage = AIMessageRequest.builder()
            .role(Role.SYSTEM)
            .content(prompt)
            .build();

        var provider = getAiModel(aiProviderState.getCurrentProvider(), limited);
        return AIChatRequest.builder()
            .messages(List.of(systemMessage, userMessage))
            .model(provider)
            .stream(false)
            .build();
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