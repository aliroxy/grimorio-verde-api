package br.com.grimorioverde.controller.v2;

import br.com.grimorioverde.assembler.ErvaAssemblerV2;
import br.com.grimorioverde.dto.ErvaRequest;
import br.com.grimorioverde.dto.ErvaResponseV2;
import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.security.ApiKey;
import br.com.grimorioverde.security.ApiKeyInterceptor;
import br.com.grimorioverde.security.ExigeNivel;
import br.com.grimorioverde.security.IdempotencyService;
import br.com.grimorioverde.security.NivelAcesso;
import br.com.grimorioverde.service.ErvaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v2/ervas")
@Tag(name = "Ervas (v2)",
        description = "Evolução do recurso Erva. Em relação à v1: resposta com `totalUsos` e `possuiFichaBotanica`, "
                + "escrita protegida por chave de API (`X-API-Key`) e POST idempotente (`X-Idempotency-Key`).")
public class ErvaControllerV2 {

    private final ErvaService service;
    private final ErvaAssemblerV2 assembler;
    private final PagedResourcesAssembler<Erva> pagedAssembler;
    private final IdempotencyService idempotencyService;

    public ErvaControllerV2(ErvaService service, ErvaAssemblerV2 assembler,
                            PagedResourcesAssembler<Erva> pagedAssembler, IdempotencyService idempotencyService) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
        this.idempotencyService = idempotencyService;
    }

    @GetMapping
    @Operation(summary = "Lista ervas (paginado)",
            description = "Leitura pública. Filtros opcionais: `nome` (nome popular) e `categoria`, que podem ser combinados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ervas"),
            @ApiResponse(responseCode = "400", description = "Categoria inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (veja o cabeçalho Retry-After)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<ErvaResponseV2>> listar(
            @Parameter(description = "Trecho do nome popular", example = "alecrim")
            @RequestParam(name = "nome", required = false) String nome,
            @Parameter(description = "Categoria da erva", example = "CULINARIA")
            @RequestParam(name = "categoria", required = false) CategoriaErva categoria,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(nome, categoria, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma erva pelo id",
            description = "Versão 2 do endpoint `GET /api/v1/ervas/{id}`: a resposta inclui `totalUsos` e `possuiFichaBotanica`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erva encontrada"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ErvaResponseV2> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Lista ervas por categoria (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ervas da categoria"),
            @ApiResponse(responseCode = "400", description = "Categoria inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<ErvaResponseV2>> porCategoria(
            @PathVariable("categoria") CategoriaErva categoria,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(null, categoria, pageable), assembler);
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Cadastra uma erva (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave de idempotência "
                    + "e o mesmo corpo não cria outra erva: devolve a já criada, com o cabeçalho `Idempotency-Replayed: true`. "
                    + "Usar a mesma chave com um corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Erva criada (ou devolvida, se for repetição)",
                    headers = @Header(name = "Idempotency-Replayed", description = "true quando a resposta veio de uma requisição anterior com a mesma chave")),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou cabeçalho X-Idempotency-Key ausente/inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome científico já cadastrado ou requisição idêntica em andamento",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<ErvaResponseV2>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody ErvaRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<Erva> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v2/ervas " + request,
                () -> service.criar(request), Erva::getId, service::buscar);

        EntityModel<ErvaResponseV2> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Atualiza uma erva", description = "Exige `X-API-Key`. Substitui todos os dados da erva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erva atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome científico já usado por outra erva",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ErvaResponseV2> atualizar(@PathVariable("id") Long id, @Valid @RequestBody ErvaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @SecurityRequirement(name = "ApiKeyAuth")
    @Operation(summary = "Remove uma erva", description = "Exige `X-API-Key`.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Erva removida"),
            @ApiResponse(responseCode = "401", description = "Chave de API ausente, inválida ou revogada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Erva em uso por receitas ou rituais",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }
}
