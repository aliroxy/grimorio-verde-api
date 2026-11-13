package br.com.grimorioverde.repository;

import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErvaRepository extends JpaRepository<Erva, Long> {

    Page<Erva> findByNomePopularContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    Page<Erva> findByNomeCientificoContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    Page<Erva> findByNomePopularContainingIgnoreCaseAndCategoria(
            String nome,
            CategoriaErva categoria,
            Pageable pageable
    );

    Page<Erva> findByCategoria(
            CategoriaErva categoria,
            Pageable pageable
    );

    boolean existsByNomeCientificoIgnoreCase(String nomeCientifico);

    boolean existsByNomeCientificoIgnoreCaseAndIdNot(String nomeCientifico, Long id);
}
