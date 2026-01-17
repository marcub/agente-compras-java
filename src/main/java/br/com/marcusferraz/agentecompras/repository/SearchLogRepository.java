package br.com.marcusferraz.agentecompras.repository;

import br.com.marcusferraz.agentecompras.model.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
}
