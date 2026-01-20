package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.exception.UserNotFoundException;
import br.com.marcusferraz.agentecompras.model.ChatMessage;
import br.com.marcusferraz.agentecompras.model.User;
import br.com.marcusferraz.agentecompras.model.enums.ChatMessageRole;
import br.com.marcusferraz.agentecompras.repository.ChatMessageRepository;
import br.com.marcusferraz.agentecompras.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    private static final int HISTORY_MESSAGES_LIMIT = 10;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    public List<ChatMessage> getHistory(String whatsappId) {
        return chatMessageRepository.findLastMessages(whatsappId, HISTORY_MESSAGES_LIMIT);
    }

    public void addChatMessage(String whatsappId, String role, String content) {
        User user = userRepository.findByWhatsappId(whatsappId)
                .orElseThrow(() -> new UserNotFoundException("User not found with whatsappId: " + whatsappId));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setChatMessageRole(ChatMessageRole.valueOf(role.toUpperCase()));
        chatMessage.setContent(content);

        chatMessageRepository.save(chatMessage);
    }
}
