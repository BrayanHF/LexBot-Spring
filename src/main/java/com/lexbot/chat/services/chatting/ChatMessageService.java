package com.lexbot.chat.services.chatting;

import com.lexbot.ai.dto.Role;
import com.lexbot.data.firestore_dao.Chat;
import com.lexbot.data.firestore_dao.Message;
import com.lexbot.data.services.ChatService;
import com.lexbot.data.services.MessageService;
import com.lexbot.utils.prompts.chat.ChatPrompt;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@AllArgsConstructor
public class ChatMessageService {

    private final ChatService chatService;
    private final MessageService messageService;
    private final AIServiceManager aiServiceManager;

    public Mono<Chat> getOrCreateChat(String userId, String chatId, String message) {
        if (chatId != null && !chatId.isEmpty()) {
            return chatService.getChatById(userId, chatId);
        }

        return chatService.addChat(userId)
            .flatMap(
                chat -> {
                    aiServiceManager.generateTitleForChat(userId, chat.getId(), message);
                    return Mono.just(chat);
                }
            )
            .onErrorResume(e -> Mono.error(new RuntimeException("Error creating a new chat", e)));
    }

    public Mono<String> getChatResume(String userId, String chatId) {
        return chatService.getChatById(userId, chatId)
            .map(chat -> chat.getResume() == null ? "" : chat.getResume());
    }

    public void saveMessages(String userId, String chatId, String userMessageText, String assistantMessageText) {

        messageService.getNextConversationIndex(userId, chatId)
            .flatMap(index -> {

                var userMessage = Message.builder()
                    .role(Role.USER)
                    .text(userMessageText)
                    .conversationIndex(index)
                    .build();

                var assistantMessage = Message.builder()
                    .role(Role.ASSISTANT)
                    .text(assistantMessageText)
                    .conversationIndex(index)
                    .build();


                return Mono.when(
                        messageService.addMessage(userId, chatId, userMessage)
                            .onErrorResume(e -> Mono.error(new RuntimeException("Error saving user message", e))),

                        messageService.addMessage(userId, chatId, assistantMessage)
                            .onErrorResume(e -> Mono.error(new RuntimeException("Error saving assistant message", e))),

                        resumeChat(userId, chatId, userMessageText, assistantMessageText)
                    )
                    .subscribeOn(Schedulers.boundedElastic());
            })
            .subscribe();

    }

    private Mono<Void> resumeChat(String userId, String chatId, String userMessage, String assistantMessage) {

        return getChatResume(userId, chatId)
            .flatMap(chatResume -> {

                String messageToChat = """
                    Existing summary:
                    %s
                    
                    Latest user message:
                    %s
                    
                    Latest assistant message:
                    %s
                    
                    Update the summary following the rules.
                    """.formatted(
                    chatResume,
                    userMessage,
                    assistantMessage
                );

                return aiServiceManager.generateAIMessageUnlimited(messageToChat, ChatPrompt.CHAT_RESUME_PROMPT)
                    .flatMap(aiChatResponse ->
                        chatService.updateChatByFields(userId, chatId, Map.of("resume", aiChatResponse.getChoices().getFirst().getResponse().getContent()))
                    );
            })
            .then();
    }

}
