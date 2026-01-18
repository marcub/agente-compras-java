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
public class AmazonSearcher implements StoreSearcher {

    private static final Logger logger = LoggerFactory.getLogger(AmazonSearcher.class);

    @Override
    public Store getStore() {
        return Store.AMAZON;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();
        String encodedTerm = URLEncoder.encode(userTerm, StandardCharsets.UTF_8);
        String url = "https://www.amazon.com.br/s?k=" + encodedTerm;

        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Referer", "https://www.google.com/")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "cross-site")
                .header("Sec-Fetch-User", "?1")
                .timeout(10000)
                .get();

        Elements items = document.select("div[data-component-type='s-search-result']");

        for (int i = 0; i < Math.min(20, items.size()); i++) {
            Element item = items.get(i);

            String title = item.select("h2 span").text();
            String price = item.select(".a-price-whole").text();
            String relativeLink = item.select(".a-link-normal").first().attr("href");

            if (!title.isEmpty() && !price.isEmpty() && !relativeLink.isEmpty()) {
                String priceFraction = item.select(".a-price-fraction").text();
                String fullPrice = "R$ " + price + (priceFraction.isEmpty() ? ",00" : priceFraction);
                String fullLink = "https://amazon.com.br" + relativeLink;
                BigDecimal finalPrice = PriceUtils.cleanPrice(fullPrice);
                offers.add(new ProductDTO(title, finalPrice, fullLink, Store.AMAZON));
            }
        }
        return offers;
    }
}
