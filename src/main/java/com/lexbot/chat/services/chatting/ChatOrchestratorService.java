package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIServiceFactory;
import com.lexbot.chat.dto.ChattingResponse;
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

    public Mono<ChattingResponse> chat(String userId, String chatId, String userMessage) {
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

                    String newChatId = chatId == null || chatId.isEmpty() ? chat_id : null;

                    return Mono.just(
                        ChattingResponse.builder()
                            .newChatId(newChatId)
                            .aiChatResponse(aiChatResponse).
                            build()
                    );
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Chat process error", e)));
    }

    public Flux<ChattingResponse> chatStream(String userId, String chatId, String userMessage) {
        var getOrCreateChatMono = chatMessageService.getOrCreateChat(userId, chatId, userMessage).cache();
        var generateStreamAIMessageFlux = aiServiceManager.generateStreamAIMessage(userMessage);

        var stringBuilder = new StringBuilder();
        return getOrCreateChatMono
            .flatMapMany(
                chat -> {
                    String newChatId = chatId == null || chatId.isEmpty() ? chat.getId() : null;

                    return generateStreamAIMessageFlux
                        .map(
                            iaChatResponse -> {
                                var choice = iaChatResponse.getChoices().getFirst();
                                if (choice.getFinish_reason() == null) {
                                    stringBuilder.append(choice.getResponse().getContent());
                                }

                                return ChattingResponse.builder()
                                    .newChatId(newChatId)
                                    .aiChatResponse(iaChatResponse)
                                    .build();
                            }
                        )
                        .doOnComplete(
                            () -> {
                                String assistantMessage = stringBuilder.toString();
                                chatMessageService.saveMessages(userId, chat.getId(), userMessage, assistantMessage);
                            }
                        );
                }
            );
    }

}
