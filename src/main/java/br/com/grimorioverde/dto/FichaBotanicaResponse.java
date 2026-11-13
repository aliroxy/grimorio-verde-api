package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.FichaBotanica;

public record FichaBotanicaResponse(
        Long id,
        String parteUtilizada,
        String formaCultivo,
        String caracteristicas,
        String advertencias,
        Long ervaId,
        String ervaNome
) {
    public static FichaBotanicaResponse de(FichaBotanica f) {
        return new FichaBotanicaResponse(f.getId(), f.getParteUtilizada(), f.getFormaCultivo(),
                f.getCaracteristicas(), f.getAdvertencias(),
                f.getErva().getId(), f.getErva().getNomePopular());
    }
}
