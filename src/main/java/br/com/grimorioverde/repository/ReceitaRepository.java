package br.com.grimorioverde.repository;

import br.com.grimorioverde.entity.Receita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    Page<Receita> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    Page<Receita> findByErvasId(
            Long ervaId,
            Pageable pageable
    );

    boolean existsByErvasId(Long ervaId);
}
