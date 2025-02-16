package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIServiceFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Todo: layer of laws
@Service
public class ChatOrchestratorService {

    private final AIServiceManager aiServiceManager;
    private final ChatMessageService chatMessageService;

    public ChatOrchestratorService(
        AIServiceManager aiServiceManager,
        ChatMessageService chatMessageService,
        AIServiceFactory aiServiceFactory
    ) {
        this.aiServiceManager = aiServiceManager;
        this.chatMessageService = chatMessageService;
        setAIService(aiServiceFactory);
    }

    private void setAIService(AIServiceFactory aiServiceFactory) {
        var aiService = aiServiceFactory.getAIService(AIProvider.OPEN_AI);
        aiServiceManager.setAiService(aiService);
    }

    public Mono<AIChatResponse> chat(String userId, String chatId, String userMessage) {
        return Mono.zip(
                chatMessageService.getOrCreateChat(userId, chatId, userMessage),
                aiServiceManager.generateAIMessage(userMessage)
            )
            .flatMap(
                tuple -> {
                    String chat_id = tuple.getT1().getId();
                    AIChatResponse aiChatResponse = tuple.getT2();
                    String assistantMessage = aiChatResponse.getChoices().getFirst().getResponse().getContent();

                    chatMessageService.saveMessages(userId, chat_id, userMessage, assistantMessage);

                    return Mono.just(aiChatResponse);
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Chat process error", e)));
    }

    public Flux<AIChatResponse> chatStream(String userId, String chatId, String userMessage) {
        var getOrCreateChatMono = chatMessageService.getOrCreateChat(userId, chatId, userMessage).cache();
        var generateStreamAIMessageFlux = aiServiceManager.generateStreamAIMessage(userMessage);

        var stringBuilder = new StringBuilder();
        return getOrCreateChatMono
            .flatMapMany(
                chat -> generateStreamAIMessageFlux
                    .doOnNext(
                        iaChatResponse -> {
                            var choice = iaChatResponse.getChoices().getFirst();
                            if (choice.getFinish_reason() == null) {
                                stringBuilder.append(choice.getResponse().getContent());
                            }
                        }
                    )
                    .doOnComplete(
                        () -> {
                            String assistantMessage = stringBuilder.toString();
                            chatMessageService.saveMessages(userId, chat.getId(), userMessage, assistantMessage);
                        }
                    )
            );
    }

}
