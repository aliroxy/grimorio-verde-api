package br.com.grimorioverde.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "fichas_botanicas")
public class FichaBotanica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A parte utilizada é obrigatória")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String parteUtilizada;

    @NotBlank(message = "A forma de cultivo é obrigatória")
    @Size(min = 5, max = 500)
    @Column(nullable = false, length = 500)
    private String formaCultivo;

    @NotBlank(message = "A descrição das características é obrigatória")
    @Size(min = 10, max = 1000)
    @Column(nullable = false, length = 1000)
    private String caracteristicas;

    @Size(max = 500)
    @Column(length = 500)
    private String advertencias;

    @OneToOne
    @JoinColumn(
            name = "erva_id",
            nullable = false,
            unique = true
    )
    private Erva erva;

    public FichaBotanica() {
    }

    public Long getId() {
        return id;
    }

    public String getParteUtilizada() {
        return parteUtilizada;
    }

    public void setParteUtilizada(String parteUtilizada) {
        this.parteUtilizada = parteUtilizada;
    }

    public String getFormaCultivo() {
        return formaCultivo;
    }

    public void setFormaCultivo(String formaCultivo) {
        this.formaCultivo = formaCultivo;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(String advertencias) {
        this.advertencias = advertencias;
    }

    public Erva getErva() {
        return erva;
    }

    public void setErva(Erva erva) {
        this.erva = erva;
    }
}
