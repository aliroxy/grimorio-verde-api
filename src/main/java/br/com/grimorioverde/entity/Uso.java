package br.com.grimorioverde.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usos")
public class Uso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O tipo de uso é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUso tipo;

    @NotBlank(message = "A descrição do uso é obrigatória")
    @Size(min = 10, max = 1000)
    @Column(nullable = false, length = 1000)
    private String descricao;

    @Size(max = 500)
    @Column(length = 500)
    private String modoPreparo;

    @Size(max = 1000)
    @Column(length = 1000)
    private String contraindicacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "erva_id", nullable = false)
    private Erva erva;

    public Uso() {
    }

    public Long getId() {
        return id;
    }

    public TipoUso getTipo() {
        return tipo;
    }

    public void setTipo(TipoUso tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public String getContraindicacoes() {
        return contraindicacoes;
    }

    public void setContraindicacoes(String contraindicacoes) {
        this.contraindicacoes = contraindicacoes;
    }

    public Erva getErva() {
        return erva;
    }

    public void setErva(Erva erva) {
        this.erva = erva;
    }
}
