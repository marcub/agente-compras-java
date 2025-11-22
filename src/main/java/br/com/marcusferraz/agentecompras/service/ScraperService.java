package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.dto.ProdutoDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ScraperService {

    public ProdutoDTO buscarNaAmazon(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Element tituloEl = doc.selectFirst("#productTitle");
            Element precoInteiroEl = doc.selectFirst(".a-price-whole");
            Element precoFracaoEl = doc.selectFirst(".a-price-fraction");

            String titulo = (tituloEl != null) ? tituloEl.text() : "Título não encontrado";
            String preco = "Indisponível";

            if (precoInteiroEl != null) {
                preco = precoInteiroEl.text();
                if (precoFracaoEl != null) {
                    preco += precoFracaoEl.text();
                }
            }

            return new ProdutoDTO(titulo, "R$ " + preco, url);
        } catch (IOException e) {
            e.printStackTrace();
            return new ProdutoDTO("Erro ao buscar", "0.00", url);
        }
    }
}
