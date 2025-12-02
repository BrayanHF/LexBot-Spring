package com.lexbotapp.api.data.controllers;

import com.lexbotapp.api.ai.dto.AIProvider;
import com.lexbotapp.api.chat.services.chatting.AIProviderState;
import com.lexbotapp.api.data.firestore_dao.ChangeAIProviderRequest;
import com.lexbotapp.api.data.firestore_dao.LBUserStatus;
import com.lexbotapp.api.data.services.LBUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/ai-provider")
@RequiredArgsConstructor
public class AIProviderController {

    private final AIProviderState aiProviderState;
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

                aiProviderState.setCurrentProvider(request.provider());
                return Mono.just(
                    "Proveedor de IA cambiado exitosamente a: " + request.provider()
                );
            })
            .onErrorResume(e -> Mono.just(
                "El proveedor de IA seleccionado no está disponible. Por favor elige uno de los proveedores permitidos."
            ));
    }

    @GetMapping("current")
    public Mono<String> getCurrentProvider() {
        AIProvider current = aiProviderState.getCurrentProvider();
        return Mono.just("Proveedor actual: " + current.name());
    }

}
