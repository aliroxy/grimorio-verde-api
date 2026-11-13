package br.com.grimorioverde.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByChaveHashAndAtivaTrue(String chaveHash);

    boolean existsByChaveHash(String chaveHash);
}
