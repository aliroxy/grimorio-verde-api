package br.com.grimorioverde.service;

import br.com.grimorioverde.dto.RitualRequest;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Ritual;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.repository.ErvaRepository;
import br.com.grimorioverde.repository.RitualRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RitualService {

    private final RitualRepository ritualRepository;
    private final ErvaRepository ervaRepository;

    public RitualService(RitualRepository ritualRepository, ErvaRepository ervaRepository) {
        this.ritualRepository = ritualRepository;
        this.ervaRepository = ervaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Ritual> listar(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return ritualRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable);
        }
        return ritualRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Ritual> listarPorErva(Long ervaId, Pageable pageable) {
        if (!ervaRepository.existsById(ervaId)) {
            throw new RecursoNaoEncontradoException("Erva", ervaId);
        }
        return ritualRepository.findByErvasId(ervaId, pageable);
    }

    @Transactional(readOnly = true)
    public Ritual buscar(Long id) {
        return ritualRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ritual", id));
    }

    @Transactional
    public Ritual criar(RitualRequest request) {
        Ritual ritual = new Ritual();
        aplicar(ritual, request);
        return ritualRepository.save(ritual);
    }

    @Transactional
    public Ritual atualizar(Long id, RitualRequest request) {
        Ritual ritual = buscar(id);
        aplicar(ritual, request);
        return ritual;
    }

    @Transactional
    public void remover(Long id) {
        ritualRepository.delete(buscar(id));
    }

    private void aplicar(Ritual ritual, RitualRequest request) {
        ritual.setNome(request.nome().trim());
        ritual.setFinalidadeEspiritual(request.finalidadeEspiritual().trim());
        ritual.setInstrucoes(request.instrucoes().trim());
        ritual.setCuidados(request.cuidados() == null || request.cuidados().isBlank()
                ? null : request.cuidados().trim());
        ritual.setErvas(resolverErvas(request.ervaIds()));
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
