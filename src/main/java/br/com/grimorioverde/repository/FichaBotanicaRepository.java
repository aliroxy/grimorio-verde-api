package br.com.grimorioverde.repository;

import br.com.grimorioverde.entity.FichaBotanica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FichaBotanicaRepository
        extends JpaRepository<FichaBotanica, Long> {

    Page<FichaBotanica> findByParteUtilizadaContainingIgnoreCase(
            String parte,
            Pageable pageable
    );

    Optional<FichaBotanica> findByErvaId(Long ervaId);

    boolean existsByErvaId(Long ervaId);
}
