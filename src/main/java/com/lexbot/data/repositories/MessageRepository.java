package com.lexbot.data.repositories;

import com.lexbot.data.firestore_dao.Message;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MessageRepository {

    Mono<List<Message>> chatMessages(String userId, String chatId);

    Mono<Message> addMessage(String userId, String chatId, Message message);

    Mono<Void> deleteMessageById(String userId, String chatId, String messageId);

    Mono<Integer> getNextConversationIndex(String userId, String chatId);

}
