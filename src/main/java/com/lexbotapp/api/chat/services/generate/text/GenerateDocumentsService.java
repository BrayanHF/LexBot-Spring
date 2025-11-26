package com.lexbotapp.api.chat.services.generate.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexbotapp.api.ai.dto.AIProvider;
import com.lexbotapp.api.ai.services.AIServiceFactory;
import com.lexbotapp.api.chat.dto.generate.BaseLegalRequest;
import com.lexbotapp.api.chat.dto.generate.QuestionAnswer;
import com.lexbotapp.api.chat.dto.generate.ValidatedAnswer;
import com.lexbotapp.api.chat.services.chatting.AIServiceManager;
import com.lexbotapp.api.chat.services.generate.pdf.MarkdownPdfService;
import com.lexbotapp.api.search.dto.tavily.TVLSearchRequest;
import com.lexbotapp.api.search.dto.tavily.TVLSearchResponse;
import com.lexbotapp.api.search.services.WebSearchService;
import com.lexbotapp.api.utils.prompts.chat.ChatPrompt;
import com.lexbotapp.api.utils.prompts.document.DocumentPromptProvider;
import com.lexbotapp.api.utils.prompts.document.DocumentPromptType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GenerateDocumentsService {

    private final AIServiceManager aiServiceManager;
    private final MarkdownPdfService markdownPdfService;
    private final WebSearchService webSearchService;

    private final ObjectMapper mapper = new ObjectMapper();

    public GenerateDocumentsService(
        AIServiceManager aiServiceManager,
        MarkdownPdfService markdownPdfService,
        AIServiceFactory aiServiceFactory, WebSearchService webSearchService
    ) {
        this.aiServiceManager = aiServiceManager;
        this.markdownPdfService = markdownPdfService;
        this.webSearchService = webSearchService;
        setAIService(aiServiceFactory);
    }

    private void setAIService(AIServiceFactory aiServiceFactory) {
        var aiService = aiServiceFactory.getAIService(AIProvider.DEEP_SEEK);
        aiServiceManager.setAiService(aiService);
    }

    public Mono<ValidatedAnswer> validateAnswer(QuestionAnswer qa, DocumentPromptType documentPromptType) {

        String userMessage = """
            Pregunta:
            %s
            
            Respuesta:
            %s
            
            Valida si la respuesta dada tiene sentido y responde correctamente a la pregunta hecha, no ser estrictos con la ortografia"
            """.formatted(
            qa.getQuestion(),
            qa.getAnswer()
        );

        return aiServiceManager.generateAIMessageLimited(userMessage, DocumentPromptProvider.getPrompt(documentPromptType))
            .map(aiChatResponse -> {
                String response = aiChatResponse.getChoices().getFirst().getResponse().getContent();

                try {
                    return ValidatedAnswer.parse(response);
                } catch (Exception e) {
                    var validatedAnswer = new ValidatedAnswer();
                    validatedAnswer.setError(null);
                    validatedAnswer.setResult(null);
                    return validatedAnswer;
                }
            });
    }

    public Mono<byte[]> generateDocument(BaseLegalRequest request, DocumentType documentType) {
        String userRequest;
        try {
            userRequest = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return search(userRequest)
            .flatMap(tvlSearchResponses -> {

                    String searches;
                    try {
                        searches = mapper.writeValueAsString(tvlSearchResponses);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }

                    String toSent = """
                        Peticion del usuario:
                        %s
                        
                        Busquedas webs:
                        %s
                        """.formatted(
                        userRequest,
                        searches
                    );

                    return aiServiceManager.generateAIMessageUnlimited(toSent, DocumentPromptProvider.getPrompt(documentType))
                        .map(res -> {
                                String textMarkDown = res.getChoices().getFirst().getResponse().getContent();
                                return markdownPdfService.generatePdfFromMarkdown(textMarkDown);
                            }
                        );
                }
            ).onErrorResume(e -> Mono.just(new byte[0]));
    }

    private Mono<List<TVLSearchResponse>> search(String json) {
        return aiServiceManager.generateAIMessageLimited(json, ChatPrompt.TO_SEARCH_CHAT)
            .map((aiChatResponse) -> {
                    var content = aiChatResponse.getChoices().getFirst().getResponse().getContent();

                    try {
                        return mapper.readValue(content, new TypeReference<>() {
                        });
                    } catch (JsonProcessingException e) {
                        return List.of("");
                    }
                }
            )
            .flatMap(queries ->
                Flux.fromIterable(queries)
                    .flatMap(query ->
                        webSearchService.search(tvlSearchRequest(query))
                    )
                    .collectList()
            );
    }

    private TVLSearchRequest tvlSearchRequest(String query) {
        return TVLSearchRequest.builder()
            .query(query)
            .country("colombia")
            .search_depth("advanced")
            .include_raw_content(true)
            .max_results(1)
            .build();
    }

}
