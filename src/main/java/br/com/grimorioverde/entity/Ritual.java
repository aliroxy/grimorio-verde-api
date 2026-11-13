package br.com.grimorioverde.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rituais")
public class Ritual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do ritual é obrigatório")
    @Size(min = 2, max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "A finalidade espiritual é obrigatória")
    @Size(min = 10, max = 1000)
    @Column(nullable = false, length = 1000)
    private String finalidadeEspiritual;

    @NotBlank(message = "As instruções são obrigatórias")
    @Size(min = 10, max = 2000)
    @Column(nullable = false, length = 2000)
    private String instrucoes;

    @Size(max = 1000)
    @Column(length = 1000)
    private String cuidados;

    @ManyToMany
    @JoinTable(
            name = "rituais_ervas",
            joinColumns = @JoinColumn(name = "ritual_id"),
            inverseJoinColumns = @JoinColumn(name = "erva_id")
    )
    private Set<Erva> ervas = new HashSet<>();

    public Ritual() {
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

    public String getFinalidadeEspiritual() {
        return finalidadeEspiritual;
    }

    public void setFinalidadeEspiritual(String finalidadeEspiritual) {
        this.finalidadeEspiritual = finalidadeEspiritual;
    }

    public String getInstrucoes() {
        return instrucoes;
    }

    public void setInstrucoes(String instrucoes) {
        this.instrucoes = instrucoes;
    }

    public String getCuidados() {
        return cuidados;
    }

    public void setCuidados(String cuidados) {
        this.cuidados = cuidados;
    }

    public Set<Erva> getErvas() {
        return ervas;
    }

    public void setErvas(Set<Erva> ervas) {
        this.ervas = ervas;
    }
}
