package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.firestore_dao.LBUserStatus;
import com.lexbot.data.repositories.LBUserRepository;
import com.lexbot.utils.validations.SimpleValidation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class LBUserService {

    private final LBUserRepository userRepository;

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
