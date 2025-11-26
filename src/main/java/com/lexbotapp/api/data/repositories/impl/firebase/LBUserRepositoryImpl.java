package com.lexbotapp.api.data.repositories.impl.firebase;

import com.google.cloud.firestore.*;
import com.google.common.util.concurrent.MoreExecutors;
import com.lexbotapp.api.data.firestore_dao.LBUser;
import com.lexbotapp.api.data.repositories.LBUserRepository;
import com.lexbotapp.api.data.repositories.impl.firebase.config.FutureUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class LBUserRepositoryImpl implements LBUserRepository {

    private final Firestore firestore;

    public LBUserRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference usersCollection() {
        return firestore.collection(LBUser.PATH);
    }

    @Override
    public Mono<LBUser> getUserById(String userId) {
        DocumentReference userRef = usersCollection().document(userId);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(userRef.get())).flatMap(
            document -> {
                LBUser lbUser = document.toObject(LBUser.class);
                if (lbUser == null || !document.exists()) return Mono.error(new RuntimeException("User not found"));
                lbUser.setUid(document.getId());
                return Mono.just(lbUser);
            }
        );
    }

    @Override
    public Mono<LBUser> addUser(LBUser user) {
        DocumentReference newUserRef = usersCollection().document(user.getUid());

        return Mono.create(sink ->
            newUserRef
                .set(user)
                .addListener(
                    () -> {
                        try {
                            sink.success(user);
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    }, MoreExecutors.directExecutor()
                )
        );
    }

    @Override
    public Mono<Void> deleteUserById(String uid) {
        return Mono
            .fromFuture(
                FutureUtils.toCompletableFuture(
                    usersCollection().document(uid).delete()
                )
            )
            .then();
    }

}
