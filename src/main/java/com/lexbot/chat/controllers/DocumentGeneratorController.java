package com.lexbot.chat.controllers;

import com.lexbot.chat.dto.QuestionAnswer;
import com.lexbot.chat.dto.ValidatedAnswer;
import com.lexbot.chat.services.chatting.ChatOrchestratorService;
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

    private ChatOrchestratorService chatOrchestratorService;

    @PostMapping("validate-answer")
    public Mono<ValidatedAnswer> validateAnswer(@RequestBody QuestionAnswer questionAnswer) {
        return chatOrchestratorService.validateAnswer(questionAnswer);
    }

}
