package com.lexbotapp.api.chat.controllers;

import com.lexbotapp.api.chat.dto.generate.*;
import com.lexbotapp.api.chat.services.generate.text.DocumentType;
import com.lexbotapp.api.chat.services.generate.text.GenerateDocumentsService;
import com.lexbotapp.api.utils.prompts.document.DocumentPromptType;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("generate")
@AllArgsConstructor
public class DocumentGeneratorController {

    private GenerateDocumentsService generateDocumentsService;

    @PostMapping("validate-answer/right-petition")
    public Mono<ValidatedAnswer> validateAnswerRightPetition(@RequestBody QuestionAnswer questionAnswer) {
        return generateDocumentsService.validateAnswer(questionAnswer, DocumentPromptType.VALIDATE_ANSWER_RIGHT_PETITION);
    }

    @PostMapping("validate-answer/complaint")
    public Mono<ValidatedAnswer> validateAnswerComplaint(@RequestBody QuestionAnswer questionAnswer) {
        return generateDocumentsService.validateAnswer(questionAnswer, DocumentPromptType.VALIDATE_ANSWER_COMPLAINT);
    }

    @PostMapping("validate-answer/power-of-attorney")
    public Mono<ValidatedAnswer> validateAnswerSpecialPower(@RequestBody QuestionAnswer questionAnswer) {
        return generateDocumentsService.validateAnswer(questionAnswer, DocumentPromptType.VALIDATE_ANSWER_SPECIAL_POWER);
    }

    @PostMapping("validate-answer/habeas-data")
    public Mono<ValidatedAnswer> validateAnswerHabeasData(@RequestBody QuestionAnswer questionAnswer) {
        return generateDocumentsService.validateAnswer(questionAnswer, DocumentPromptType.VALIDATE_ANSWER_HABEAS_DATA);
    }

    @PostMapping("right-petition")
    public Mono<ResponseEntity<byte[]>> generateRightPetition(@RequestBody RightPetitionRequest rpRequest) {
        return generateDocumentsService.generateDocument(rpRequest, DocumentType.RIGHT_PETITION)
            .map(pdfBytes -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=doc.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes)
            ).onErrorResume(e -> errorMessageCreatingDoc());
    }

    @PostMapping(value = "complaint", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> generateComplaint(@RequestBody ComplaintRequest complaintRequest) {
        return generateDocumentsService.generateDocument(complaintRequest, DocumentType.COMPLAINT)
                .map(pdfBytes -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=doc.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes)
                ).onErrorResume(e -> errorMessageCreatingDoc());
    }

    @PostMapping(value = "power-of-attorney", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> generateSpecialPower(@RequestBody SpecialPowerRequest spRequest) {
        return generateDocumentsService.generateDocument(spRequest, DocumentType.SPECIAL_POWER)
                .map(pdfBytes -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=doc.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes)
                ).onErrorResume(e -> errorMessageCreatingDoc());
    }

    @PostMapping(value = "habeas-data", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> generateHabeasData(@RequestBody HabeasDataRequest hdRequest) {
        return generateDocumentsService.generateDocument(hdRequest, DocumentType.HABEAS_DATA)
                .map(pdfBytes -> ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=doc.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes)
                ).onErrorResume(e -> errorMessageCreatingDoc());
    }

    private Mono<ResponseEntity<byte[]>> errorMessageCreatingDoc() {
        String errorMessage = "Ocurrió un error al generar el documento. Inténtalo nuevamente.";
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(errorMessage.getBytes(StandardCharsets.UTF_8))
        );
    }

}
