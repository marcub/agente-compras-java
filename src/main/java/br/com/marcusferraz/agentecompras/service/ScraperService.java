package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.dto.ProdutoDTO;
import br.com.marcusferraz.agentecompras.model.HistoricoPreco;
import br.com.marcusferraz.agentecompras.model.Produto;
import br.com.marcusferraz.agentecompras.repository.ProdutoRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ScraperService {

    private final ProdutoRepository produtoRepository;

    public ScraperService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoDTO buscarNaAmazon(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get();

            Element tituloEl = doc.selectFirst("#productTitle");
            String titulo = (tituloEl != null) ? tituloEl.text() : "Título não encontrado";

            Element precoInteiroEl = doc.selectFirst(".a-price-whole");
            Element precoFracaoEl = doc.selectFirst(".a-price-fraction");

            Double precoFinal = 0.0;
            if (precoInteiroEl != null) {
                String precoTexto = precoInteiroEl.text().replace(".", "").replace(",", ".");
                precoFinal = Double.parseDouble(precoTexto);
                if (precoFracaoEl != null) {
                    precoFinal += Double.parseDouble(precoFracaoEl.text()) / 100;
                }
            }

            Produto produto = produtoRepository.findByUrl(url)
                    .orElse(new Produto(url, titulo));

            HistoricoPreco historico = new HistoricoPreco(precoFinal, produto);
            produto.getHistorico().add(historico);

            produtoRepository.save(produto);

            return new ProdutoDTO(titulo, "R$ " + precoFinal, url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
