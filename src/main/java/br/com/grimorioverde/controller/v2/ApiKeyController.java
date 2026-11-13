package br.com.grimorioverde.controller.v2;

import br.com.grimorioverde.assembler.ApiKeyAssembler;
import br.com.grimorioverde.dto.ApiKeyCriadaResponse;
import br.com.grimorioverde.dto.ApiKeyRequest;
import br.com.grimorioverde.dto.ApiKeyResponse;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.security.ApiKey;
import br.com.grimorioverde.security.ApiKeyService;
import br.com.grimorioverde.security.ExigeNivel;
import br.com.grimorioverde.security.NivelAcesso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/v2/api-keys")
@Tag(name = "Chaves de API (v2)",
        description = "Geração e gerenciamento de chaves de API. Todas as rotas exigem uma chave de nível ADMIN. "
                + "Chaves de nível USUARIO podem cadastrar, alterar e remover dados; só a ADMIN gerencia chaves.")
public class ApiKeyController {

    private final ApiKeyService service;
    private final ApiKeyAssembler assembler;
    private final PagedResourcesAssembler<ApiKey> pagedAssembler;

    public ApiKeyController(ApiKeyService service, ApiKeyAssembler assembler,
                            PagedResourcesAssembler<ApiKey> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.ADMIN)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Gera uma chave de API",
            description = "Exige chave de nível ADMIN. O valor da chave aparece **apenas nesta resposta**: "
                    + "guarde-o e entregue ao dono, que deve enviá-lo no cabeçalho `X-API-Key`. "
                    + "O campo `nivel` é opcional (padrão USUARIO).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Chave sem nível ADMIN",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ApiKeyCriadaResponse> gerar(@Valid @RequestBody ApiKeyRequest request) {
        NivelAcesso nivel = request.nivel() != null ? request.nivel() : NivelAcesso.USUARIO;
        ApiKeyService.ChaveCriada criada = service.criar(request.proprietario(), nivel);
        URI location = linkTo(ApiKeyController.class).slash(criada.apiKey().getId()).toUri();
        return ResponseEntity.created(location).body(ApiKeyCriadaResponse.de(criada.apiKey(), criada.chave()));
    }

    @GetMapping
    @ExigeNivel(NivelAcesso.ADMIN)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Lista as chaves de API (paginado)", description = "Exige chave de nível ADMIN. Não retorna o valor das chaves.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de chaves"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Chave sem nível ADMIN",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<ApiKeyResponse>> listar(@ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(pageable), assembler);
    }

    @GetMapping("/{id}")
    @ExigeNivel(NivelAcesso.ADMIN)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Busca uma chave de API pelo id", description = "Exige chave de nível ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chave encontrada"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Chave sem nível ADMIN",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ApiKeyResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.ADMIN)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Revoga uma chave de API", description = "Exige chave de nível ADMIN. A chave deixa de ser aceita imediatamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave revogada"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Chave sem nível ADMIN",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Chave não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> revogar(@PathVariable("id") Long id) {
        service.revogar(id);
        return ResponseEntity.noContent().build();
    }
}
