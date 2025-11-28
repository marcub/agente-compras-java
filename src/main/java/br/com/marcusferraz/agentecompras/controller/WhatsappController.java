package br.com.marcusferraz.agentecompras.controller;

import br.com.marcusferraz.agentecompras.dto.ProdutoDTO;
import br.com.marcusferraz.agentecompras.service.ScraperService;
import br.com.marcusferraz.agentecompras.service.WhatsappSenderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WhatsappController {

    private final ScraperService scraperService;
    private final WhatsappSenderService whatsappSenderService;

    @Value("${admin.whatsapp.number}")
    private String adminNumber;

    public WhatsappController(ScraperService scraperService, WhatsappSenderService whatsappSenderService) {
        this.scraperService = scraperService;
        this.whatsappSenderService = whatsappSenderService;
    }

    @PostMapping
    public void receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null) return;

            Map<String, Object> key = (Map<String, Object>) data.get("key");
            boolean fromMe = (boolean) key.get("fromMe");

            if (fromMe) return;

            String remoteJid = (String) key.get("remoteJid");

            if (!remoteJid.startsWith(adminNumber)) {
                System.out.println("⛔ Acesso negado para: " + remoteJid);
                return;
            }

            Map<String, Object> message = (Map<String, Object>) data.get("message");
            String texto = "";

            if (message.containsKey("conversation")) {
                texto = (String) message.get("conversation");
            } else if (message.containsKey("extendedTextMessage")) {
                Map<String, Object> extended = (Map<String, Object>) message.get("extendedTextMessage");
                texto = (String) extended.get("text");
            }

            System.out.println("📩 Mensagem de " + remoteJid + ": " + texto);

            if (texto != null && (texto.contains("amazon.com")) || texto.contains("magazineluiza")) {
                System.out.println("🔎 Detectei um link! Iniciando busca...");
                ProdutoDTO produto = scraperService.buscarNaAmazon(texto);
                System.out.println("✅ Produto encontrado: " + produto.titulo() + " - " + produto.preco());
                String resposta = "Encontrei! \n📦 *" + produto.titulo() + "*\n💰 Preço: " + produto.preco();
                whatsappSenderService.enviarTexto(remoteJid, resposta);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
        }
    }
}
