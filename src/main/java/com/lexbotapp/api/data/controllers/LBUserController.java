package com.lexbotapp.api.data.controllers;

import com.lexbotapp.api.chat.dto.chat.ApiResponse;
import com.lexbotapp.api.data.firestore_dao.LBUser;
import com.lexbotapp.api.data.services.LBUserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("user")
@AllArgsConstructor
public class LBUserController {

    private LBUserService lbUserService;

    @PostMapping("exists")
    public Mono<ApiResponse<Boolean>> getUserById(@RequestBody String uid) {
        return lbUserService.getUserById(uid)
            .map(lbUser -> ApiResponse.success(true, false))
            .onErrorResume(e -> Mono.just(ApiResponse.error("User not found", false)));
    }

    @PostMapping("new")
    public Mono<ApiResponse<Boolean>> newLBUser(
        @RequestBody LBUser lbUser,
        Authentication authentication
    ) {
        String uid = authentication.getName();

        lbUser.setUid(uid);
        return lbUserService.addUser(lbUser)
            .map(savedUser ->
                ApiResponse.success(true, false)
            )
            .onErrorResume(e -> Mono.just(
                ApiResponse.error("No fue posible crear el usuario en este momento. Por favor intenta nuevamente.", false)
            ));
    }

    @DeleteMapping("delete")
    public Mono<Void> deleteLBUser(Authentication authentication) {
        return lbUserService.deleteUserById(authentication.getName());
    }

}
