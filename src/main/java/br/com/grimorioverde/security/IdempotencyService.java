package br.com.grimorioverde.security;

import br.com.grimorioverde.exception.ConflitoException;
import br.com.grimorioverde.exception.IdempotenciaException;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.exception.RequisicaoInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Idempotência para POST com o cabeçalho X-Idempotency-Key.
 * A primeira requisição cria o recurso e guarda (chave, hash do corpo, id criado).
 * Repetições com a mesma chave e o mesmo corpo devolvem o recurso já criado, sem criar outro.
 * A mesma chave com corpo diferente é rejeitada (422).
 */
@Service
public class IdempotencyService {

    public static final String HEADER = "X-Idempotency-Key";
    private static final Pattern FORMATO_VALIDO = Pattern.compile("^[A-Za-z0-9._:-]{8,100}$");

    /** @param reaproveitado true quando a resposta veio de uma requisição anterior com a mesma chave */
    public record Resultado<T>(T recurso, boolean reaproveitado) {
    }

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public <T> Resultado<T> executar(Long escopo, String chave, Object corpo,
                                     Supplier<T> criar, Function<T, Long> idDoRecurso, Function<Long, T> buscar) {
        validarChave(chave);
        String hashDoCorpo = Hashes.sha256(String.valueOf(corpo));

        Optional<IdempotencyRecord> existente = repository.findByEscopoAndChave(escopo, chave);
        if (existente.isPresent()) {
            IdempotencyRecord registro = existente.get();
            if (!registro.getRequestHash().equals(hashDoCorpo)) {
                throw new IdempotenciaException("A chave " + HEADER + " já foi usada com um corpo de requisição diferente. "
                        + "Gere uma nova chave para uma nova operação.");
            }
            try {
                return new Resultado<>(buscar.apply(registro.getRecursoId()), true);
            } catch (RecursoNaoEncontradoException e) {
                throw new ConflitoException("A requisição com esta chave já foi processada, mas o recurso criado "
                        + "foi removido depois. Use uma nova chave para criar de novo.");
            }
        }

        T recurso = criar.get();
        try {
            repository.saveAndFlush(new IdempotencyRecord(escopo, chave, hashDoCorpo, idDoRecurso.apply(recurso)));
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException("Outra requisição com a mesma chave " + HEADER + " está sendo processada. "
                    + "Aguarde e tente novamente.");
        }
        return new Resultado<>(recurso, false);
    }

    private void validarChave(String chave) {
        if (chave == null || !FORMATO_VALIDO.matcher(chave).matches()) {
            throw new RequisicaoInvalidaException("O cabeçalho " + HEADER + " deve ter de 8 a 100 caracteres "
                    + "(letras, números, '.', '_', ':' ou '-'). Sugestão: use um UUID.");
        }
    }
}
