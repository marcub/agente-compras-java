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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoLivreSearcher implements StoreSearcher {

    private static final Logger logger = LoggerFactory.getLogger(MercadoLivreSearcher.class);

    @Override
    public Store getStore() {
        return Store.MERCADO_LIVRE;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();
        String encodedTerm = URLEncoder.encode(userTerm, StandardCharsets.UTF_8);
        String url = "https://lista.mercadolivre.com.br/" + encodedTerm;

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(5000)
                .get();

        Elements items = document.select("li.ui-search-layout__item");

        for (int i = 0; i < Math.min(20, items.size()); i++) {
            Element item = items.get(i);

            String title = item.select("a.poly-component__title").text();
            String price = item.select("div.poly-price__current span.andes-money-amount__fraction").first().text();
            String link = item.select("a.poly-component__title").attr("href");

            if (!title.isEmpty() && !price.isEmpty() && !link.isEmpty()) {
                Element priceFraction = item.select("div.poly-price__current span.andes-money-amount__cents").first();
                String fullPrice = "R$ " + price + ",00";
                if (priceFraction != null) {
                    fullPrice = "R$ " + price + "," + priceFraction.text();
                }
                BigDecimal finalPrice = PriceUtils.cleanPrice(fullPrice);
                offers.add(new ProductDTO(title, finalPrice, link, Store.MERCADO_LIVRE));
            }
        }
        return offers;
    }
}
