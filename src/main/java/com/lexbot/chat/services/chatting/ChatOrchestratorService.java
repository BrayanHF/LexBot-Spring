package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIServiceFactory;
import com.lexbot.chat.dto.ChattingResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatOrchestratorService {

    public static final String LEGAL_COL_PROMPT = """
        Eres un asistente legal especializado únicamente en el contexto jurídico colombiano.
        Debes responder con base en las leyes, normas, decretos y jurisprudencia de Colombia, actual o históricamente vigente.
        No debes referenciar ni aplicar normas de otros países bajo ninguna circunstancia.
        Responde siempre de manera clara, sencilla y con lenguaje comprensible para personas sin formación jurídica, sin perder precisión legal.
        Cuando una norma tenga excepciones o condiciones especiales, explícalas también de forma accesible.
        No inventes información. Si no tienes certeza sobre un tema o norma específica, responde indicando que no puedes dar una respuesta definitiva y sugiere acudir a una entidad competente en Colombia (como una Personería, Defensoría del Pueblo, consultorio jurídico, etc.).
        Tu enfoque debe ser siempre ayudar con temas legales exclusivamente de Colombia, manteniendo la claridad, precisión y el lenguaje para todo público.
        """;


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
                aiServiceManager.generateAIMessageLimited(userMessage, LEGAL_COL_PROMPT)
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
        var generateStreamAIMessageFlux = aiServiceManager.generateStreamAIMessage(userMessage, LEGAL_COL_PROMPT);

        var stringBuilder = new StringBuilder();
        return getOrCreateChatMono
            .flatMapMany(
                chat -> {
                    String newChatId = chatId == null || chatId.isEmpty() ? chat.getId() : chatId;

                    return generateStreamAIMessageFlux
                        .map(
                            iaChatResponse -> {
                                var choice = iaChatResponse.getChoices().getFirst();
                                if (choice.getFinish_reason() == null) {
                                    stringBuilder.append(choice.getResponse().getContent());
                                }

                                return ChattingResponse.builder()
                                    .chatId(newChatId)
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
