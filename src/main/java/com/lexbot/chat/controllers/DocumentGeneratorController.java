package com.lexbot.chat.controllers;

import com.lexbot.ai.dto.response.AIChatResponse;
import com.lexbot.chat.dto.QuestionAnswer;
import com.lexbot.chat.dto.RightPetitionRequest;
import com.lexbot.chat.dto.ValidatedAnswer;
import com.lexbot.chat.services.generate.GenerateDocumentsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("generate")
@AllArgsConstructor
public class DocumentGeneratorController {

    private GenerateDocumentsService generateDocumentsService;

    @PostMapping("validate-answer")
    public Mono<ValidatedAnswer> validateAnswer(@RequestBody QuestionAnswer questionAnswer) {
        return generateDocumentsService.validateAnswer(questionAnswer);
    }

    @PostMapping("right-petition")
    public Mono<AIChatResponse> generateRightPetition(@RequestBody RightPetitionRequest rpRequest) {
        try {
            return generateDocumentsService.generateRightPetition(rpRequest);
        } catch (Exception e) {
            // todo: better
            return Mono.error(e);
        }
    }


}
