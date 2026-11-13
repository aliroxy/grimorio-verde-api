package br.com.grimorioverde.service;

import br.com.grimorioverde.dto.FichaBotanicaRequest;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.FichaBotanica;
import br.com.grimorioverde.exception.ConflitoException;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.repository.ErvaRepository;
import br.com.grimorioverde.repository.FichaBotanicaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FichaBotanicaService {

    private final FichaBotanicaRepository fichaRepository;
    private final ErvaRepository ervaRepository;

    public FichaBotanicaService(FichaBotanicaRepository fichaRepository, ErvaRepository ervaRepository) {
        this.fichaRepository = fichaRepository;
        this.ervaRepository = ervaRepository;
    }

    @Transactional(readOnly = true)
    public Page<FichaBotanica> listar(String parteUtilizada, Pageable pageable) {
        if (parteUtilizada != null && !parteUtilizada.isBlank()) {
            return fichaRepository.findByParteUtilizadaContainingIgnoreCase(parteUtilizada.trim(), pageable);
        }
        return fichaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public FichaBotanica buscar(Long id) {
        return fichaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ficha botânica", id));
    }

    @Transactional(readOnly = true)
    public FichaBotanica buscarPorErva(Long ervaId) {
        return fichaRepository.findByErvaId(ervaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ficha botânica não encontrada para a erva com id " + ervaId));
    }

    @Transactional
    public FichaBotanica criar(FichaBotanicaRequest request) {
        Erva erva = ervaRepository.findById(request.ervaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erva", request.ervaId()));
        if (fichaRepository.existsByErvaId(erva.getId())) {
            throw new ConflitoException("A erva '" + erva.getNomePopular() + "' já possui uma ficha botânica.");
        }
        FichaBotanica ficha = new FichaBotanica();
        aplicar(ficha, request);
        ficha.setErva(erva);
        erva.setFichaBotanica(ficha);
        return fichaRepository.save(ficha);
    }

    @Transactional
    public FichaBotanica atualizar(Long id, FichaBotanicaRequest request) {
        FichaBotanica ficha = buscar(id);
        if (!ficha.getErva().getId().equals(request.ervaId())) {
            throw new ConflitoException("A ficha botânica pertence à erva '" + ficha.getErva().getNomePopular()
                    + "' (id " + ficha.getErva().getId() + ") e não pode ser movida para outra erva.");
        }
        aplicar(ficha, request);
        return ficha;
    }

    @Transactional
    public void remover(Long id) {
        FichaBotanica ficha = buscar(id);
        Erva erva = ficha.getErva();
        // desfaz o lado inverso para o cascade da Erva não "ressuscitar" a ficha removida
        erva.setFichaBotanica(null);
        fichaRepository.delete(ficha);
    }

    private void aplicar(FichaBotanica ficha, FichaBotanicaRequest request) {
        ficha.setParteUtilizada(request.parteUtilizada().trim());
        ficha.setFormaCultivo(request.formaCultivo().trim());
        ficha.setCaracteristicas(request.caracteristicas().trim());
        ficha.setAdvertencias(request.advertencias() == null || request.advertencias().isBlank()
                ? null : request.advertencias().trim());
    }
}
