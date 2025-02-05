package com.lexbot.data.repositories.impl.firebase;

import com.google.cloud.firestore.*;
import com.google.common.util.concurrent.MoreExecutors;
import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.repositories.LBUserRepository;
import com.lexbot.data.repositories.impl.firebase.config.FutureUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public Mono<LBUser> addUser(LBUser user) {
        DocumentReference newUserRef = usersCollection().document();
        user.setId(newUserRef.getId());

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
    public Mono<LBUser> getUserByEmail(String email) {
        Query query = usersCollection().whereEqualTo("email", email);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(query.get()))
            .mapNotNull(querySnapshot -> {
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

                if (documents.isEmpty()) return null;

                DocumentSnapshot document = documents.getFirst();
                LBUser user = document.toObject(LBUser.class);
                user.setId(document.getId());
                return user;
            });
    }

    @Override
    public Mono<Void> deleteUserById(String userId) {
        return Mono
            .fromFuture(
                FutureUtils.toCompletableFuture(
                    usersCollection().document(userId).delete()
                )
            )
            .then();
    }

}
