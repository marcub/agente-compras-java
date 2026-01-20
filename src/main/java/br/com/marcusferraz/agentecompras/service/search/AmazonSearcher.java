package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.utils.PriceUtils;
import br.com.marcusferraz.agentecompras.utils.ScraperUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AmazonSearcher implements StoreSearcher {

    @Override
    public Store getStore() {
        return Store.AMAZON;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();
        String encodedTerm = URLEncoder.encode(userTerm, StandardCharsets.UTF_8);
        String url = "https://www.amazon.com.br/s?k=" + encodedTerm;

        ScraperUtils.sleepRandomInterval();

        Map<String, String> headers = ScraperUtils.getRandomBrowserProfile();
        Document document = Jsoup.connect(url)
                .headers(headers)
                .timeout(15000)
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
