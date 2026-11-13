package br.com.grimorioverde.config;

import br.com.grimorioverde.security.ExigeNivel;
import br.com.grimorioverde.security.NivelAcesso;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O Swagger UI mostra um seletor com três grupos:
 * v1 (/api/v1/**), v2 (/api/v2/**) e versionamento por cabeçalho (/api/ervas).
 * As descrições usam Markdown, que o Swagger UI renderiza.
 */
@Configuration
public class OpenApiConfig {

    private static final String AVISO = "As informações desta API são educativas e não substituem "
            + "orientação médica, nutricional ou de outro profissional habilitado.";

    private static final String SOBRE = """
            ## O que é esta API
            O **Grimório Verde** é um catálogo de ervas. Para cada erva é possível consultar e cadastrar:
            propriedades medicinais, usos culinários e espirituais, formas de uso, receitas, rituais e
            informações botânicas (parte utilizada, cultivo, características e advertências).

            > ⚠️ **Aviso importante:** %s
            > O conteúdo medicinal, culinário e espiritual é apresentado como informação cultural e educativa,
            > nunca como diagnóstico, prescrição ou recomendação de tratamento.

            ## Entidades e relacionamentos
            | Entidade | Descrição | Relacionamento |
            |---|---|---|
            | **Erva** | Nome popular e científico, descrição, categoria (MEDICINAL, CULINARIA, ESPIRITUAL, AROMATICA, ORNAMENTAL) e origem | Entidade central |
            | **Ficha botânica** | Parte utilizada, forma de cultivo, características e advertências | **1 para 1** com Erva (cada erva tem no máximo uma ficha) |
            | **Uso** | Tipo (MEDICINAL, CULINARIO, ESPIRITUAL), descrição, modo de preparo e contraindicações | **N para 1**: uma erva tem vários usos |
            | **Receita** | Nome, ingredientes e modo de preparo | **N para N** com Erva |
            | **Ritual** | Nome, finalidade espiritual, instruções e cuidados | **N para N** com Erva |

            ## Como usar o Swagger
            1. Escolha o grupo no seletor **Select a definition** (canto superior direito): `v1`, `v2` ou `versionamento`.
            2. Abra um endpoint, clique em **Try it out**, preencha os campos e clique em **Execute**.
            3. Confira o código da resposta (por exemplo 200, 201 ou 404) e o corpo JSON logo abaixo.
            4. Rotas com **cadeado** exigem chave de API: clique em **Authorize** (topo da página) e cole a chave.
               Em desenvolvimento, use `gv_usuario_dev_chave_trocar_em_producao` (nível USUARIO) ou
               `gv_admin_dev_chave_trocar_em_producao` (nível ADMIN).

            ## Paginação
            Todas as listagens são paginadas. Parâmetros de consulta:
            - `page`: número da página, começando em **0** (padrão 0);
            - `size`: itens por página (padrão **10**, máximo **50**);
            - `sort`: ordenação no formato `campo,asc` ou `campo,desc` (exemplo: `nomePopular,desc`).

            A resposta traz o bloco `page` (`size`, `totalElements`, `totalPages`, `number`). Pedir uma página
            além do total **não é erro**: a API responde 200 com a lista vazia.

            ## HATEOAS (links nas respostas)
            Cada recurso traz `_links` para navegar pela API: `self` (o próprio recurso), `update` (atualizar),
            `delete` (remover) e links para recursos relacionados. As listagens trazem também `first`, `prev`,
            `next` e `last`. Os links de leitura (GET) podem ser abertos direto no navegador.

            ## Formato dos erros
            Todo erro devolve o mesmo JSON:
            ```json
            {
              "timestamp": "2026-11-13T10:30:00Z",
              "status": 400,
              "erro": "Bad Request",
              "mensagem": "Dados inválidos na requisição.",
              "caminho": "/api/v1/ervas",
              "campos": { "nomePopular": "O nome popular é obrigatório" }
            }
            ```
            O campo `campos` aparece só nos erros de validação e diz qual campo está errado e por quê.

            ## Códigos de status usados
            | Código | Significado |
            |---|---|
            | **200** OK | Consulta ou atualização realizada |
            | **201** Created | Recurso criado (o cabeçalho `Location` traz o endereço) |
            | **204** No Content | Remoção realizada |
            | **400** Bad Request | Dados inválidos, parâmetro com tipo errado ou cabeçalho obrigatório ausente |
            | **401** Unauthorized | Chave de API ausente, inválida ou revogada |
            | **403** Forbidden | A chave não tem o nível de acesso necessário (ADMIN) |
            | **404** Not Found | Recurso não encontrado |
            | **409** Conflict | Conflito: nome científico repetido, erva já com ficha, remoção de erva em uso etc. |
            | **422** Unprocessable Entity | Chave de idempotência reutilizada com um corpo diferente |
            | **429** Too Many Requests | Limite de requisições excedido (veja `Retry-After`) |
            | **500** Internal Server Error | Erro inesperado no servidor |

            ## Regras comuns a toda a API
            - **Chave de API (`X-API-Key`):** leituras (GET) são públicas; cadastrar, alterar e remover exigem chave.
            - **Idempotência (`X-Idempotency-Key`):** todo POST que cria recurso exige este cabeçalho
              (8 a 100 caracteres; use um UUID). Repetir a mesma chave com o mesmo corpo **não cria outro registro**:
              devolve o já criado, com o cabeçalho `Idempotency-Replayed: true`. A mesma chave com corpo
              diferente resulta em **422**.
            - **Limite de requisições:** %d por janela de %d segundos, por IP. Os cabeçalhos `X-RateLimit-Limit`,
              `X-RateLimit-Remaining` e `X-RateLimit-Reset` informam o uso. Ao exceder, a resposta é **429** com
              `Retry-After` (segundos para tentar de novo).
            - **CORS:** o acesso a partir de navegadores é permitido para origens configuradas
              (`http://localhost:3000`, `http://localhost:5173` e `http://localhost:4200`), com os métodos
              GET, POST, PUT, DELETE e OPTIONS.
            - **Versionamento:** a resposta sempre traz o cabeçalho `X-API-Version` com a versão atendida.
            """.formatted(AVISO, 60, 60);

    private static final String V1 = """
            # Grimório Verde API — v1

            Versão **1** (`/api/v1`): CRUD completo e paginado das **cinco entidades**: Erva, Ficha botânica, Uso,
            Receita e Ritual, com HATEOAS e consultas personalizadas.

            ## Endpoints desta versão
            | Recurso | Rotas principais | Consultas personalizadas |
            |---|---|---|
            | **Ervas** | `/api/v1/ervas` | filtro `?nome=`, `/categoria/{categoria}`, `/busca/cientifico?nome=` |
            | **Fichas botânicas** | `/api/v1/fichas-botanicas` | filtro `?parte=`, `/erva/{ervaId}` |
            | **Usos** | `/api/v1/usos` | filtro `?tipo=`, `/erva/{ervaId}` |
            | **Receitas** | `/api/v1/receitas` | filtro `?nome=`, `/erva/{ervaId}` |
            | **Rituais** | `/api/v1/rituais` | filtro `?nome=`, `/erva/{ervaId}` |

            Em cada recurso: `GET` (lista paginada), `GET /{id}`, `POST`, `PUT /{id}` e `DELETE /{id}`.

            ## Quem pode fazer o quê
            - **GET:** público, não precisa de chave.
            - **POST, PUT e DELETE:** exigem `X-API-Key` (nível USUARIO ou ADMIN). POST exige também `X-Idempotency-Key`.

            ## Regras de negócio que geram erro
            - Nome científico da erva **repetido** → 409.
            - Criar uma segunda ficha botânica para a mesma erva → 409.
            - Remover uma erva que é usada em receitas ou rituais → 409.
            - Informar o id de uma erva que não existe (em usos, fichas, receitas e rituais) → 404.

            """ + SOBRE;

    private static final String V2 = """
            # Grimório Verde API — v2

            Versão **2** (`/api/v2`): recursos avançados de API. Esta versão acrescenta, em relação à v1,
            autenticação por chave com níveis de acesso, idempotência obrigatória e a gestão das chaves.

            ## Endpoints desta versão
            | Recurso | Rotas | Acesso |
            |---|---|---|
            | **Ervas (v2)** | `GET /api/v2/ervas` (filtros `?nome=` e `?categoria=`, combináveis), `GET /{id}`, `GET /categoria/{categoria}` | Público |
            | | `POST`, `PUT /{id}` e `DELETE /{id}` | Chave USUARIO ou ADMIN |
            | **Chaves de API** | `POST`, `GET`, `GET /{id}` e `DELETE /{id}` em `/api/v2/api-keys` | Somente **ADMIN** |

            ## O que muda na erva da v2
            A resposta de erva da v2 traz, além dos dados da v1, os campos **`totalUsos`** (quantidade de usos
            cadastrados) e **`possuiFichaBotanica`** (se a erva tem ficha botânica).

            ## Chaves de API e níveis de acesso
            | Nível | Pode |
            |---|---|
            | **USUARIO** | Cadastrar, alterar e remover dados (POST, PUT e DELETE) |
            | **ADMIN** | Tudo o que o USUARIO faz, mais **gerar, listar e revogar chaves** |

            - A chave é enviada no cabeçalho `X-API-Key`.
            - Ao gerar uma chave (`POST /api/v2/api-keys`), o valor completo é mostrado **uma única vez**:
              guarde-o. O sistema guarda apenas o hash (SHA-256), por segurança.
            - Uma chave **revogada** deixa de funcionar imediatamente (401).

            ## Idempotência (`X-Idempotency-Key`)
            O `POST /api/v2/ervas` exige o cabeçalho `X-Idempotency-Key`. Para testar no Swagger:
            1. Execute o POST com uma chave como `teste-0001` e um nome científico novo → **201**.
            2. Execute de novo com a **mesma chave e o mesmo corpo** → 201, mesma erva e `Idempotency-Replayed: true`.
            3. Mude algo no corpo e mantenha a chave → **422**.

            """ + SOBRE;

    private static final String VERSIONAMENTO = """
            # Grimório Verde API — versionamento por cabeçalho

            Neste grupo a **URL é sempre a mesma** (`/api/ervas`) e o cabeçalho **`X-API-Version`** escolhe a
            versão da resposta. É a forma de versionamento por cabeçalho personalizado.

            | Cabeçalho `X-API-Version` | Resposta |
            |---|---|
            | ausente, `1` ou `1.0` | Formato da **v1**: id, nomes, descrição, categoria, origem e aviso |
            | `2` ou `2.0` | Formato da **v2**: o mesmo da v1 mais `totalUsos` e `possuiFichaBotanica` |
            | qualquer outro valor | **400**: versão não suportada |

            ## Endpoints
            - `GET /api/ervas`: lista paginada de ervas (aceita `nome`, `categoria`, `page`, `size` e `sort`).
            - `GET /api/ervas/{id}`: uma erva pelo id (404 se não existir).

            ## Como testar
            1. No `GET /api/ervas`, deixe o campo `X-API-Version` vazio e clique em Execute: formato v1.
            2. Preencha `2` e execute de novo: a resposta ganha `totalUsos` e `possuiFichaBotanica`.
            3. Preencha `9`: erro **400**.

            A resposta sempre devolve o cabeçalho `X-API-Version` com a versão usada.
            Os links de paginação (`next`, `prev`) não repetem o cabeçalho: envie-o também ao segui-los.

            ## Relação com os grupos v1 e v2
            As rotas `/api/v1/...` e `/api/v2/...` têm a versão **na própria URL**. Se você enviar o cabeçalho
            `X-API-Version` com um valor diferente da versão da rota, a API responde 400.

            > ⚠️ **Aviso importante:** %s
            """.formatted(AVISO);

    @Bean
    public OpenAPI grimorioVerdeOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Grimório Verde API").version("v1").description(AVISO))
                .components(new Components().addSecuritySchemes("ApiKeyAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API enviada no cabeçalho X-API-Key. Em desenvolvimento use a chave "
                                        + "USUARIO (`gv_usuario_dev_chave_trocar_em_producao`) ou ADMIN "
                                        + "(`gv_admin_dev_chave_trocar_em_producao`), definidas em application.properties.")));
    }

    /**
     * Todo método anotado com @ExigeNivel ganha, na documentação, o cadeado (ApiKeyAuth)
     * e as respostas 401 (e 403 para ADMIN). Todas as rotas ganham o 429 do rate limit.
     */
    @Bean
    public OperationCustomizer protecaoPorChaveDeApi() {
        return (operation, metodo) -> {
            ApiResponses respostas = operation.getResponses();
            if (respostas == null) {
                respostas = new ApiResponses();
                operation.setResponses(respostas);
            }
            if (!respostas.containsKey("429")) {
                respostas.addApiResponse("429", new ApiResponse()
                        .description("Limite de requisições excedido; veja o cabeçalho Retry-After (segundos)."));
            }
            ExigeNivel exigencia = metodo.getMethodAnnotation(ExigeNivel.class);
            if (exigencia != null) {
                if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
                    operation.addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
                }
                if (!respostas.containsKey("401")) {
                    respostas.addApiResponse("401", new ApiResponse()
                            .description("Chave de API ausente, inválida ou revogada (cabeçalho X-API-Key)."));
                }
                if (exigencia.value() == NivelAcesso.ADMIN && !respostas.containsKey("403")) {
                    respostas.addApiResponse("403", new ApiResponse()
                            .description("A chave informada não tem nível ADMIN."));
                }
            }
            return operation;
        };
    }

    @Bean
    public GroupedOpenApi grupoV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Grimório Verde API — v1")
                        .version("1.0")
                        .contact(new Contact().name("Grimório Verde"))
                        .description(V1)))
                .build();
    }

    @Bean
    public GroupedOpenApi grupoV2() {
        return GroupedOpenApi.builder()
                .group("v2")
                .pathsToMatch("/api/v2/**")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Grimório Verde API — v2")
                        .version("2.0")
                        .contact(new Contact().name("Grimório Verde"))
                        .description(V2)))
                .build();
    }

    @Bean
    public GroupedOpenApi grupoVersionamentoPorCabecalho() {
        return GroupedOpenApi.builder()
                .group("versionamento")
                .pathsToMatch("/api/ervas", "/api/ervas/**")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("Grimório Verde API — versionamento por cabeçalho")
                        .version("1.0 / 2.0")
                        .contact(new Contact().name("Grimório Verde"))
                        .description(VERSIONAMENTO)))
                .build();
    }
}
