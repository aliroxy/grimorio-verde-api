package br.com.grimorioverde.dto;

import br.com.grimorioverde.security.NivelAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para gerar uma chave de API")
public record ApiKeyRequest(

        @Schema(example = "Maria da Silva", description = "Nome da pessoa ou sistema dono da chave")
        @NotBlank(message = "O proprietário é obrigatório")
        @Size(min = 2, max = 100, message = "O proprietário deve ter entre 2 e 100 caracteres")
        String proprietario,

        @Schema(example = "USUARIO", defaultValue = "USUARIO",
                description = "Nível de acesso da chave. Se omitido, será USUARIO.")
        NivelAcesso nivel
) {
}
