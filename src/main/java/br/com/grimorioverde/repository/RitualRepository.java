package br.com.grimorioverde.repository;

import br.com.grimorioverde.entity.Ritual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RitualRepository extends JpaRepository<Ritual, Long> {

    Page<Ritual> findByNomeContainingIgnoreCase(
            String nome,
            Pageable pageable
    );

    Page<Ritual> findByErvasId(
            Long ervaId,
            Pageable pageable
    );

    boolean existsByErvasId(Long ervaId);
}
