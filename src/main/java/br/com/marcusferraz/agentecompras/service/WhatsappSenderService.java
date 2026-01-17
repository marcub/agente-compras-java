package br.com.marcusferraz.agentecompras.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsappSenderService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappSenderService.class);

    private final RestClient restClient;

    @Value("${evolution.url}")
    private String evolutionUrl;

    @Value("${evolution.token}")
    private String token;

    @Value("${evolution.instance}")
    private String instance;

    public WhatsappSenderService() {
        this.restClient = RestClient.create();
    }

    public void sendText(String whatsappId, String text) {
        String uri = evolutionUrl + "/message/sendText/" + instance;

        Map<String, Object> body = Map.of(
                "number", whatsappId,
                "delay", 1200,
                "text", text
        );

        try {
            restClient.post()
                    .uri(uri)
                    .header("apikey", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            logger.info("Sending message to: {}", whatsappId);
        } catch (Exception e) {
            logger.error("Error sending message to: {}", whatsappId, e);
        }
    }
}
