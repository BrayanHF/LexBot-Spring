package com.lexbot.chat.services.generate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lexbot.ai.dto.AIProvider;
import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.ai.services.AIServiceFactory;
import com.lexbot.chat.dto.QuestionAnswer;
import com.lexbot.chat.dto.RightPetitionRequest;
import com.lexbot.chat.dto.ValidatedAnswer;
import com.lexbot.chat.services.chatting.AIServiceManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GenerateDocumentsService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final AIServiceManager aiServiceManager;

    public GenerateDocumentsService(
        AIServiceManager aiServiceManager,
        AIServiceFactory aiServiceFactory
    ) {
        this.aiServiceManager = aiServiceManager;
        setAIService(aiServiceFactory);
    }

    private void setAIService(AIServiceFactory aiServiceFactory) {
        var aiService = aiServiceFactory.getAIService(AIProvider.OPEN_AI);
        aiServiceManager.setAiService(aiService);
    }

    public Mono<ValidatedAnswer> validateAnswer(QuestionAnswer qa) {
        String VALIDATE_ANSWER_PROMPT = """
            Eres un asistente que valida respuestas para un formulario jurídico colombiano.
            Responde únicamente en formato JSON con las siguientes propiedades:
            - error (string | null): si la respuesta no es válida, aquí va el mensaje que se le debe mostrar al usuario.
            - result (string | null): si la respuesta es válida, este es el texto útil que se usará para el documento.
            No expliques nada, no incluyas texto fuera del JSON.
            """;


        String userMessage = "Pregunta: " + qa.getQuestion() + "\nRespuesta: " + qa.getAnswer() + "\nValida si la respuesta dada tiene sentido y responde correctamente a la pregunta hecha";

        return aiServiceManager.generateAIMessageLimited(userMessage, VALIDATE_ANSWER_PROMPT).map(aiChatResponse -> {
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

    public Mono<AIChatResponse> generateRightPetition(RightPetitionRequest rpRequest) throws JsonProcessingException {
        String RIGHT_PETITION_PROMPT = """
            Eres un asistente legal experto en la legislación colombiana.
            Tu tarea es redactar **solo la parte variable** de un derecho de petición, siguiendo esta estructura general,
            pero tomando libertad en el tono, redacción y estilo según el caso.
            
            1. Encabezado:
               Inicia con una fórmula de saludo adecuada al destinatario:
               Usa “Señores”, “Señoras”, “Señor”, “Señora” u otra alternativa formal que consideres pertinente según el nombre o tipo de entidad:
               $recipient
            2. Asunto:
               Asunto: Derecho de petición
            3. Identificación del solicitante:
               Identifica al solicitante de manera formal y clara, utilizando estos datos:
               - Nombre completo: $fullName
               - Tipo y número de documento: $documentId.type No. $documentId.number
               - Lugar de expedición del documento: $documentId.expedition
               - Ciudad de residencia: $city
               - Dirección: $address
            4. Fundamento:
               Expón que se actúa en ejercicio del derecho de petición consagrado en el artículo 23 de la Constitución Política de Colombia.
               Puedes hacer referencia también a la Ley 1755 de 2015, y, si lo crees útil, a otras normas aplicables.
            5. Exposición de hechos:
               Con base en este resumen, redacta de forma **cronológica y ampliada** los hechos relevantes:
               $facts
            6. Petición:
               A partir de este resumen, formula una o más solicitudes **claras, formales y bien justificadas**:
               $request
            7. Despedida:
               Finaliza con una fórmula cordial y adecuada al tono del documento. Puedes variar la redacción según el estilo del texto;
               ejemplos posibles son: “Agradezco su atención y quedo atento(a) a su pronta respuesta.”,
               “Esperando una pronta y favorable respuesta, quedo atento(a).”,
               entre otras opciones que consideres apropiadas.
            
            **No** incluyas fecha, lugar, datos de contacto adicionales ni firma.
            **No** utilices negritas, listas Markdown ni etiquetas HTML.
            Responde **únicamente** con el texto completo, redactado en un lenguaje formal, claro y accesible.
            """;


        String json = mapper.writeValueAsString(rpRequest);

        return aiServiceManager.generateAIMessageUnlimited(json, RIGHT_PETITION_PROMPT);
    }

}
