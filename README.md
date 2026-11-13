# Grimório Verde API

API REST sobre ervas: propriedades medicinais, culinárias e espirituais, formas de uso, receitas, rituais e informações botânicas.

> **Aviso:** as informações desta API são educativas e não substituem orientação médica, nutricional ou de outro profissional habilitado.

## Tecnologias
Java 17 · Spring Boot 3.5 · Maven · H2 · Spring Data JPA · Bean Validation · Spring HATEOAS · Springdoc OpenAPI

## Como rodar no IntelliJ IDEA
1. **File > Open** e selecione a pasta `grimorio-verde` (a que contém o `pom.xml`).
2. Clique em **Trust Project** e, se aparecer, em **Load Maven Changes**.
3. Confira o JDK 17 em **File > Project Structure > Project**.
4. Execute `GrimorioVerdeApplication` (botão verde ao lado do `main`).

| O quê | Endereço |
|---|---|
| Swagger UI (seletor **v1 / v2** no topo) | http://localhost:8080/swagger-ui.html |
| OpenAPI v1 / v2 (JSON) | http://localhost:8080/api-docs/v1 · http://localhost:8080/api-docs/v2 |
| Página web (lista de ervas, com CSS) | http://localhost:8080/ |
| Console H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:grimorioverde`, user `sa`, senha vazia) |

Ao iniciar, a aplicação carrega ervas, fichas, usos, receitas e rituais de exemplo.

## Página web
Em `src/main/resources/static/` (`index.html`, `css/style.css`, `js/app.js`) há uma página simples que consome a própria API: lista as ervas com busca por nome, filtro por categoria e paginação, e mostra os detalhes de cada uma (ficha botânica, usos, receitas e rituais). Abre em http://localhost:8080/ sem precisar de outro servidor.

## Modelo de dados
- `Erva` 1 — 1 `FichaBotanica`
- `Erva` 1 — N `Uso`
- `Receita` N — N `Erva`
- `Ritual` N — N `Erva`
- Enums: `CategoriaErva`, `TipoUso`

## v1 — Parte 1 (`/api/v1`)
Leituras (GET) são públicas; **POST, PUT e DELETE exigem chave de API** (`X-API-Key`, nível USUARIO ou ADMIN) e todo POST exige também `X-Idempotency-Key`. CRUD completo, listagens paginadas (`?page=0&size=10&sort=nomePopular,asc`), HATEOAS e consultas personalizadas.

| Recurso | Rotas |
|---|---|
| Ervas | `/api/v1/ervas` (+ `?nome=`), `/{id}`, `/categoria/{categoria}`, `/busca/cientifico?nome=` |
| Fichas botânicas | `/api/v1/fichas-botanicas` (+ `?parte=`), `/{id}`, `/erva/{ervaId}` |
| Usos | `/api/v1/usos` (+ `?tipo=`), `/{id}`, `/erva/{ervaId}` |
| Receitas | `/api/v1/receitas` (+ `?nome=`), `/{id}`, `/erva/{ervaId}` |
| Rituais | `/api/v1/rituais` (+ `?nome=`), `/{id}`, `/erva/{ervaId}` |

## v2 — Parte 2 (`/api/v2`)
| Recurso | Como funciona |
|---|---|
| **Autenticação por chave de API** | Cabeçalho `X-API-Key`. **Gerar, listar e revogar chaves exige nível `ADMIN`** (`/api/v2/api-keys`; o corpo aceita `proprietario` e `nivel` = USUARIO ou ADMIN). Chaves são guardadas como hash SHA-256. |
| **Idempotência** | **Todos os POSTs que criam recursos** (v1: ervas, fichas, usos, receitas, rituais; v2: ervas) exigem `X-Idempotency-Key` (8 a 100 caracteres; sugestão: UUID). Mesma chave + mesmo corpo = devolve o recurso já criado (`Idempotency-Replayed: true`) sem duplicar. Mesma chave + corpo diferente (ou em outra rota) = 422. Sem o cabeçalho = 400. PUT e DELETE são idempotentes pela própria semântica HTTP. |
| **Rate limiting** | 60 requisições por minuto por IP em `/api/**` (configurável). Cabeçalhos `X-RateLimit-Limit/Remaining/Reset`; ao exceder: **429** + `Retry-After`. |
| **CORS** | Origens, métodos e cabeçalhos permitidos configurados em `WebConfig` / `application.properties`. |
| **Versionamento** | (a) Na rota: `/api/v1`, `/api/v2` — a resposta traz `X-API-Version` e, se o cliente enviar o cabeçalho com valor diferente da rota, recebe 400. (b) **Por cabeçalho**: `GET /api/ervas` e `GET /api/ervas/{id}` devolvem o formato v1 (padrão) ou v2 conforme `X-API-Version: 1` ou `2`; outro valor = 400. Grupo próprio no Swagger ("versionamento"). |
| **Duas versões do mesmo endpoint** | `GET /api/v1/ervas/{id}` e `GET /api/v2/ervas/{id}` (a v2 acrescenta `totalUsos` e `possuiFichaBotanica`). |

Rotas protegidas: `POST`, `PUT` e `DELETE` de todas as rotas da v1 e de `/api/v2/ervas` (nível USUARIO) e todas as rotas de `/api/v2/api-keys` (nível ADMIN). Leituras de ervas são públicas.

### Testando a v2 rapidamente
```bash
# 1) gerar uma chave (só a ADMIN pode; guarde o valor de "chave")
curl -X POST http://localhost:8080/api/v2/api-keys -H "Content-Type: application/json" \
  -H "X-API-Key: gv_admin_dev_chave_trocar_em_producao" -d '{"proprietario":"Maria","nivel":"USUARIO"}'

# 2) criar uma erva (repita o mesmo comando: não cria duplicada)
curl -X POST http://localhost:8080/api/v2/ervas \
  -H "Content-Type: application/json" -H "X-API-Key: SUA_CHAVE" \
  -H "X-Idempotency-Key: 7c9e6679-7425-40de-944b-e07fc1f90ae7" \
  -d '{"nomePopular":"Lavanda","nomeCientifico":"Lavandula angustifolia","descricao":"Arbusto aromático de flores roxas.","categoria":"AROMATICA","origem":"Mediterrâneo"}'
```
No Swagger, use o botão **Authorize** com a chave (as rotas protegidas têm um cadeado). Chaves de desenvolvimento criadas na inicialização (`application.properties`; troque-as fora do ambiente de estudo):

| Nível | Propriedade | Valor padrão |
|---|---|---|
| ADMIN | `grimorio.security.admin-api-key` | `gv_admin_dev_chave_trocar_em_producao` |
| USUARIO | `grimorio.security.user-api-key` | `gv_usuario_dev_chave_trocar_em_producao` |

## Coleção do Postman
Arquivo: `postman/Grimorio-Verde.postman_collection.json` (Postman → Import). Com a API rodando, execute as pastas em ordem pelo **Runner**. As variáveis `baseUrl`, `adminKey` e `userKey` já vêm preenchidas; ids criados são guardados automaticamente. Cobre CRUD v1, v2 + idempotência, versionamento por cabeçalho, chaves ADMIN, rate limit e CORS. O limite é de 60 requisições/minuto por IP: se a execução completa bater em 429, espere o `Retry-After` e continue.

## Erros
Todas as respostas de erro seguem o formato:
```json
{ "timestamp": "...", "status": 400, "erro": "Bad Request", "mensagem": "Dados inválidos na requisição.", "caminho": "/api/v1/ervas", "campos": { "nomePopular": "O nome popular é obrigatório" } }
```
Códigos usados: 400, 401, 403, 404, 405, 409, 415, 422, 429, 500.

## Observações
- O limite de requisições e as chaves de idempotência ficam em memória/H2: reiniciar a aplicação zera tudo.
- O limite usa o IP de origem da conexão; atrás de um proxy seria preciso configurar `X-Forwarded-For`.
