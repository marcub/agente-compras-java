package br.com.marcusferraz.agentecompras.service.formatter;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.service.UrlShortenerService;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class WhatsappMessageFormatter {

    private final UrlShortenerService urlShortenerService;

    public WhatsappMessageFormatter(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    public String formatComparison(List<ProductDTO> products, String term) {
        if (products.isEmpty()) return formatError(term);

        StringBuilder message = new StringBuilder();

        message.append("🔎 *Resultado da busca:* _").append(term).append("_\n");
        message.append("━━━━━━━━━━━━━━━━\n\n");

        for (ProductDTO product : products) {
            String productLink = urlShortenerService.shortLink(product.url());
            String storeName = product.store().getName();
            String emoji = getStoreEmoji(storeName);

            message.append(emoji).append(" *").append(storeName).append("*\n");

            message.append("📝 ").append(product.title()).append("\n");
            message.append("💰 *R$ ").append(product.price()).append("*\n");
            message.append("👉 ").append(productLink).append("\n");

            String searchLink = generateSearchLink(product.store(), term);
            if (searchLink != null) {
                String shortSearchLink = urlShortenerService.shortLink(searchLink);
                message.append("🔍 _Ver mais opções:_ ").append(shortSearchLink).append("\n");
            }

            message.append("\n");
        }

        message.append("━━━━━━━━━━━━━━━━━━━━\n");
        message.append("⚠️ _Preços sujeitos a alteração._");

        return message.toString();
    }

    private String generateSearchLink(Store store, String term) {
        try {
            String encodedTerm = URLEncoder.encode(term, StandardCharsets.UTF_8);

            return switch (store) {
                case AMAZON -> "https://www.amazon.com.br/s?k=" + encodedTerm;
                case MAGAZINE_LUIZA -> "https://www.magazineluiza.com.br/busca/" + encodedTerm;
                case SHOPEE -> "https://shopee.com.br/search?keyword=" + encodedTerm;
                case CASAS_BAHIA -> "https://www.casasbahia.com.br/b/" + encodedTerm;
                case MERCADO_LIVRE -> "https://lista.mercadolivre.com.br/" + encodedTerm;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
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
