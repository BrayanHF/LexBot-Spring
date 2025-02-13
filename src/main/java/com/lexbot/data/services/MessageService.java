package com.lexbot.data.services;

import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.repositories.MessageRepository;
import com.lexbot.utils.validations.SimpleValidation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Mono<List<Message>> chatMessages(String userId, String chatId) {
        SimpleValidation.validateStrings(userId, chatId);

        return messageRepository.chatMessages(userId, chatId);
    }

    public Mono<Message> addMessage(String userId, String chatId, Message message) {
        SimpleValidation.validateNotNulls(message);
        SimpleValidation.validateStrings(userId, chatId, message.getText());

        message.setDate(new Date());
        return messageRepository.addMessage(userId, chatId, message);
    }

    public Mono<Void> deleteMessageById(String userId, String chatId, String messageId) {
        SimpleValidation.validateStrings(userId, chatId, messageId);

        return messageRepository.deleteMessageById(userId, chatId, messageId);
    }

}
