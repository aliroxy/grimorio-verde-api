package br.com.grimorioverde.controller.v1;

import br.com.grimorioverde.assembler.UsoAssembler;
import br.com.grimorioverde.dto.UsoRequest;
import br.com.grimorioverde.dto.UsoResponse;
import br.com.grimorioverde.entity.TipoUso;
import br.com.grimorioverde.entity.Uso;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.service.UsoService;
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
@RequestMapping("/api/v1/usos")
@Tag(name = "Usos (v1)", description = "Formas de uso das ervas: medicinal, culinário e espiritual (N:1 com Erva). "
        + "Usos medicinais trazem aviso de que não substituem orientação profissional.")
public class UsoController {

    private final IdempotencyService idempotencyService;
    private final UsoService service;
    private final UsoAssembler assembler;
    private final PagedResourcesAssembler<Uso> pagedAssembler;

    public UsoController(UsoService service, UsoAssembler assembler, PagedResourcesAssembler<Uso> pagedAssembler, IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista usos (paginado)", description = "Use `tipo` para filtrar por MEDICINAL, CULINARIO ou ESPIRITUAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usos"),
            @ApiResponse(responseCode = "400", description = "Tipo inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<UsoResponse>> listar(
            @Parameter(description = "Tipo de uso", example = "CULINARIO")
            @RequestParam(name = "tipo", required = false) TipoUso tipo,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(tipo, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um uso pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uso encontrado"),
            @ApiResponse(responseCode = "404", description = "Uso não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<UsoResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/erva/{ervaId}")
    @Operation(summary = "Lista os usos de uma erva (paginado)", description = "Consulta personalizada por erva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usos da erva"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<UsoResponse>> listarPorErva(@PathVariable("ervaId") Long ervaId,
                                                             @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listarPorErva(ervaId, pageable), assembler);
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Cadastra um uso para uma erva (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave e o mesmo corpo não cria outro registro: devolve o já criado, com `Idempotency-Replayed: true`. Mesma chave com corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Uso criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<UsoResponse>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody UsoRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<Uso> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v1/usos " + request,
                () -> service.criar(request), Uso::getId, service::buscar);

        EntityModel<UsoResponse> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Atualiza um uso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Uso atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Uso ou erva não encontrados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<UsoResponse> atualizar(@PathVariable("id") Long id, @Valid @RequestBody UsoRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Remove um uso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Uso removido"),
            @ApiResponse(responseCode = "404", description = "Uso não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
