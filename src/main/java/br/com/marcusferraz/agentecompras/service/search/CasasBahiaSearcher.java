package br.com.marcusferraz.agentecompras.service.search;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.service.SerpApiService;
import br.com.marcusferraz.agentecompras.utils.PriceUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CasasBahiaSearcher implements StoreSearcher {

    private final SerpApiService serpApiService;

    public CasasBahiaSearcher(SerpApiService serpApiService) {
        this.serpApiService = serpApiService;
    }

    @Override
    public Store getStore() {
        return Store.CASAS_BAHIA;
    }

    @Override
    public List<ProductDTO> search(String userTerm) throws Exception {
        List<ProductDTO> offers = new ArrayList<>();

        String shopeeTerm = "casas bahia " + userTerm;

        JsonNode root = serpApiService.searchGoogleShoppingLite(shopeeTerm);
        JsonNode shoppingResults = root.path("shopping_results");

        if (shoppingResults.isMissingNode() || !shoppingResults.isArray()) {
            return offers;
        }

        for (JsonNode item : shoppingResults) {
            String source = item.path("source").asString();

            if (source.toLowerCase().contains("casas bahia")) {
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
                    offers.add(new ProductDTO(title, price, googleLink, Store.CASAS_BAHIA, pageToken));
                }
            }
        }
        return offers;
    }
}
