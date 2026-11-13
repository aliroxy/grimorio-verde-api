package br.com.grimorioverde.service;

import br.com.grimorioverde.dto.UsoRequest;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.TipoUso;
import br.com.grimorioverde.entity.Uso;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.repository.ErvaRepository;
import br.com.grimorioverde.repository.UsoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsoService {

    private final UsoRepository usoRepository;
    private final ErvaRepository ervaRepository;

    public UsoService(UsoRepository usoRepository, ErvaRepository ervaRepository) {
        this.usoRepository = usoRepository;
        this.ervaRepository = ervaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Uso> listar(TipoUso tipo, Pageable pageable) {
        return tipo != null ? usoRepository.findByTipo(tipo, pageable) : usoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Uso> listarPorErva(Long ervaId, Pageable pageable) {
        if (!ervaRepository.existsById(ervaId)) {
            throw new RecursoNaoEncontradoException("Erva", ervaId);
        }
        return usoRepository.findByErvaId(ervaId, pageable);
    }

    @Transactional(readOnly = true)
    public Uso buscar(Long id) {
        return usoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Uso", id));
    }

    @Transactional
    public Uso criar(UsoRequest request) {
        Erva erva = buscarErva(request.ervaId());
        Uso uso = new Uso();
        aplicar(uso, request);
        uso.setErva(erva);
        return usoRepository.save(uso);
    }

    @Transactional
    public Uso atualizar(Long id, UsoRequest request) {
        Uso uso = buscar(id);
        aplicar(uso, request);
        if (!uso.getErva().getId().equals(request.ervaId())) {
            uso.setErva(buscarErva(request.ervaId()));
        }
        return uso;
    }

    @Transactional
    public void remover(Long id) {
        Uso uso = buscar(id);
        uso.getErva().getUsos().remove(uso);
        usoRepository.delete(uso);
    }

    private Erva buscarErva(Long ervaId) {
        return ervaRepository.findById(ervaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erva", ervaId));
    }

    private void aplicar(Uso uso, UsoRequest request) {
        uso.setTipo(request.tipo());
        uso.setDescricao(request.descricao().trim());
        uso.setModoPreparo(vazioParaNulo(request.modoPreparo()));
        uso.setContraindicacoes(vazioParaNulo(request.contraindicacoes()));
    }

    private String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
