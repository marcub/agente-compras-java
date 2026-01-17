package br.com.marcusferraz.agentecompras.controller;

import br.com.marcusferraz.agentecompras.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    public RedirectController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/r/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        String url = urlShortenerService.decodeUrl(code);
        if (url != null) {
            response.sendRedirect(url);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Link não encontrado");
        }
    }
}
