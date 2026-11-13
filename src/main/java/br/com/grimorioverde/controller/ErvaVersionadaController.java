package br.com.grimorioverde.controller;

import br.com.grimorioverde.assembler.ErvaAssembler;
import br.com.grimorioverde.assembler.ErvaAssemblerV2;
import br.com.grimorioverde.entity.CategoriaErva;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.exception.ApiError;
import br.com.grimorioverde.exception.RequisicaoInvalidaException;
import br.com.grimorioverde.service.ErvaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versionamento por cabeçalho: a URL é sempre a mesma (/api/ervas) e o cabeçalho
 * X-API-Version escolhe o formato da resposta.
 * <ul>
 *   <li>sem cabeçalho, "1" ou "1.0": formato da v1 (ErvaResponse);</li>
 *   <li>"2" ou "2.0": formato da v2 (ErvaResponseV2, com totalUsos e possuiFichaBotanica);</li>
 *   <li>qualquer outro valor: 400.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/ervas")
@Tag(name = "Ervas (versão por cabeçalho)",
        description = "Mesma URL, duas versões: o cabeçalho `X-API-Version` (1 ou 2) escolhe o formato da resposta. "
                + "Sem o cabeçalho, vale a versão 1.")
public class ErvaVersionadaController {

    public static final String HEADER_VERSAO = "X-API-Version";

    private final ErvaService service;
    private final ErvaAssembler assemblerV1;
    private final ErvaAssemblerV2 assemblerV2;
    private final PagedResourcesAssembler<Erva> pagedAssembler;

    public ErvaVersionadaController(ErvaService service, ErvaAssembler assemblerV1, ErvaAssemblerV2 assemblerV2,
                                    PagedResourcesAssembler<Erva> pagedAssembler) {
        this.service = service;
        this.assemblerV1 = assemblerV1;
        this.assemblerV2 = assemblerV2;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista ervas (paginado) na versão escolhida pelo cabeçalho",
            description = "**Versão 1** (padrão): cada erva traz id, nomes, descrição, categoria, origem e aviso. "
                    + "**Versão 2**: acrescenta `totalUsos` e `possuiFichaBotanica`. "
                    + "Os links de paginação (`next`, `prev`) não repetem o cabeçalho: envie-o também ao segui-los.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ervas no formato da versão pedida",
                    headers = @Header(name = HEADER_VERSAO, description = "Versão usada na resposta (1 ou 2)")),
            @ApiResponse(responseCode = "400", description = "Versão não suportada ou parâmetro inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RepresentationModel<?>> listar(
            @Parameter(in = ParameterIn.HEADER, name = HEADER_VERSAO, required = false,
                    description = "Versão da API: 1 (padrão) ou 2.",
                    schema = @Schema(type = "string", allowableValues = {"1", "2"}, defaultValue = "1"))
            @RequestHeader(name = HEADER_VERSAO, required = false) String versao,
            @Parameter(description = "Trecho do nome popular", example = "alecrim")
            @RequestParam(name = "nome", required = false) String nome,
            @Parameter(description = "Categoria da erva", example = "CULINARIA")
            @RequestParam(name = "categoria", required = false) CategoriaErva categoria,
            @ParameterObject Pageable pageable) {

        String v = resolverVersao(versao);
        var pagina = service.listar(nome, categoria, pageable);

        RepresentationModel<?> corpo;
        if ("2".equals(v)) {
            corpo = pagedAssembler.toModel(pagina, assemblerV2);
        } else {
            corpo = pagedAssembler.toModel(pagina, assemblerV1);
        }
        return ResponseEntity.ok().header(HEADER_VERSAO, v).body(corpo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma erva pelo id na versão escolhida pelo cabeçalho",
            description = "**Versão 1** (padrão): formato básico. **Versão 2**: acrescenta `totalUsos` e `possuiFichaBotanica`.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erva encontrada, no formato da versão pedida",
                    headers = @Header(name = HEADER_VERSAO, description = "Versão usada na resposta (1 ou 2)")),
            @ApiResponse(responseCode = "400", description = "Versão não suportada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Erva não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RepresentationModel<?>> buscar(
            @PathVariable("id") Long id,
            @Parameter(in = ParameterIn.HEADER, name = HEADER_VERSAO, required = false,
                    description = "Versão da API: 1 (padrão) ou 2.",
                    schema = @Schema(type = "string", allowableValues = {"1", "2"}, defaultValue = "1"))
            @RequestHeader(name = HEADER_VERSAO, required = false) String versao) {

        String v = resolverVersao(versao);
        Erva erva = service.buscar(id);

        RepresentationModel<?> corpo;
        if ("2".equals(v)) {
            corpo = assemblerV2.toModel(erva);
        } else {
            corpo = assemblerV1.toModel(erva);
        }
        return ResponseEntity.ok().header(HEADER_VERSAO, v).body(corpo);
    }

    /** Aceita 1, 1.0, 2 e 2.0 (ou ausente = 1). Qualquer outro valor é erro 400. */
    private String resolverVersao(String versao) {
        if (versao == null || versao.isBlank()) {
            return "1";
        }
        String v = versao.trim();
        if (v.equals("1") || v.equals("1.0")) {
            return "1";
        }
        if (v.equals("2") || v.equals("2.0")) {
            return "2";
        }
        throw new RequisicaoInvalidaException(
                "Versão de API '" + v + "' não suportada. Use o cabeçalho " + HEADER_VERSAO + " com 1 ou 2.");
    }
}
