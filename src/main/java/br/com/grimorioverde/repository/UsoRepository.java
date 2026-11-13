package br.com.grimorioverde.repository;

import br.com.grimorioverde.entity.TipoUso;
import br.com.grimorioverde.entity.Uso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsoRepository extends JpaRepository<Uso, Long> {

    Page<Uso> findByTipo(
            TipoUso tipo,
            Pageable pageable
    );

    Page<Uso> findByErvaId(
            Long ervaId,
            Pageable pageable
    );
}
