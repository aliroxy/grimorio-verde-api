package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;

public record ErvaResponse(
        Long id,
        String nomePopular,
        String nomeCientifico,
        String descricao,
        CategoriaErva categoria,
        String origem,
        String aviso
) {
    public static ErvaResponse de(Erva e) {
        return new ErvaResponse(e.getId(), e.getNomePopular(), e.getNomeCientifico(),
                e.getDescricao(), e.getCategoria(), e.getOrigem(), Avisos.MEDICINAL);
    }
}
