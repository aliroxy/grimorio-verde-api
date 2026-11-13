package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.CategoriaErva;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar ou atualizar uma erva")
public record ErvaRequest(

        @Schema(example = "Camomila")
        @NotBlank(message = "O nome popular é obrigatório")
        @Size(min = 2, max = 100, message = "O nome popular deve ter entre 2 e 100 caracteres")
        String nomePopular,

        @Schema(example = "Matricaria chamomilla")
        @NotBlank(message = "O nome científico é obrigatório")
        @Size(min = 2, max = 150, message = "O nome científico deve ter entre 2 e 150 caracteres")
        String nomeCientifico,

        @Schema(example = "Erva aromática de flores brancas com centro amarelo, muito usada em chás.")
        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 10, max = 1000, message = "A descrição deve ter entre 10 e 1000 caracteres")
        String descricao,

        @Schema(example = "MEDICINAL")
        @NotNull(message = "A categoria é obrigatória")
        CategoriaErva categoria,

        @Schema(example = "Europa")
        @Size(max = 100, message = "A origem deve ter no máximo 100 caracteres")
        String origem
) {
}
