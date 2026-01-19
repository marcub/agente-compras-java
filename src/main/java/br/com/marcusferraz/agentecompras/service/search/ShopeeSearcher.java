package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.service.SerpApiService;
import br.com.marcusferraz.agentecompras.utils.PriceUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

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

    private final SerpApiService serpApiService;

    public ShopeeSearcher(SerpApiService serpApiService) {
        this.serpApiService = serpApiService;
    }

    @Override
    public Store getStore() {
        return Store.SHOPEE;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();

        String shopeeTerm = "shopee " + userTerm;

        JsonNode root = serpApiService.searchGoogleShoppingLite(shopeeTerm);
        JsonNode shoppingResults = root.path("shopping_results");

        if (shoppingResults.isMissingNode() || !shoppingResults.isArray()) {
            return offers;
        }

        for (JsonNode item : shoppingResults) {
            String source = item.path("source").asString();

            if (source.toLowerCase().contains("shopee")) {
                String title = item.path("title").asString();
                String googleLink = item.path("product_link").asString();
                String pageToken = item.path("immersive_product_page_token").asString();

                BigDecimal price;
                if (item.has("extracted_price")) {
                    price = BigDecimal.valueOf(item.path("extracted_price").asDouble());
                } else {
                    price = PriceUtils.cleanPrice(item.path("price").asString());
                }

                if (!title.isEmpty() && !pageToken.isEmpty() && price != null && !googleLink.isEmpty()) {
                    offers.add(new ProductDTO(title, price, googleLink, Store.SHOPEE, pageToken));
                }
            }
        }
        return offers;
    }
}
