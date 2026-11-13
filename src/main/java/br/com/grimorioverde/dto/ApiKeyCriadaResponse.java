package br.com.grimorioverde.dto;

import br.com.grimorioverde.security.ApiKey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Chave recém-criada. O valor de `chave` é exibido somente nesta resposta.")
public record ApiKeyCriadaResponse(
        Long id,
        String proprietario,
        String nivel,
        @Schema(example = "gv_Zk3p...") String chave,
        String aviso
) {
    public static ApiKeyCriadaResponse de(ApiKey k, String chave) {
        return new ApiKeyCriadaResponse(k.getId(), k.getProprietario(), k.getNivel().name(), chave,
                "Guarde esta chave agora: ela não será mostrada novamente. Envie-a no cabeçalho X-API-Key.");
    }
}
