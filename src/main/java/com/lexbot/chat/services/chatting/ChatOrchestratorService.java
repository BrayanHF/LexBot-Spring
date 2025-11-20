package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIServiceFactory;
import com.lexbot.chat.dto.chat.ChattingResponse;
import com.lexbot.search.dto.tavily.TVLSearchRequest;
import com.lexbot.search.dto.tavily.TVLSearchResponse;
import com.lexbot.search.services.WebSearchService;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatOrchestratorService {

    public String LEGAL_COL_PROMPT = """
        Eres un asistente legal especializado únicamente en el contexto jurídico colombiano.
        Debes responder con base en las leyes, normas, decretos y jurisprudencia de Colombia, actual o históricamente vigente.
        No debes referenciar ni aplicar normas de otros países bajo ninguna circunstancia.
        Responde siempre de manera clara, sencilla y con lenguaje comprensible para personas sin formación jurídica, sin perder precisión legal.
        Cuando una norma tenga excepciones o condiciones especiales, explícalas también de forma accesible.
        No inventes información. Si no tienes certeza sobre un tema o norma específica, responde indicando que no puedes dar una respuesta definitiva y sugiere acudir a una entidad competente en Colombia (como una Personería, Defensoría del Pueblo, consultorio jurídico, etc.).
        Tu enfoque debe ser siempre ayudar con temas legales exclusivamente de Colombia, manteniendo la claridad, precisión y el lenguaje para todo público.
        Se te va enviar un resumen del chat que lleva el usuario hasta el momento, si esta vacio es porque es el inico del chat.
        IMPORTANTE! Se va enviar toda la respuesta de una busqueda web hecha en base al mensaje del usuario, esta busqueda vendra en formato json y es de la api de tavily (esto no lo tienes que mencionar).
        """;


    private final AIServiceManager aiServiceManager;
    private final ChatMessageService chatMessageService;
    private final AIServiceFactory aiServiceFactory;
    private final WebSearchService webSearchService;

    @Getter
    private AIProvider currentProvider = AIProvider.OPEN_AI;

    public ChatOrchestratorService(
        AIServiceManager aiServiceManager,
        ChatMessageService chatMessageService,
        AIServiceFactory aiServiceFactory,
        WebSearchService webSearchService
    ) {
        this.aiServiceManager = aiServiceManager;
        this.chatMessageService = chatMessageService;
        this.aiServiceFactory = aiServiceFactory;
        this.webSearchService = webSearchService;
        setAIService(currentProvider);
    }

    private void setAIService(AIProvider provider) {
        var aiService = aiServiceFactory.getAIService(provider);
        aiServiceManager.setAiService(aiService);
        this.currentProvider = provider;
    }

    public void changeAIProvider(AIProvider newProvider) {
        setAIService(newProvider);
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

                        LEGAL_COL_PROMPT = LEGAL_COL_PROMPT + "\n\nResumen del chat:\n" + resume + "\n\nBusqueda web del mensaje del usuario:\n" + tuple.getT2();

                        return generateStreamAIMessageFlux
                            .map(
                                iaChatResponse -> {
                                    var choice = iaChatResponse.getChoices().getFirst();
                                    if (choice.getFinish_reason() == null) {
                                        stringBuilder.append(choice.getResponse().getContent());
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
            .build();

        return webSearchService.search(request);
    }

}
