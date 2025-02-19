package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.repositories.ChatRepository;
import com.lexbot.utils.validations.SimpleValidation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    public Mono<List<Chat>> userChats(String userId) {
        SimpleValidation.validateStrings(userId);

        return chatRepository.userChats(userId);
    }

    public Mono<Chat> addChat(String userId) {
        SimpleValidation.validateStrings(userId);

        var chat = Chat.builder()
            .title("Nuevo chat")
            .lastUse(new Date())
            .build();

        return chatRepository.addChat(userId, chat);
    }

    public Mono<Map<String, Object>> updateChatByFields(String userId, String chatId, Map<String, Object> updates) {
        SimpleValidation.validateStrings(userId, chatId);
        SimpleValidation.validateUpdates(Chat.class, updates);

        return chatRepository.updateChatByFields(userId, chatId, updates);
    }

    public Mono<Void> deleteChatById(String userId, String chatId) {
        SimpleValidation.validateStrings(userId, chatId);

        return chatRepository.deleteChatById(userId, chatId);
    }

}
