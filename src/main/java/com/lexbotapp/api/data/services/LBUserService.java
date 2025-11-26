package com.lexbotapp.api.data.services;

import com.lexbotapp.api.data.firestore_dao.LBUser;
import com.lexbotapp.api.data.firestore_dao.LBUserStatus;
import com.lexbotapp.api.data.repositories.LBUserRepository;
import com.lexbotapp.api.utils.validations.SimpleValidation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class LBUserService {

    private final LBUserRepository userRepository;

    public Mono<LBUser> getUserById(String userId) {
        SimpleValidation.validateStrings(userId);

        return userRepository.getUserById(userId);
    }

    public Mono<LBUser> addUser(LBUser user) {
        SimpleValidation.validateNotNulls(user);

        user.setStatus(LBUserStatus.LIMITED);

        return userRepository.addUser(user);
    }

    public Mono<Void> deleteUserById(String userId) {
        SimpleValidation.validateStrings(userId);

        return userRepository.deleteUserById(userId);
    }

}
