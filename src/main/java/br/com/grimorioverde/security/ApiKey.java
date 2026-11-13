package br.com.grimorioverde.security;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String proprietario;

    /** SHA-256 (hex) da chave. A chave em texto puro nunca é armazenada. */
    @Column(nullable = false, unique = true, length = 64)
    private String chaveHash;

    /** Primeiros caracteres da chave, só para identificá-la em listagens. */
    @Column(nullable = false, length = 20)
    private String prefixo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NivelAcesso nivel;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(nullable = false)
    private Instant criadaEm = Instant.now();

    public ApiKey() {
    }

    public Long getId() { return id; }

    public String getProprietario() { return proprietario; }
    public void setProprietario(String proprietario) { this.proprietario = proprietario; }

    public String getChaveHash() { return chaveHash; }
    public void setChaveHash(String chaveHash) { this.chaveHash = chaveHash; }

    public String getPrefixo() { return prefixo; }
    public void setPrefixo(String prefixo) { this.prefixo = prefixo; }

    public NivelAcesso getNivel() { return nivel; }
    public void setNivel(NivelAcesso nivel) { this.nivel = nivel; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant criadaEm) { this.criadaEm = criadaEm; }
}
