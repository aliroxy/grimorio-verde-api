package br.com.grimorioverde.security;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_escopo_chave", columnNames = {"escopo", "chave"})
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id da chave de API dona da requisição: cada cliente tem seu próprio espaço de chaves. */
    @Column(nullable = false)
    private Long escopo;

    @Column(nullable = false, length = 100)
    private String chave;

    /** SHA-256 do corpo da requisição original. */
    @Column(nullable = false, length = 64)
    private String requestHash;

    /** Id do recurso criado pela requisição original. */
    @Column(nullable = false)
    private Long recursoId;

    @Column(nullable = false)
    private Instant criadoEm = Instant.now();

    public IdempotencyRecord() {
    }

    public IdempotencyRecord(Long escopo, String chave, String requestHash, Long recursoId) {
        this.escopo = escopo;
        this.chave = chave;
        this.requestHash = requestHash;
        this.recursoId = recursoId;
    }

    public Long getId() { return id; }
    public Long getEscopo() { return escopo; }
    public String getChave() { return chave; }
    public String getRequestHash() { return requestHash; }
    public Long getRecursoId() { return recursoId; }
    public Instant getCriadoEm() { return criadoEm; }
}
