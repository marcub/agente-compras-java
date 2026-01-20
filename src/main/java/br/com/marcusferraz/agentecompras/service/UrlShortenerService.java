package br.com.marcusferraz.agentecompras.service;

import br.com.marcusferraz.agentecompras.model.ShortLink;
import br.com.marcusferraz.agentecompras.repository.ShortLinkRepository;
import br.com.marcusferraz.agentecompras.utils.Base62Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlShortenerService {

    private final ShortLinkRepository shortLinkRepository;
    private static final long OFFSET = 100000L;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlShortenerService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }

    public String shortLink(String url) {
        String hash = new ShortLink(url).getUrlHash();

        return shortLinkRepository.findByUrlHash(hash)
                .map(shortLink -> generateFinalUrl(shortLink.getId()))
                .orElseGet(() -> {
                    ShortLink newShortLink = new ShortLink(url);
                    shortLinkRepository.save(newShortLink);
                    return generateFinalUrl(newShortLink.getId());
                });
    }

    public String decodeUrl(String code) {
        try {
            long id = Base62Utils.decode(code);
            long realId = id - OFFSET;

            return shortLinkRepository.findById(realId)
                    .map(ShortLink::getUrl)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateFinalUrl(Long id) {
        return baseUrl + "/r/" + Base62Utils.encode(id + OFFSET);
    }
}
