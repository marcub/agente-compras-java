package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.dto.llm.LlmAnalysisResult;
import br.com.marcusferraz.agentecompras.dto.llm.LlmRequest;
import br.com.marcusferraz.agentecompras.dto.llm.LlmResponse;
import br.com.marcusferraz.agentecompras.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GroqService {

    private static final Logger logger = LoggerFactory.getLogger(GroqService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    @Value("${groq.url}")
    private String url;

    public GroqService(ObjectMapper objectMapper, ChatMessageService chatMessageService) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.chatMessageService = chatMessageService;
    }

    public LlmAnalysisResult analyzeUserIntent(String userText, String remoteJid) {

        String systemPrompt = """
            You are an intelligent and proactive personal shopping assistant.
            Your mission is to transform vague desires into precise search terms for e-commerce.
            
            IMPORTANT:
            - YOUR INTERNAL LOGIC IS IN ENGLISH.
            - YOUR FINAL OUTPUT FOR CHAT PHASE IN SIMPLE RESPONSE MUST BE IN PORTUGUESE (PT-BR).
            
            RESPONSE STRUCTURE (JSON REQUIRED):
            {
              "isPurchaseIntent": boolean,
              "productName": string | null,
              "simpleResponse": string | null
            }
        
            INTELLIGENCE GUIDELINES:
        
            1. REFINEMENT PHASE (User is vague):
               - If user says "I want a playstation" or "I need a phone":
               - SET "isPurchaseIntent": false.
               - IN "simpleResponse", ask for critical specs (Model, Version, Size).
               - Ex: "Which PlayStation? The 4, 5 Slim, or Pro?"
        
            2. SEARCH PHASE (User is specific OR you refined it):
               - If user says "PlayStation 5 Slim" or "iPhone 15 128GB":
               - SET "isPurchaseIntent": true.
               - SET "simpleResponse": null.
               - TERM OPTIMIZATION ("productName"): Add the category to the term.
                 - "iPhone 13" -> "Smartphone iPhone 13"
            
            3. CHAT PHASE (Casual):
               - If not shopping: "isPurchaseIntent": false.
               - Reply politely in "simpleResponse".
            """;

        List<LlmRequest.Message> messages = new ArrayList<>();

        messages.add(new LlmRequest.Message("system", systemPrompt));
        List<ChatMessage> messagesHistory = chatMessageService.getHistory(remoteJid);
        Collections.reverse(messagesHistory);
        List<LlmRequest.Message> messagesHistoryLlm = messagesHistory.stream()
                        .map(msg -> new LlmRequest.Message(
                                msg.getChatMessageRole().toString().toLowerCase(),
                                msg.getContent())
                        ).toList();
        messages.addAll(messagesHistoryLlm);
        messages.add(new LlmRequest.Message("user", userText));

        try {
            logger.info("Analyzing user intent with Groq LLM: {}", userText);

            LlmRequest requestBody = new LlmRequest(
                    this.model,
                    messages,
                    new LlmRequest.ResponseFormat("json_object")
            );

            LlmResponse response = callGroqApi(requestBody);

            if (response == null || response.choices().isEmpty()) {
                return new LlmAnalysisResult(false, null, "Ocorreu um erro. Tente novamente.");
            }

            String jsonResponse = response.choices().get(0).message().content();
            return objectMapper.readValue(jsonResponse, LlmAnalysisResult.class);
        } catch (Exception e) {
            logger.error("Error calling Groq", e);
            return new LlmAnalysisResult(false, null, "Desculpe, estou meio lento agora. Pode repetir?");
        }
    }

    public List<Integer> filterBestOffers(String userTerm, List<ProductDTO> offers) {

        try {
            ArrayNode jsonOffers = objectMapper.createArrayNode();
            for (int i = 0; i < offers.size(); i++) {
                ObjectNode node = jsonOffers.addObject();
                node.put("id", i);
                node.put("item", offers.get(i).title());
                node.put("price", offers.get(i).price());
            }

            String prompt = """
                You are an E-commerce Quality Auditor.
                User wants: "%s".
                
                Filter out the garbage from the list below.
                RULES:
                1. Remove accessories if the user wants the main product.
                2. Remove incompatible items.
                3. Remove suspiciously low prices.
                
                Return JSON: { "validIds": [0, 2, 5] }
                """.formatted(userTerm);

            LlmRequest request = new LlmRequest(
                    this.model,
                    List.of(
                            new LlmRequest.Message("system", prompt),
                            new LlmRequest.Message("user", jsonOffers.toString())
                    ),
                    new LlmRequest.ResponseFormat("json_object")
            );

            LlmResponse response = callGroqApi(request);
            String jsonResponse = response.choices().get(0).message().content();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            List<Integer> validIds = new ArrayList<>();
            if (rootNode.has("validIds")) {
                rootNode.get("validIds").forEach(id -> validIds.add(id.asInt()));
            }
            return validIds;
        } catch (Exception e) {
            logger.info("Error filtering offers", e);
            return offers.stream().map(offers::indexOf).toList();
        }
    }

    private LlmResponse callGroqApi(LlmRequest request) {
        return restClient.post()
                .uri(this.url)
                .header("Authorization", "Bearer " + this.apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LlmResponse.class);
    }
}
