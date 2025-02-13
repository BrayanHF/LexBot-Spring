package com.lexbot.data.repositories;

import com.lexbot.data.firestore_dao.LBUser;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface LBUserRepository {

    Mono<LBUser> addUser(LBUser user);

    Mono<Void> updateUser(String userId, Map<String, Object> updates);

    Mono<LBUser> getUserByEmail(String email);

    Mono<Void> deleteUserById(String userId);

}
