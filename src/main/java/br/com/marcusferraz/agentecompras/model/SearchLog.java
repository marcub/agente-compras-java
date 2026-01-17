package br.com.marcusferraz.agentecompras.model;

import br.com.marcusferraz.agentecompras.model.enums.SearchLogStatus;
import br.com.marcusferraz.agentecompras.model.enums.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_logs")
@Getter
@Setter
@NoArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_term", nullable = false)
    private String searchTerm;

    @Enumerated(EnumType.STRING)
    private Store store;

    @Column(name = "items_found")
    private Integer itemsFound;

    @Column(name = "search_log_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SearchLogStatus searchLogStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
