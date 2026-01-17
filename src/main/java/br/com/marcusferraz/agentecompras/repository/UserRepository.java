package br.com.marcusferraz.agentecompras.repository;

import br.com.marcusferraz.agentecompras.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByWhatsappId(String whatsappId);
}
