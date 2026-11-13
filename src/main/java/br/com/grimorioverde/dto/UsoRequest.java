package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.TipoUso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar ou atualizar uma forma de uso de uma erva")
public record UsoRequest(

        @Schema(example = "1")
        @NotNull(message = "O id da erva é obrigatório")
        Long ervaId,

        @Schema(example = "CULINARIO")
        @NotNull(message = "O tipo de uso é obrigatório")
        TipoUso tipo,

        @Schema(example = "Tradicionalmente consumida em infusão para relaxamento.")
        @NotBlank(message = "A descrição do uso é obrigatória")
        @Size(min = 10, max = 1000, message = "A descrição deve ter entre 10 e 1000 caracteres")
        String descricao,

        @Schema(example = "Infusão de 1 colher de sopa de flores secas em 200 ml de água quente por 5 minutos.")
        @Size(max = 500, message = "O modo de preparo deve ter no máximo 500 caracteres")
        String modoPreparo,

        @Schema(example = "Evitar em caso de alergia; gestantes devem consultar um profissional de saúde.")
        @Size(max = 1000, message = "As contraindicações devem ter no máximo 1000 caracteres")
        String contraindicacoes
) {
}
