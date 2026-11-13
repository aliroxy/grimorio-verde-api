package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.TipoUso;
import br.com.grimorioverde.entity.Uso;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsoResponse(
        Long id,
        TipoUso tipo,
        String descricao,
        String modoPreparo,
        String contraindicacoes,
        String aviso,
        Long ervaId,
        String ervaNome
) {
    public static UsoResponse de(Uso u) {
        String aviso = u.getTipo() == TipoUso.MEDICINAL ? Avisos.MEDICINAL : null;
        return new UsoResponse(u.getId(), u.getTipo(), u.getDescricao(), u.getModoPreparo(),
                u.getContraindicacoes(), aviso, u.getErva().getId(), u.getErva().getNomePopular());
    }
}
