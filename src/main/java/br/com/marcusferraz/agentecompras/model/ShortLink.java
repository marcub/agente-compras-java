package br.com.marcusferraz.agentecompras.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_links")
@Getter
@Setter
@NoArgsConstructor
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShortLink(String url) {
        this.url = url;
    }
}
