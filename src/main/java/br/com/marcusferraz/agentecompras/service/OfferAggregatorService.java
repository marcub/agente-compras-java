package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.dto.ProductDTO;
import br.com.marcusferraz.agentecompras.model.enums.SearchLogStatus;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import br.com.marcusferraz.agentecompras.service.formatter.WhatsappMessageFormatter;
import br.com.marcusferraz.agentecompras.service.search.StoreSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OfferAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(OfferAggregatorService.class);
    private final List<StoreSearcher> storeSearchers;
    private final WhatsappSenderService whatsappSenderService;
    private final WhatsappMessageFormatter whatsappMessageFormatter;
    private final GroqService groqService;
    private final SearchLogService searchLogService;
    private final ChatMessageService chatMessageService;
    private final SerpApiService serpApiService;

    public OfferAggregatorService(List<StoreSearcher> storeSearchers, WhatsappSenderService whatsappSenderService, WhatsappMessageFormatter whatsappMessageFormatter, GroqService groqService, SearchLogService searchLogService, ChatMessageService chatMessageService, SerpApiService serpApiService) {
        this.storeSearchers = storeSearchers;
        this.whatsappSenderService = whatsappSenderService;
        this.whatsappMessageFormatter = whatsappMessageFormatter;
        this.groqService = groqService;
        this.searchLogService = searchLogService;
        this.chatMessageService = chatMessageService;
        this.serpApiService = serpApiService;
    }

    @Async
    public void processSearch(String term, String whatsappId) {
        List<ProductDTO> allOffers = Collections.synchronizedList(new ArrayList<>());

        storeSearchers.parallelStream().forEach(searcher -> {
            try {
                List<ProductDTO> storeOffers = searcher.search(term);

                if (storeOffers == null) {
                    storeOffers = Collections.emptyList();
                }

                allOffers.addAll(storeOffers);

                searchLogService.addSearchLog(whatsappId, term, storeOffers.size(), searcher.getStore(), SearchLogStatus.SUCCESS);
            } catch (Exception e) {
                searchLogService.addSearchLog(whatsappId, term, 0, searcher.getStore(), SearchLogStatus.FAILURE);
                logger.error("Error searching offers in store: {}", searcher.getStore().getName(), e);
            }
        });

        if (allOffers.isEmpty()) {
            String formattedText = whatsappMessageFormatter.formatError(term);
            chatMessageService.addChatMessage(whatsappId, "assistant", formattedText);
            whatsappSenderService.sendText(whatsappId, formattedText);
            return;
        }

        List<ProductDTO> bestOffers = filterAndGroupByStore(allOffers, term);

        List<ProductDTO> finalOffers = new ArrayList<>();
        for (ProductDTO offer : bestOffers) {
            if (offer.store() == Store.SHOPEE && offer.externalId() != null) {
                String realLink = serpApiService.getShopeeDirectLink(offer.externalId());
                if (realLink != null && !realLink.isEmpty()) {
                    finalOffers.add(new ProductDTO(
                            offer.title(),
                            offer.price(),
                            realLink,
                            offer.store(),
                            offer.externalId()
                    ));
                } else {
                    finalOffers.add(offer);
                }
            } else {
                finalOffers.add(offer);
            }
        }
        sendComparison(finalOffers, whatsappId, term);
    }

    private List<ProductDTO> filterAndGroupByStore(List<ProductDTO> offers, String term) {
        if (offers.isEmpty()) return offers;

        List<ProductDTO> preFiltered = offers.stream()
                .filter(p -> p.price().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (preFiltered.size() <= 1) return preFiltered;

        List<Integer> validIds = groqService.filterBestOffers(term, preFiltered);

        List<ProductDTO> filtered = new ArrayList<>();
        for (int i = 0; i < preFiltered.size(); i++) {
            if (validIds.contains(i)) {
                filtered.add(preFiltered.get(i));
            }
        }

        Map<Store, ProductDTO> winners = filtered.stream()
                .collect(Collectors.toMap(
                        ProductDTO::store,
                        Function.identity(),
                        (productA, productB) -> {
                            return productA.price().compareTo(productB.price()) < 0 ? productA : productB;
                        }
                ));

        return winners.values().stream()
                .sorted(Comparator.comparing(ProductDTO::price))
                .toList();

    }

    private void sendComparison(List<ProductDTO> offers, String whatsappId, String term) {
        if (offers.isEmpty()) {
            String text = whatsappMessageFormatter.formatError(term);
            chatMessageService.addChatMessage(whatsappId, "assistant", text);
            whatsappSenderService.sendText(whatsappId, text);
            return;
        }

        String formattedText = whatsappMessageFormatter.formatComparison(offers, term);

        chatMessageService.addChatMessage(whatsappId, "assistant", formattedText);
        whatsappSenderService.sendText(whatsappId, formattedText);
    }
}
