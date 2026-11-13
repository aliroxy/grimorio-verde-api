package br.com.grimorioverde.controller.v1;

import br.com.grimorioverde.assembler.ErvaAssembler;
import br.com.grimorioverde.dto.ErvaRequest;
import br.com.grimorioverde.dto.ErvaResponse;
import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.service.ErvaService;
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
@RequestMapping("/api/v1/ervas")
@Tag(name = "Ervas (v1)", description = "Cadastro de ervas. As informações são educativas e não substituem orientação de profissionais de saúde.")
public class ErvaController {

    private final IdempotencyService idempotencyService;
    private final ErvaService service;
    private final ErvaAssembler assembler;
    private final PagedResourcesAssembler<Erva> pagedAssembler;

    public ErvaController(ErvaService service, ErvaAssembler assembler, PagedResourcesAssembler<Erva> pagedAssembler, IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista ervas (paginado)",
            description = "Retorna as ervas com paginação e ordenação (`page`, `size`, `sort`). "
                    + "Use `nome` para filtrar pelo nome popular (busca parcial, sem diferenciar maiúsculas).")
    @ApiResponse(responseCode = "200", description = "Página de ervas")
    public PagedModel<EntityModel<ErvaResponse>> listar(
            @Parameter(description = "Trecho do nome popular", example = "cam")
            @RequestParam(name = "nome", required = false) String nome,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(nome, null, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma erva pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erva encontrada"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ErvaResponse> buscar(@PathVariable("id") Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Cadastra uma erva (idempotente)",
            description = "Exige `X-API-Key` e `X-Idempotency-Key`. Repetir a requisição com a mesma chave e o mesmo corpo não cria outro registro: devolve o já criado, com `Idempotency-Replayed: true`. Mesma chave com corpo diferente resulta em 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Erva criada (o cabeçalho Location aponta para o novo recurso)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma erva com esse nome científico",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "422", description = "Chave de idempotência já usada com outro corpo",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<EntityModel<ErvaResponse>> criar(
            @Parameter(description = "Identificador único da operação (sugestão: UUID)",
                    example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", required = true)
            @RequestHeader(name = IdempotencyService.HEADER) String idempotencyKey,
            @Valid @RequestBody ErvaRequest request,
            HttpServletRequest http) {

        ApiKey apiKey = (ApiKey) http.getAttribute(ApiKeyInterceptor.ATRIBUTO_API_KEY);
        IdempotencyService.Resultado<Erva> resultado = idempotencyService.executar(
                apiKey.getId(), idempotencyKey, "POST /api/v1/ervas " + request,
                () -> service.criar(request), Erva::getId, service::buscar);

        EntityModel<ErvaResponse> model = assembler.toModel(resultado.recurso());
        ResponseEntity.BodyBuilder resposta = ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri());
        if (resultado.reaproveitado()) {
            resposta.header("Idempotency-Replayed", "true");
        }
        return resposta.body(model);
    }

    @PutMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Atualiza uma erva", description = "Substitui todos os dados da erva.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erva atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome científico já usado por outra erva",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public EntityModel<ErvaResponse> atualizar(@PathVariable("id") Long id, @Valid @RequestBody ErvaRequest request) {
        return assembler.toModel(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ExigeNivel(NivelAcesso.USUARIO)
    @Operation(summary = "Remove uma erva",
            description = "Remove também a ficha botânica e os usos da erva. Não é possível remover uma erva usada em receitas ou rituais.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Erva removida"),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Erva em uso por receitas ou rituais",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable("id") Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Lista ervas por categoria (paginado)", description = "Consulta personalizada por categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ervas da categoria"),
            @ApiResponse(responseCode = "400", description = "Categoria inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PagedModel<EntityModel<ErvaResponse>> porCategoria(
            @Parameter(description = "MEDICINAL, CULINARIA, ESPIRITUAL, AROMATICA ou ORNAMENTAL", example = "AROMATICA")
            @PathVariable("categoria") CategoriaErva categoria,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.listar(null, categoria, pageable), assembler);
    }

    @GetMapping("/busca/cientifico")
    @Operation(summary = "Busca ervas pelo nome científico (paginado)", description = "Busca parcial, sem diferenciar maiúsculas.")
    @ApiResponse(responseCode = "200", description = "Página de ervas encontradas")
    public PagedModel<EntityModel<ErvaResponse>> porNomeCientifico(
            @Parameter(description = "Trecho do nome científico", example = "matricaria", required = true)
            @RequestParam("nome") String nome,
            @ParameterObject Pageable pageable) {
        return pagedAssembler.toModel(service.buscarPorNomeCientifico(nome, pageable), assembler);
    }
}
