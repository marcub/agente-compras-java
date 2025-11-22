package br.com.marcusferraz.agentecompras.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoricoPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double valor;

    private LocalDateTime dataVerificacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    public HistoricoPreco(Double valor, Produto produto) {
        this.valor = valor;
        this.produto = produto;
    }
}
