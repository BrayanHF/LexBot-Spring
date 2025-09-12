package com.lexbot.chat.services.generate.text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.services.AIServiceFactory;
import com.lexbot.chat.dto.generate.BaseLegalRequest;
import com.lexbot.chat.dto.generate.QuestionAnswer;
import com.lexbot.chat.dto.generate.ValidatedAnswer;
import com.lexbot.chat.services.chatting.AIServiceManager;
import com.lexbot.chat.services.generate.pdf.MarkdownPdfService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GenerateDocumentsService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final AIServiceManager aiServiceManager;
    private final MarkdownPdfService markdownPdfService;

    public GenerateDocumentsService(
        AIServiceManager aiServiceManager,
        MarkdownPdfService markdownPdfService,
        AIServiceFactory aiServiceFactory
    ) {
        this.aiServiceManager = aiServiceManager;
        this.markdownPdfService = markdownPdfService;
        setAIService(aiServiceFactory);
    }

    private void setAIService(AIServiceFactory aiServiceFactory) {
        var aiService = aiServiceFactory.getAIService(AIProvider.OPEN_AI);
        aiServiceManager.setAiService(aiService);
    }

    public Mono<ValidatedAnswer> validateAnswer(QuestionAnswer qa, PromptType promptType) {

        String userMessage = "Pregunta: " + qa.getQuestion() +
            "\nRespuesta: " + qa.getAnswer() +
            "\nValida si la respuesta dada tiene sentido y responde correctamente a la pregunta hecha, no ser estrictos con la ortografia";


        return aiServiceManager.generateAIMessageLimited(userMessage, PromptProvider.getPrompt(promptType))
            .map(aiChatResponse -> {
                String json = aiChatResponse.getChoices().getFirst().getResponse().getContent();

                try {
                    return ValidatedAnswer.parse(json);
                } catch (Exception e) {
                    var validatedAnswer = new ValidatedAnswer();
                    validatedAnswer.setError(null);
                    validatedAnswer.setResult(null);
                    return validatedAnswer;
                }
            });
    }

    public Mono<byte[]> generateDocument(BaseLegalRequest request, DocumentType documentType) throws JsonProcessingException {
        String json = mapper.writeValueAsString(request);
        return aiServiceManager.generateAIMessageUnlimited(json, PromptProvider.getPrompt(documentType))
            .map(res -> {
                    String textMarkDown = res.getChoices().getFirst().getResponse().getContent();
                    return markdownPdfService.generatePdfFromMarkdown(textMarkDown);
                }
            );
    }

}
