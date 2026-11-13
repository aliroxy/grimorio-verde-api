package br.com.grimorioverde.security;

import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Resultado da criação: a chave em texto puro só existe neste momento. */
    public record ChaveCriada(ApiKey apiKey, String chave) {
    }

    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ChaveCriada criar(String proprietario, NivelAcesso nivel) {
        String chave = gerarChave();
        return new ChaveCriada(salvar(proprietario, nivel, chave), chave);
    }

    /** Cadastra uma chave conhecida (usada para a chave ADMIN de desenvolvimento). */
    @Transactional
    public void garantirChave(String proprietario, NivelAcesso nivel, String chave) {
        if (!repository.existsByChaveHash(Hashes.sha256(chave))) {
            salvar(proprietario, nivel, chave);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ApiKey> autenticar(String chave) {
        return repository.findByChaveHashAndAtivaTrue(Hashes.sha256(chave.trim()));
    }

    @Transactional(readOnly = true)
    public Page<ApiKey> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ApiKey buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chave de API", id));
    }

    @Transactional
    public void revogar(Long id) {
        buscar(id).setAtiva(false);
    }

    private ApiKey salvar(String proprietario, NivelAcesso nivel, String chave) {
        ApiKey apiKey = new ApiKey();
        apiKey.setProprietario(proprietario.trim());
        apiKey.setNivel(nivel);
        apiKey.setChaveHash(Hashes.sha256(chave));
        apiKey.setPrefixo(chave.substring(0, Math.min(11, chave.length())));
        return repository.save(apiKey);
    }

    private String gerarChave() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return "gv_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
