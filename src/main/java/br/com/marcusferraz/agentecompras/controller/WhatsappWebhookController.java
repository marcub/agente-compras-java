package br.com.marcusferraz.agentecompras.controller;

import br.com.marcusferraz.agentecompras.dto.UserDTO;
import br.com.marcusferraz.agentecompras.dto.llm.LlmAnalysisResult;
import br.com.marcusferraz.agentecompras.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/whatsapp/webhook")
public class WhatsappWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappWebhookController.class);
    private final GroqService groqService;
    private final WhatsappSenderService whatsappSenderService;
    private final OfferAggregatorService offerAggregatorService;
    private final ChatMessageService chatMessageService;

    private final UserService userService;

    public WhatsappWebhookController(GroqService groqService, WhatsappSenderService whatsappSenderService, OfferAggregatorService offerAggregatorService, UserService userService, ChatMessageService chatMessageService) {
        this.groqService = groqService;
        this.whatsappSenderService = whatsappSenderService;
        this.offerAggregatorService = offerAggregatorService;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    public void receiveMessage(@RequestBody Map<String, Object> payload) {
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) return;

        Map<String, Object> key = (Map<String, Object>) data.get("key");
        boolean fromMe = (boolean) key.get("fromMe");

        if (fromMe) return;

        String whatsappId = (String) key.get("remoteJid");
        String name = (String) data.get("pushName");
        UserDTO userDTO = new UserDTO(whatsappId, name);

        userService.createUserIfNotExists(userDTO);

        Map<String, Object> message = (Map<String, Object>) data.get("message");
        String userText = "";

        if (message.containsKey("conversation")) {
            userText = (String) message.get("conversation");
        } else if (message.containsKey("extendedTextMessage")) {
            Map<String, Object> extended = (Map<String, Object>) message.get("extendedTextMessage");
            userText = (String) extended.get("text");
        }

        if (userText != null) {
            chatMessageService.addChatMessage(whatsappId, "user", userText);
            LlmAnalysisResult result = groqService.analyzeUserIntent(userText, whatsappId);
            if (result.isPurchaseIntent()) {
                String assistantText = "🤖 Entendi! Pesquisando preços para: *" + result.productName() + "*...";
                chatMessageService.addChatMessage(whatsappId, "assistant", assistantText);
                whatsappSenderService.sendText(whatsappId, assistantText);
                offerAggregatorService.processSearch(result.productName(), whatsappId);
            } else {
                chatMessageService.addChatMessage(whatsappId, "assistant", result.simpleResponse());
                whatsappSenderService.sendText(whatsappId, result.simpleResponse());
            }
        }
    }
}
