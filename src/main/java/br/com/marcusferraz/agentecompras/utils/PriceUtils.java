package br.com.marcusferraz.agentecompras.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class PriceUtils {

    private static final Logger logger = LoggerFactory.getLogger(PriceUtils.class);

    public static BigDecimal cleanPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            String cleanedPrice = priceStr.replaceAll("[^0-9,.]", "")
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(cleanedPrice);
        } catch (Exception e) {
            logger.warn("Error converting price: {}", priceStr, e);
            return BigDecimal.ZERO;
        }
    }
}
