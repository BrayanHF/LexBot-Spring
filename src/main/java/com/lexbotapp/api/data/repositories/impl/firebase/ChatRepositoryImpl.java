package com.lexbotapp.api.data.repositories.impl.firebase;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.common.util.concurrent.MoreExecutors;
import com.lexbotapp.api.data.firestore_dao.Chat;
import com.lexbotapp.api.data.firestore_dao.LBUser;
import com.lexbotapp.api.data.firestore_dao.Message;
import com.lexbotapp.api.data.repositories.ChatRepository;
import com.lexbotapp.api.data.repositories.impl.firebase.config.FutureUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ChatRepositoryImpl implements ChatRepository {

    private final Firestore firestore;

    public ChatRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    private CollectionReference chatsCollection(String userId) {
        return firestore
            .collection(LBUser.PATH).document(userId)
            .collection(Chat.PATH);
    }

    @Override
    public Mono<List<Chat>> userChats(String userId) {
        CollectionReference chatsCollection = chatsCollection(userId);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(chatsCollection.get()))
            .map(
                querySnapshot -> querySnapshot
                    .getDocuments()
                    .stream()
                    .map(
                        document -> {
                            Chat chat = document.toObject(Chat.class);
                            chat.setId(document.getId());
                            return chat;
                        }
                    )
                    .collect(Collectors.toList())
            );
    }

    @Override
    public Mono<Chat> getChatById(String userId, String chatId) {
        DocumentReference chatsCollection = chatsCollection(userId).document(chatId);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(chatsCollection.get()))
            .flatMap(
                document -> {
                    Chat chat = document.toObject(Chat.class);
                    if (chat == null || !document.exists()) return Mono.error(new RuntimeException("Chat not found"));
                    chat.setId(document.getId());
                    return Mono.just(chat);
                }
            );
    }

    @Override
    public Mono<Chat> addChat(String userId, Chat chat) {
        DocumentReference newChatRef = chatsCollection(userId).document();
        chat.setId(newChatRef.getId());

        return Mono.create(sink ->
            newChatRef
                .set(chat)
                .addListener(
                    () -> {
                        try {
                            sink.success(chat);
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    }, MoreExecutors.directExecutor()
                )
        );
    }

    @Override
    public Mono<Map<String, Object>> updateChatByFields(String userId, String chatId, Map<String, Object> updates) {
        return Mono.create(sink ->
            chatsCollection(userId).document(chatId).update(updates)
                .addListener(
                    () -> {
                        try {
                            sink.success(updates);
                        } catch (Exception e) {
                            sink.error(e);
                        }
                    }, MoreExecutors.directExecutor()
                )
        );
    }

    @Override
    public Mono<Void> deleteChatById(String userId, String chatId) {
        var chatRef = chatsCollection(userId).document(chatId);
        var messagesRef = chatRef.collection(Message.PATH);

        return Mono.fromFuture(FutureUtils.toCompletableFuture(messagesRef.get()))
            .flatMapMany(
                qsMessages -> Flux.fromIterable(qsMessages.getDocuments())
            ).flatMap(
                message -> Mono.fromFuture(FutureUtils.toCompletableFuture(
                    message.getReference().delete())
                )
            )
            .then(
                Mono.fromFuture(FutureUtils.toCompletableFuture(
                    chatRef.delete())
                )
            )
            .then();
    }

}
