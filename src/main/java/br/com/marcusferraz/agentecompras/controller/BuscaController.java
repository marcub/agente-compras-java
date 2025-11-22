package br.com.marcusferraz.agentecompras.controller;

import br.com.marcusferraz.agentecompras.dto.ProdutoDTO;
import br.com.marcusferraz.agentecompras.service.ScraperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BuscaController {

    private final ScraperService scraperService;

    public BuscaController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/buscar")
    public ProdutoDTO buscar(@RequestParam String url) {
        return scraperService.buscarNaAmazon(url);
    }
}
