package br.com.marcusferraz.agentecompras.dto;

import br.com.marcusferraz.agentecompras.model.enums.Store;

import java.math.BigDecimal;

public record ProductDTO(
        String title,
        BigDecimal price,
        String url,
        Store store
) {}
