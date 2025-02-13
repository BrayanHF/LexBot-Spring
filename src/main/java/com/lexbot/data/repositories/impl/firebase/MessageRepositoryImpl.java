package com.lexbot.data.repositories.impl.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.util.concurrent.MoreExecutors;
import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.repositories.MessageRepository;
import com.lexbot.data.repositories.impl.firebase.config.FutureUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final Firestore firestore;

    public MessageRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference messagesCollection(String userId, String chatId) {
        return firestore
            .collection(LBUser.PATH).document(userId)
            .collection(Chat.PATH).document(chatId)
            .collection(Message.PATH);
    }

    @Override
    public Mono<List<Message>> chatMessages(String userId, String chatId) {
        CollectionReference messagesCollection = messagesCollection(userId, chatId);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(messagesCollection.get()))
            .map(
                querySnapshots -> querySnapshots
                    .getDocuments()
                    .stream()
                    .map(
                        document -> {
                            Message message = document.toObject(Message.class);
                            message.setId(document.getId());
                            return message;
                        }
                    )
                    .collect(Collectors.toList())
            );
    }

    @Override
    public Mono<Message> addMessage(String userId, String chatId, Message message) {
        DocumentReference newMessageRef = messagesCollection(userId, chatId).document();
        message.setId(newMessageRef.getId());

        return Mono.create(sink ->
            newMessageRef
                .set(message)
                .addListener(
                    () -> {
                        try {
                            sink.success(message);
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    }, MoreExecutors.directExecutor()
                )
        );
    }

    @Override
    public Mono<Void> deleteMessageById(String userId, String chatId, String messageId) {
        return Mono
            .fromFuture(
                FutureUtils.toCompletableFuture(
                    messagesCollection(userId, chatId).document(messageId).delete()
                )
            )
            .then();
    }

}
