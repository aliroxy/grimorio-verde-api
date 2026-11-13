package br.com.grimorioverde.service;

import br.com.grimorioverde.dto.ReceitaRequest;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Receita;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.repository.ErvaRepository;
import br.com.grimorioverde.repository.ReceitaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReceitaService {

    private final ReceitaRepository receitaRepository;
    private final ErvaRepository ervaRepository;

    public ReceitaService(ReceitaRepository receitaRepository, ErvaRepository ervaRepository) {
        this.receitaRepository = receitaRepository;
        this.ervaRepository = ervaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Receita> listar(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return receitaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        }
        return receitaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Receita> listarPorErva(Long ervaId, Pageable pageable) {
        if (!ervaRepository.existsById(ervaId)) {
            throw new RecursoNaoEncontradoException("Erva", ervaId);
        }
        return receitaRepository.findByErvasId(ervaId, pageable);
    }

    @Transactional(readOnly = true)
    public Receita buscar(Long id) {
        return receitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Receita", id));
    }

    @Transactional
    public Receita criar(ReceitaRequest request) {
        Receita receita = new Receita();
        aplicar(receita, request);
        return receitaRepository.save(receita);
    }

    @Transactional
    public Receita atualizar(Long id, ReceitaRequest request) {
        Receita receita = buscar(id);
        aplicar(receita, request);
        return receita;
    }

    @Transactional
    public void remover(Long id) {
        receitaRepository.delete(buscar(id));
    }

    private void aplicar(Receita receita, ReceitaRequest request) {
        receita.setNome(request.nome().trim());
        receita.setIngredientes(request.ingredientes().trim());
        receita.setModoPreparo(request.modoPreparo().trim());
        receita.setErvas(resolverErvas(request.ervaIds()));
    }

    private Set<Erva> resolverErvas(Set<Long> ids) {
        List<Erva> encontradas = ervaRepository.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            Set<Long> achados = encontradas.stream().map(Erva::getId).collect(Collectors.toSet());
            String faltando = ids.stream().filter(i -> !achados.contains(i)).sorted()
                    .map(String::valueOf).collect(Collectors.joining(", "));
            throw new RecursoNaoEncontradoException("Erva(s) não encontrada(s) com id: " + faltando);
        }
        return new HashSet<>(encontradas);
    }
}
