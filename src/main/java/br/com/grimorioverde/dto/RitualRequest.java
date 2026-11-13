package br.com.grimorioverde.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Dados para cadastrar ou atualizar um ritual")
public record RitualRequest(

        @Schema(example = "Defumação de alecrim")
        @NotBlank(message = "O nome do ritual é obrigatório")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres")
        String nome,

        @Schema(example = "Simbolizar a limpeza do ambiente e a renovação das energias da casa.")
        @NotBlank(message = "A finalidade espiritual é obrigatória")
        @Size(min = 10, max = 1000, message = "A finalidade deve ter entre 10 e 1000 caracteres")
        String finalidadeEspiritual,

        @Schema(example = "Queime um galho seco de alecrim em recipiente à prova de fogo, com a janela aberta.")
        @NotBlank(message = "As instruções são obrigatórias")
        @Size(min = 10, max = 2000, message = "As instruções devem ter entre 10 e 2000 caracteres")
        String instrucoes,

        @Schema(example = "Mantenha longe de crianças e de materiais inflamáveis.")
        @Size(max = 1000, message = "Os cuidados devem ter no máximo 1000 caracteres")
        String cuidados,

        @Schema(example = "[3]", description = "Ids das ervas usadas no ritual")
        @NotEmpty(message = "Informe ao menos uma erva")
        Set<@NotNull(message = "Id de erva inválido") Long> ervaIds
) {
}
