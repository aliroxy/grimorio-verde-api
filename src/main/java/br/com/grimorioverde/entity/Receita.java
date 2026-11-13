package br.com.grimorioverde.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "receitas")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da receita é obrigatório")
    @Size(min = 2, max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "Os ingredientes são obrigatórios")
    @Size(min = 5, max = 2000)
    @Column(nullable = false, length = 2000)
    private String ingredientes;

    @NotBlank(message = "O modo de preparo é obrigatório")
    @Size(min = 10, max = 2000)
    @Column(nullable = false, length = 2000)
    private String modoPreparo;

    @ManyToMany
    @JoinTable(
            name = "receitas_ervas",
            joinColumns = @JoinColumn(name = "receita_id"),
            inverseJoinColumns = @JoinColumn(name = "erva_id")
    )
    private Set<Erva> ervas = new HashSet<>();

    public Receita() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public String getModoPreparo() {
        return modoPreparo;
    }

    public void setModoPreparo(String modoPreparo) {
        this.modoPreparo = modoPreparo;
    }

    public Set<Erva> getErvas() {
        return ervas;
    }

    public void setErvas(Set<Erva> ervas) {
        this.ervas = ervas;
    }
}
