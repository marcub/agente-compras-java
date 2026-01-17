package br.com.marcusferraz.agentecompras.service.presentation;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.service.UrlShortenerService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhatsappPresenter {

    private final UrlShortenerService urlShortenerService;

    public WhatsappPresenter(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    public String formatComparison(List<ProductDTO> winners, String term) {
        if (winners.isEmpty()) return formatError(term);

        ProductDTO winner = winners.get(0);
        String winnerLink = urlShortenerService.shortLink(winner.url());

        StringBuilder message = new StringBuilder();

        message.append("🏆 *MELHOR PREÇO ENCONTRADO*\n");
        message.append("🔎 _").append(term).append("_\n\n");

        message.append("📦 *").append(winner.store().getName()).append("* venceu!\n");
        message.append("🔥 *R$ ").append(winner.price()).append("*\n");
        message.append("👉 *Comprar:* ").append(winnerLink).append("\n");

        if (winners.size() > 1) {
            message.append("\n━━━━━━━━━━━━━━━━━━━━\n");
            message.append("⚖️ *COMPARATIVO POR LOJA:*\n\n");

            for (int i = 1; i < winners.size(); i++) {
                ProductDTO product = winners.get(i);
                String productLink = urlShortenerService.shortLink(product.url());
                String emoji = getStoreEmoji(product.store().getName());

                message.append(emoji).append(" *").append(product.store().getName()).append("* \n");
                message.append("   💰 R$ ").append(product.price()).append("\n");
                message.append("   🔗 ").append(productLink).append("\n\n");
            }
        }

        message.append("_Preços sujeitos a alteração._");
        return message.toString();
    }

    private String getStoreEmoji(String storeName) {
        if (storeName == null) return "🏪";
        String lower = storeName.toLowerCase();
        if (lower.contains("amazon")) return "📦";
        if (lower.contains("mercado")) return "🤝";
        if (lower.contains("magalu") || lower.contains("magazine")) return "🔵";
        if (lower.contains("shopee")) return "🟠";
        if (lower.contains("americanas")) return "🔴";
        return "🏪";
    }

    public String formatError(String term) {
        return "❌ Poxa, não encontrei ofertas para: *" + term + "* nas lojas parceiras.";
    }
}
