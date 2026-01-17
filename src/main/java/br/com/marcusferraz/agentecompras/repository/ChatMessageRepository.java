package br.com.marcusferraz.agentecompras.repository;

import br.com.marcusferraz.agentecompras.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.user.whatsappId = :whatsappId ORDER BY m.createdAt DESC LIMIT :limit")
    public List<ChatMessage> findLastMessages(@Param("whatsappId") String whatsappId, @Param("limit") int limit);
}
