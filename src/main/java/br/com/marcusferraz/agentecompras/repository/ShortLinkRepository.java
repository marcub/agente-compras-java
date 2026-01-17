package br.com.marcusferraz.agentecompras.repository;

import br.com.marcusferraz.agentecompras.model.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByUrl(String url);
}
