package com.lexbot.data.controllers;

import com.lexbot.ai.dto.AIProvider;
import com.lexbot.chat.services.chatting.ChatOrchestratorService;
import com.lexbot.data.firestore_dao.ChangeAIProviderRequest;
import com.lexbot.data.firestore_dao.LBUserStatus;
import com.lexbot.data.services.LBUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/ai-provider")
@RequiredArgsConstructor
public class AIProviderController {

    private final ChatOrchestratorService chatOrchestratorService;
    private final LBUserService userService;

    @PostMapping("change")
    public Mono<String> changeAIProvider(
        @RequestBody ChangeAIProviderRequest request,
        Authentication authentication
    ) {
        String userId = authentication.getName();

        return userService.getUserById(userId)
            .flatMap(user -> {
                if (user.getStatus() != LBUserStatus.ADMIN) {
                    return Mono.error(new IllegalAccessException(
                        "No tienes permisos para cambiar el proveedor de IA."
                    ));
                }

                chatOrchestratorService.changeAIProvider(request.provider());
                return Mono.just(
                    "Proveedor de IA cambiado exitosamente a: " + request.provider()
                );
            });
    }

    @GetMapping("current")
    public Mono<String> getCurrentProvider() {
        AIProvider current = chatOrchestratorService.getCurrentProvider();
        return Mono.just("Proveedor actual: " + current.name());
    }

}

