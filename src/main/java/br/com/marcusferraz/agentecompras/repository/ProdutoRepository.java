package br.com.marcusferraz.agentecompras.repository;

import br.com.marcusferraz.agentecompras.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByUrl(String url);
}
