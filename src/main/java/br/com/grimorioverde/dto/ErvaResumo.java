package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.Erva;

public record ErvaResumo(Long id, String nomePopular, String nomeCientifico) {

    public static ErvaResumo de(Erva e) {
        return new ErvaResumo(e.getId(), e.getNomePopular(), e.getNomeCientifico());
    }
}
