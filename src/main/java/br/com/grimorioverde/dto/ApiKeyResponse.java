package br.com.grimorioverde.dto;

import br.com.grimorioverde.security.ApiKey;
import br.com.grimorioverde.security.NivelAcesso;

import java.time.Instant;

public record ApiKeyResponse(
        Long id,
        String proprietario,
        String prefixo,
        NivelAcesso nivel,
        boolean ativa,
        Instant criadaEm
) {
    public static ApiKeyResponse de(ApiKey k) {
        return new ApiKeyResponse(k.getId(), k.getProprietario(), k.getPrefixo(), k.getNivel(),
                k.isAtiva(), k.getCriadaEm());
    }
}
