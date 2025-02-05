package com.lexbot.data.repositories;

import com.lexbot.data.firestore_dao.LBUser;
import reactor.core.publisher.Mono;

public interface LBUserRepository {

    Mono<LBUser> addUser(LBUser user);

    Mono<LBUser> getUserByEmail(String email);

    Mono<Void> deleteUserById(String userId);

}
