package com.lexbot.data.repositories.impl.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.common.util.concurrent.MoreExecutors;
import com.lexbot.ai.dto.Role;
import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.firestore_dao.LBUser;
import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.repositories.MessageRepository;
import com.lexbot.data.repositories.impl.firebase.config.FutureUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
            .flatMap(
                querySnapshots -> {
                    var messages = querySnapshots
                        .getDocuments()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(
                            document -> {
                                if (!document.exists()) return null;

                                Message message = document.toObject(Message.class);
                                message.setId(document.getId());
                                return message;
                            }
                        )
                        .sorted(Comparator
                            .comparing(Message::getConversationIndex)
                            .thenComparing(msg -> msg.getRole() == Role.USER ? 0 : 1)
                        )
                        .collect(Collectors.toList());

                    return messages.isEmpty() ? Mono.empty() : Mono.just(messages);
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Failed to fetch messages", e)));
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

    @Override
    public Mono<Integer> getNextConversationIndex(String userId, String chatId) {
        Query query = messagesCollection(userId, chatId)
            .orderBy("conversationIndex", Query.Direction.DESCENDING)
            .limit(1);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(query.get()))
            .map(querySnapshot -> {
                    var documents = querySnapshot.getDocuments();
                    if (documents.isEmpty()) return 0;

                    Long lastIndex = documents.getFirst().getLong("conversationIndex");
                    return lastIndex != null ? lastIndex.intValue() + 1 : 0;
                }
            );
    }

}
