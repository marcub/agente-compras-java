package br.com.marcusferraz.agentecompras.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsappSenderService {

    private final RestClient restClient;

    @Value("${evoliton.url}")
    private String evolutionUrl;

    @Value("${evolution.token}")
    private String token;

    @Value("${evolution.instance}")
    private String instance;

    public WhatsappSenderService() {
        this.restClient = RestClient.create();
    }

    public void enviarTexto(String numeroDestino, String texto) {
        String uri = evolutionUrl + "/message/sendText/" + instance;

        Map<String, Object> body = Map.of(
                "number", numeroDestino,
                "options", Map.of(
                        "delay", 1200,
                        "presence", "composing"
                ),
                "textMessage", Map.of(
                        "text", texto
                )
        );

        try {
            restClient.post()
                    .uri(uri)
                    .header("apikey", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            System.out.println("📤 Resposta enviada para: " + numeroDestino);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
        }
    }
}
