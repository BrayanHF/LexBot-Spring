package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.repositories.ChatRepository;
import com.lexbot.data.services.validations.Validation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Mono<List<Chat>> userChats(String userId) {
        Validation.validateStrings(userId);

        return chatRepository.userChats(userId);
    }

    public Mono<Chat> addChat(String userId, Chat chat) {
        Validation.validateStrings(userId);
        Validation.validateNotNulls(chat);

        chat.setLastUse(new Date());
        return chatRepository.addChat(userId, chat);
    }

    public Mono<Map<String, Object>> updateChatByFields(String userId, String chatId, Map<String, Object> updates) {
        Validation.validateStrings(userId, chatId);
        Validation.validateUpdates(Chat.class, updates);

        return chatRepository.updateChatByFields(userId, chatId, updates);
    }

    public Mono<Void> deleteChatById(String userId, String chatId) {
        Validation.validateStrings(userId, chatId);

        return chatRepository.deleteChatById(userId, chatId);
    }

}
