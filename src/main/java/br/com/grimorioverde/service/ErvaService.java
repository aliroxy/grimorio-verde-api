package br.com.grimorioverde.service;

import br.com.grimorioverde.dto.ErvaRequest;
import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.exception.ConflitoException;
import br.com.grimorioverde.exception.RecursoNaoEncontradoException;
import br.com.grimorioverde.repository.ErvaRepository;
import br.com.grimorioverde.repository.ReceitaRepository;
import br.com.grimorioverde.repository.RitualRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ErvaService {

    private final ErvaRepository ervaRepository;
    private final ReceitaRepository receitaRepository;
    private final RitualRepository ritualRepository;

    public ErvaService(ErvaRepository ervaRepository,
                       ReceitaRepository receitaRepository,
                       RitualRepository ritualRepository) {
        this.ervaRepository = ervaRepository;
        this.receitaRepository = receitaRepository;
        this.ritualRepository = ritualRepository;
    }

    @Transactional(readOnly = true)
    public Page<Erva> listar(String nome, CategoriaErva categoria, Pageable pageable) {
        boolean temNome = nome != null && !nome.isBlank();
        if (temNome && categoria != null) {
            return ervaRepository.findByNomePopularContainingIgnoreCaseAndCategoria(nome.trim(), categoria, pageable);
        }
        if (temNome) {
            return ervaRepository.findByNomePopularContainingIgnoreCase(nome.trim(), pageable);
        }
        if (categoria != null) {
            return ervaRepository.findByCategoria(categoria, pageable);
        }
        return ervaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Erva> buscarPorNomeCientifico(String nome, Pageable pageable) {
        return ervaRepository.findByNomeCientificoContainingIgnoreCase(nome.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Erva buscar(Long id) {
        return ervaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Erva", id));
    }

    @Transactional
    public Erva criar(ErvaRequest request) {
        String nomeCientifico = request.nomeCientifico().trim();
        if (ervaRepository.existsByNomeCientificoIgnoreCase(nomeCientifico)) {
            throw new ConflitoException("Já existe uma erva cadastrada com o nome científico '" + nomeCientifico + "'.");
        }
        Erva erva = new Erva();
        aplicar(erva, request);
        return ervaRepository.save(erva);
    }

    @Transactional
    public Erva atualizar(Long id, ErvaRequest request) {
        Erva erva = buscar(id);
        String nomeCientifico = request.nomeCientifico().trim();
        if (ervaRepository.existsByNomeCientificoIgnoreCaseAndIdNot(nomeCientifico, id)) {
            throw new ConflitoException("Já existe outra erva cadastrada com o nome científico '" + nomeCientifico + "'.");
        }
        aplicar(erva, request);
        return erva;
    }

    @Transactional
    public void remover(Long id) {
        Erva erva = buscar(id);
        if (receitaRepository.existsByErvasId(id)) {
            throw new ConflitoException("Não é possível excluir: a erva é usada em uma ou mais receitas.");
        }
        if (ritualRepository.existsByErvasId(id)) {
            throw new ConflitoException("Não é possível excluir: a erva é usada em um ou mais rituais.");
        }
        ervaRepository.delete(erva);
    }

    private void aplicar(Erva erva, ErvaRequest request) {
        erva.setNomePopular(request.nomePopular().trim());
        erva.setNomeCientifico(request.nomeCientifico().trim());
        erva.setDescricao(request.descricao().trim());
        erva.setCategoria(request.categoria());
        erva.setOrigem(request.origem() == null || request.origem().isBlank() ? null : request.origem().trim());
    }
}
