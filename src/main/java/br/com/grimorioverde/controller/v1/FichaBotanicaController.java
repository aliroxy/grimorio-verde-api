package br.com.grimorioverde.controller.v1;

import br.com.grimorioverde.assembler.FichaBotanicaAssembler;
import br.com.grimorioverde.dto.FichaBotanicaRequest;
import br.com.grimorioverde.dto.FichaBotanicaResponse;
import br.com.grimorioverde.entity.FichaBotanica;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.service.FichaBotanicaService;
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
@RequestMapping("/api/v1/fichas-botanicas")
@Tag(name = "Fichas botânicas (v1)", description = "Informações botânicas de cada erva (relação 1:1 com Erva).")
public class FichaBotanicaController {

    private final IdempotencyService idempotencyService;
    private final FichaBotanicaService service;
    private final FichaBotanicaAssembler assembler;
    private final PagedResourcesAssembler<FichaBotanica> pagedAssembler;

    public FichaBotanicaController(FichaBotanicaService service, FichaBotanicaAssembler assembler,
                                   PagedResourcesAssembler<FichaBotanica> pagedAssembler, IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista fichas botânicas (paginado)",
            description = "Use `parte` para filtrar pela parte utilizada da planta.")
    @ApiResponse(responseCode = "200", description = "Página de fichas botânicas")
    public PagedModel<EntityModel<FichaBotanicaResponse>> listar(
            @Parameter(description = "Trecho da parte utilizada", example = "folhas")
            @RequestParam(name = "parte", required = false) String parte,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(parte, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma ficha botânica pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha encontrada"),
            @ApiResponse(responseCode = "404", description = "Ficha não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<FichaBotanicaResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/erva/{ervaId}")
    @Operation(summary = "Busca a ficha botânica de uma erva", description = "Consulta personalizada: retorna a ficha ligada à erva informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha encontrada"),
            @ApiResponse(responseCode = "404", description = "A erva não possui ficha botânica",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<FichaBotanicaResponse> buscarPorErva(@PathVariable("ervaId") Long ervaId) {
        return assembler.toModel(service.buscarPorErva(ervaId));
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Cadastra a ficha botânica de uma erva (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave e o mesmo corpo não cria outro registro: devolve o já criado, com `Idempotency-Replayed: true`. Mesma chave com corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ficha criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "A erva já possui ficha botânica",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<FichaBotanicaResponse>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody FichaBotanicaRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<FichaBotanica> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v1/fichas-botanicas " + request,
                () -> service.criar(request), FichaBotanica::getId, service::buscar);

        EntityModel<FichaBotanicaResponse> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Atualiza uma ficha botânica", description = "O `ervaId` deve continuar sendo o da erva dona da ficha.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Ficha não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Tentativa de mover a ficha para outra erva",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<FichaBotanicaResponse> atualizar(@PathVariable("id") Long id,
                                                        @Valid @RequestBody FichaBotanicaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Remove uma ficha botânica")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ficha removida"),
            @ApiResponse(responseCode = "404", description = "Ficha não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
