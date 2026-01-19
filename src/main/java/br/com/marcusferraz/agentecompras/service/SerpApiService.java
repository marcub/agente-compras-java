package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Service
public class SerpApiService {

    private static final Logger logger = LoggerFactory.getLogger(SerpApiService.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${serpapi.api.key}")
    private String apiKey;

    @Value("${serpapi.url}")
    private String url;

    public SerpApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public JsonNode searchGoogleShoppingLite(String query) {
        try {

            URI uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("engine", "google_shopping_light")
                    .queryParam("q", query)
                    .queryParam("google_domain", "google.com.br")
                    .queryParam("hl", "pt-br")
                    .queryParam("gl", "br")
                    .queryParam("location", "Brazil")
                    .queryParam("shoprs", "CAEYAiodcGFuZWxhIGRlIHByZXNzw6NvIHRyYW1vbnRpbmEyCggCEgZTaG9wZWVYjrQgYAI")
                    .queryParam("api_key", apiKey)
                    .build()
                    .toUri();

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (Exception e) {
            logger.error("Error during SerpApi Google Shopping search for query: {}", query, e);
            throw new ExternalServiceException("Failed to perform search on SerpApi", e);
        }
    }

    private JsonNode googleImmersiveProduct(String pageToken) {
        try {

            URI uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("engine", "google_immersive_product")
                    .queryParam("page_token", pageToken)
                    .queryParam("api_key", apiKey)
                    .build()
                    .toUri();

            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return objectMapper.readTree(response);
        } catch (Exception e) {
            logger.error("Error during SerpApi Google Immersive Product for query: {}", pageToken, e);
            throw new ExternalServiceException("Failed to perform search on SerpApi", e);
        }
    }

    public String getShopeeDirectLink(String pageToken) {
        try {
            JsonNode root = googleImmersiveProduct(pageToken);
            JsonNode stores = root.path("product_results").path("stores");

            if (stores.isArray()) {
                for (JsonNode store : stores) {
                    String name = store.path("name").asString();
                    if (name.toLowerCase().contains("shopee")) {
                        return store.path("link").asString();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to extract direct Shopee link for token: {}", pageToken, e);
        }
        return null;
    }
}
