package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.utils.PriceUtils;
import br.com.marcusferraz.agentecompras.utils.ScraperUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MagazineLuizaSearcher implements StoreSearcher {

    private static final Logger logger = LoggerFactory.getLogger(AmazonSearcher.class);

    @Override
    public Store getStore() {
        return Store.MAGAZINE_LUIZA;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();
        String encodedTerm = URLEncoder.encode(userTerm, StandardCharsets.UTF_8);
        String url = "https://www.magazineluiza.com.br/busca/" + encodedTerm;

        ScraperUtils.sleepRandomInterval();

        Map<String, String> headers = ScraperUtils.getRandomBrowserProfile();
        Document document = Jsoup.connect(url)
                .headers(headers)
                .timeout(15000)
                .get();

        Elements items = document.select("div[data-testid='product-list'] li");

        for (int i = 0; i < Math.min(20, items.size()); i++) {
            Element item = items.get(i);

            String title = item.select("h2[data-testid='product-title']").text();
            String price = item.select("p[data-testid='price-value']").text();
            String relativeLink = item.select("a[data-testid='product-card-container']").first().attr("href");

            if (!title.isEmpty() && !price.isEmpty() && !relativeLink.isEmpty()) {
                String fullLink = "https://www.magazineluiza.com.br/" + relativeLink;
                BigDecimal finalPrice = PriceUtils.cleanPrice(price);
                offers.add(new ProductDTO(title, finalPrice, fullLink, Store.MAGAZINE_LUIZA));
            }
        }
        return offers;
    }
}
