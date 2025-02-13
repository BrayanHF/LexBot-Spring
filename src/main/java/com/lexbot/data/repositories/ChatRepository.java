package com.lexbot.data.repositories;

import com.lexbot.data.firestore_dao.Chat;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ChatRepository {

    Mono<List<Chat>> userChats(String userId);

    Mono<Chat> addChat(String userId, Chat chat);

    Mono<Map<String, Object>> updateChatByFields(String userId, String chatId, Map<String, Object> updates);

    Mono<Void> deleteChatById(String userId, String chatId);

}
