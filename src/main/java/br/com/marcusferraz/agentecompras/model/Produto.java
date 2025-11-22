package br.com.marcusferraz.agentecompras.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String url;

    private String nome;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL)
    private List<HistoricoPreco> historico = new ArrayList<>();

    public Produto(String url, String nome) {
        this.url = url;
        this.nome = nome;
    }
}
