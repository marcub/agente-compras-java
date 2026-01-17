package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.utils.PriceUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShopeeSearcher implements StoreSearcher {

    private static final Logger logger = LoggerFactory.getLogger(ShopeeSearcher.class);

    @Override
    public Store getStore() {
        return Store.SHOPEE;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();
        String shopeeTerm = "shopee " + userTerm;
        String encodedTerm = URLEncoder.encode(shopeeTerm, StandardCharsets.UTF_8);
        String url = "https://www.google.com/search?q=" + encodedTerm + "&udm=28&hl=pt-BR";

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept-Language", "pt-BR")
                .timeout(5000)
                .get();

        Elements items = document.select("li .Ez5pwe");

        for (int i = 0; i < Math.min(20, items.size()); i++) {
            Element item = items.get(i);

            String title = item.select("div .gkQHve SsM98d RmEs5b").first().text();
            String price = item.select("spam .lmQWe").text();
            String relativeLink = item.select("a .contents").first().attr("href");

            if (!title.isEmpty() && !price.isEmpty() && !relativeLink.isEmpty()) {
                String fullPrice = "R$ " + price;
                BigDecimal finalPrice = PriceUtils.cleanPrice(fullPrice);
                String fullLink = "https://shopee.com.br" + relativeLink;
                offers.add(new ProductDTO(title, finalPrice, fullLink, Store.SHOPEE));
            }
        }
        return offers;
    }
}
