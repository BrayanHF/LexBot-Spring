package com.lexbot.data.controllers;

import com.lexbot.chat.dto.chat.ApiResponse;
import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.services.LBUserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("user")
@AllArgsConstructor
public class LBUserController {

    private LBUserService lbUserService;

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
