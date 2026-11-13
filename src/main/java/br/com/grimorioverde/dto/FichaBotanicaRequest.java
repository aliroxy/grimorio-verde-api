package br.com.grimorioverde.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar ou atualizar a ficha botânica de uma erva")
public record FichaBotanicaRequest(

        @Schema(example = "1", description = "Id da erva dona da ficha (cada erva tem no máximo uma ficha)")
        @NotNull(message = "O id da erva é obrigatório")
        Long ervaId,

        @Schema(example = "Flores")
        @NotBlank(message = "A parte utilizada é obrigatória")
        @Size(min = 2, max = 100, message = "A parte utilizada deve ter entre 2 e 100 caracteres")
        String parteUtilizada,

        @Schema(example = "Sol pleno ou meia-sombra, solo bem drenado")
        @NotBlank(message = "A forma de cultivo é obrigatória")
        @Size(min = 5, max = 500, message = "A forma de cultivo deve ter entre 5 e 500 caracteres")
        String formaCultivo,

        @Schema(example = "Planta anual de até 60 cm, com folhas finas e flores perfumadas.")
        @NotBlank(message = "A descrição das características é obrigatória")
        @Size(min = 10, max = 1000, message = "As características devem ter entre 10 e 1000 caracteres")
        String caracteristicas,

        @Schema(example = "Pessoas alérgicas a plantas da família das Asteráceas devem evitar.")
        @Size(max = 500, message = "As advertências devem ter no máximo 500 caracteres")
        String advertencias
) {
}
