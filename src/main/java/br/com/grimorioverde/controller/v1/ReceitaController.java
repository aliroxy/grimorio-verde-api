package br.com.grimorioverde.controller.v1;

import br.com.grimorioverde.assembler.ReceitaAssembler;
import br.com.grimorioverde.dto.ReceitaRequest;
import br.com.grimorioverde.dto.ReceitaResponse;
import br.com.grimorioverde.entity.Receita;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.service.ReceitaService;
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
@RequestMapping("/api/v1/receitas")
@Tag(name = "Receitas (v1)", description = "Receitas culinárias e preparos com ervas (N:N com Erva).")
public class ReceitaController {

    private final IdempotencyService idempotencyService;
    private final ReceitaService service;
    private final ReceitaAssembler assembler;
    private final PagedResourcesAssembler<Receita> pagedAssembler;

    public ReceitaController(ReceitaService service, ReceitaAssembler assembler,
                             PagedResourcesAssembler<Receita> pagedAssembler, IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista receitas (paginado)", description = "Use `nome` para filtrar pelo nome da receita.")
    @ApiResponse(responseCode = "200", description = "Página de receitas")
    public PagedModel<EntityModel<ReceitaResponse>> listar(
            @Parameter(description = "Trecho do nome da receita", example = "chá")
            @RequestParam(name = "nome", required = false) String nome,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(nome, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma receita pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita encontrada"),
            @ApiResponse(responseCode = "404", description = "Receita não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ReceitaResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/erva/{ervaId}")
    @Operation(summary = "Lista as receitas que usam uma erva (paginado)", description = "Consulta personalizada por erva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de receitas"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<ReceitaResponse>> listarPorErva(@PathVariable("ervaId") Long ervaId,
                                                                 @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listarPorErva(ervaId, pageable), assembler);
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Cadastra uma receita (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave e o mesmo corpo não cria outro registro: devolve o já criado, com `Idempotency-Replayed: true`. Mesma chave com corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Receita criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Alguma erva informada não existe",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<ReceitaResponse>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody ReceitaRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<Receita> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v1/receitas " + request,
                () -> service.criar(request), Receita::getId, service::buscar);

        EntityModel<ReceitaResponse> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Atualiza uma receita")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Receita atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Receita ou erva não encontradas",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ReceitaResponse> atualizar(@PathVariable("id") Long id, @Valid @RequestBody ReceitaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Remove uma receita")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Receita removida"),
            @ApiResponse(responseCode = "404", description = "Receita não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
