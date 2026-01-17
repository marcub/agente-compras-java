package br.com.marcusferraz.agentecompras.model.enums;

public enum Store {
    AMAZON("Amazon"),
    MERCADO_LIVRE("Mercado Livre"),
    GOOGLE_SHOPPING("Google Shopping"),
    MAGAZINE_LUIZA("Magazine Luiza"),
    AMERICANAS("Americanas"),
    CASAS_BAHIA("Casas Bahia"),
    SHOPEE("Shopee"),
    OUTROS("Outros");

    private final String name;

    Store(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
