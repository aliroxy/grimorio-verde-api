package br.com.grimorioverde.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Dados para cadastrar ou atualizar uma receita")
public record ReceitaRequest(

        @Schema(example = "Chá de camomila com hortelã")
        @NotBlank(message = "O nome da receita é obrigatório")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
        String nome,

        @Schema(example = "1 colher de sopa de camomila seca; 4 folhas de hortelã; 200 ml de água")
        @NotBlank(message = "Os ingredientes são obrigatórios")
        @Size(min = 5, max = 2000, message = "Os ingredientes devem ter entre 5 e 2000 caracteres")
        String ingredientes,

        @Schema(example = "Despeje a água quente sobre as ervas, tampe e aguarde 5 minutos. Coe e sirva.")
        @NotBlank(message = "O modo de preparo é obrigatório")
        @Size(min = 10, max = 2000, message = "O modo de preparo deve ter entre 10 e 2000 caracteres")
        String modoPreparo,

        @Schema(example = "[1, 2]", description = "Ids das ervas usadas na receita")
        @NotEmpty(message = "Informe ao menos uma erva")
        Set<@NotNull(message = "Id de erva inválido") Long> ervaIds
) {
}
