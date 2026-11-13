package br.com.grimorioverde.controller.v1;

import br.com.grimorioverde.assembler.RitualAssembler;
import br.com.grimorioverde.dto.RitualRequest;
import br.com.grimorioverde.dto.RitualResponse;
import br.com.grimorioverde.entity.Ritual;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.service.RitualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.grimorioverde.security.ExigeNivel;
import br.com.grimorioverde.security.NivelAcesso;
import br.com.grimorioverde.security.ApiKeyInterceptor;
import br.com.grimorioverde.security.IdempotencyService;
import br.com.grimorioverde.security.ApiKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rituais")
@Tag(name = "Rituais (v1)", description = "Rituais e práticas espirituais com ervas, de caráter cultural e simbólico (N:N com Erva).")
public class RitualController {

    private final IdempotencyService idempotencyService;
    private final RitualService service;
    private final RitualAssembler assembler;
    private final PagedResourcesAssembler<Ritual> pagedAssembler;

    public RitualController(RitualService service, RitualAssembler assembler,
                            PagedResourcesAssembler<Ritual> pagedAssembler, IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista rituais (paginado)", description = "Use `nome` para filtrar pelo nome do ritual.")
    @ApiResponse(responseCode = "200", description = "Página de rituais")
    public PagedModel<EntityModel<RitualResponse>> listar(
            @Parameter(description = "Trecho do nome do ritual", example = "defumação")
            @RequestParam(name = "nome", required = false) String nome,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um ritual pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ritual encontrado"),
            @ApiResponse(responseCode = "404", description = "Ritual não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<RitualResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/erva/{ervaId}")
    @Operation(summary = "Lista os rituais que usam uma erva (paginado)", description = "Consulta personalizada por erva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de rituais"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<RitualResponse>> listarPorErva(@PathVariable("ervaId") Long ervaId,
                                                                @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listarPorErva(ervaId, pageable), assembler);
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Cadastra um ritual (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave e o mesmo corpo não cria outro registro: devolve o já criado, com `Idempotency-Replayed: true`. Mesma chave com corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ritual criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Alguma erva informada não existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<RitualResponse>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody RitualRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<Ritual> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v1/rituais " + request,
                () -> service.criar(request), Ritual::getId, service::buscar);

        EntityModel<RitualResponse> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Atualiza um ritual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ritual atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Ritual ou erva não encontrados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<RitualResponse> atualizar(@PathVariable("id") Long id, @Valid @RequestBody RitualRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Remove um ritual")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ritual removido"),
            @ApiResponse(responseCode = "404", description = "Ritual não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
