package com.lexbotapp.api.chat.services.chatting;

import com.lexbotapp.api.ai.dto.AIProvider;
import com.lexbotapp.api.ai.dto.response.AIChatResponse;
import com.lexbotapp.api.ai.services.AIServiceFactory;
import com.lexbotapp.api.chat.dto.chat.ChattingResponse;
import com.lexbotapp.api.search.dto.tavily.TVLSearchRequest;
import com.lexbotapp.api.search.dto.tavily.TVLSearchResponse;
import com.lexbotapp.api.search.services.WebSearchService;
import com.lexbotapp.api.utils.prompts.chat.ChatPrompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatOrchestratorService {

    private final AIServiceManager aiServiceManager;
    private final ChatMessageService chatMessageService;
    private final AIServiceFactory aiServiceFactory;
    private final WebSearchService webSearchService;
    private final AIProviderState aiProviderState;

    public ChatOrchestratorService(
        AIServiceManager aiServiceManager,
        ChatMessageService chatMessageService,
        AIServiceFactory aiServiceFactory,
        WebSearchService webSearchService, AIProviderState aiProviderState
    ) {
        this.aiServiceManager = aiServiceManager;
        this.chatMessageService = chatMessageService;
        this.aiServiceFactory = aiServiceFactory;
        this.webSearchService = webSearchService;
        this.aiProviderState = aiProviderState;
        setAIService(aiProviderState.getCurrentProvider());
    }

    public void setAIService(AIProvider provider) {
        aiProviderState.setCurrentProvider(provider);
        var aiService = aiServiceFactory.getAIService(provider);
        aiServiceManager.setAiService(aiService);
    }

    public Mono<ChattingResponse> chat(String userId, String chatId, String userMessage) {
        return Mono.zip(
                chatMessageService.getOrCreateChat(userId, chatId, userMessage),
                aiServiceManager.generateAIMessageLimited(userMessage, ChatPrompt.LEXBOT_CHAT_PROMPT)
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
                            .chatId(newChatId)
                            .aiChatResponse(aiChatResponse).
                            build()
                    );
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Chat process error", e)));
    }

    public Flux<ChattingResponse> chatStream(String userId, String chatId, String userMessage) {
        var getOrCreateChatMono = chatMessageService.getOrCreateChat(userId, chatId, userMessage).cache();
        var tvlSearchRequestMono = userMessageWebSearch(userMessage);

        var stringBuilder = new StringBuilder();
        return Mono.zip(
            getOrCreateChatMono,
            tvlSearchRequestMono
        ).flatMapMany(
            tuple -> {

                String currentChatId = chatId == null || chatId.isEmpty() ? tuple.getT1().getId() : chatId;

                return chatMessageService.getChatResume(userId, currentChatId)
                    .flatMapMany(resume -> {

                        String messageToChat = """
                            Mensaje escrito por el usuario:
                            %s
                            
                            Resumen del chat actual:
                            %s
                            
                            Busqueda web del mensaje del usuario:
                            %s
                            """.formatted(
                            userMessage,
                            resume,
                            tuple.getT2()
                        );

                        return aiServiceManager.generateStreamAIMessage(messageToChat, ChatPrompt.LEXBOT_CHAT_PROMPT)
                            .map(
                                iaChatResponse -> {
                                    var choice = iaChatResponse.getChoices().getFirst();
                                    var content = choice.getResponse().getContent() == null ? "" : choice.getResponse().getContent();
                                    if (choice.getFinish_reason() == null) {
                                        stringBuilder.append(content);
                                    }

                                    return ChattingResponse.builder()
                                        .chatId(currentChatId)
                                        .aiChatResponse(iaChatResponse)
                                        .build();
                                }
                            )
                            .doOnComplete(
                                () -> {
                                    String assistantMessage = stringBuilder.toString();
                                    chatMessageService.saveMessages(userId, currentChatId, userMessage, assistantMessage);
                                }
                            );
                    });
            }
        );
    }

    public Mono<String> newChat(String userId, String chatId, String userMessage) {
        return chatMessageService
            .getOrCreateChat(userId, chatId, userMessage)
            .flatMap(chat -> Mono.just(chat.getId()));
    }

    private Mono<TVLSearchResponse> userMessageWebSearch(String userMessage) {
        TVLSearchRequest request = TVLSearchRequest.builder()
            .query(userMessage)
            .country("colombia")
            .search_depth("advanced")
            .include_raw_content(true)
            .max_results(5)
            .build();

        return webSearchService.search(request);
    }

}
