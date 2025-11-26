package com.lexbotapp.api.data.repositories;

import com.lexbotapp.api.data.firestore_dao.LBUser;
import reactor.core.publisher.Mono;

public interface LBUserRepository {

    Mono<LBUser> getUserById(String userId);

    Mono<LBUser> addUser(LBUser user);

    Mono<Void> deleteUserById(String userId);

}
