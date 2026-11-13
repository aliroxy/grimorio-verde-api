package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação da erva na v2: inclui o resumo de usos e da ficha botânica")
public record ErvaResponseV2(
        Long id,
        String nomePopular,
        String nomeCientifico,
        String descricao,
        CategoriaErva categoria,
        String origem,
        int totalUsos,
        boolean possuiFichaBotanica,
        String aviso
) {
    public static ErvaResponseV2 de(Erva e) {
        return new ErvaResponseV2(e.getId(), e.getNomePopular(), e.getNomeCientifico(),
                e.getDescricao(), e.getCategoria(), e.getOrigem(),
                e.getUsos().size(), e.getFichaBotanica() != null, Avisos.MEDICINAL);
    }
}
