package br.com.grimorioverde.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ervas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_erva_nome_cientifico",
                        columnNames = "nome_cientifico"
                )
        }
)
public class Erva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome popular é obrigatório")
    @Size(min = 2, max = 100, message = "O nome popular deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nomePopular;

    @NotBlank(message = "O nome científico é obrigatório")
    @Size(min = 2, max = 150, message = "O nome científico deve ter entre 2 e 150 caracteres")
    @Column(name = "nome_cientifico", nullable = false, length = 150)
    private String nomeCientifico;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "A descrição deve ter entre 10 e 1000 caracteres")
    @Column(nullable = false, length = 1000)
    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaErva categoria;

    @Size(max = 100, message = "A origem deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String origem;

    @OneToOne(
            mappedBy = "erva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private FichaBotanica fichaBotanica;

    @OneToMany(
            mappedBy = "erva",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Uso> usos = new ArrayList<>();

    public Erva() {
    }

    public Long getId() {
        return id;
    }

    public String getNomePopular() {
        return nomePopular;
    }

    public void setNomePopular(String nomePopular) {
        this.nomePopular = nomePopular;
    }

    public String getNomeCientifico() {
        return nomeCientifico;
    }

    public void setNomeCientifico(String nomeCientifico) {
        this.nomeCientifico = nomeCientifico;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public CategoriaErva getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaErva categoria) {
        this.categoria = categoria;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public FichaBotanica getFichaBotanica() {
        return fichaBotanica;
    }

    public void setFichaBotanica(FichaBotanica fichaBotanica) {
        this.fichaBotanica = fichaBotanica;
    }

    public List<Uso> getUsos() {
        return usos;
    }

    public void setUsos(List<Uso> usos) {
        this.usos = usos;
    }
}
