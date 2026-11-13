package br.com.grimorioverde.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de exceções. Estende ResponseEntityExceptionHandler para que
 * os erros padrão do Spring MVC (404, 405, 415, cabeçalho ausente...) também saiam
 * no mesmo formato ApiError.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- exceções da aplicação ----------

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiError> conflito(ConflitoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null, null);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ApiError> requisicaoInvalida(RequisicaoInvalidaException ex, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null, null);
    }

    @ExceptionHandler(IdempotenciaException.class)
    public ResponseEntity<ApiError> idempotencia(IdempotenciaException ex, HttpServletRequest request) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI(), null, null);
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<ApiError> naoAutorizado(NaoAutorizadoException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"grimorio-verde\", header=\"X-API-Key\"");
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI(), null, headers);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ApiError> acessoNegado(AcessoNegadoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI(), null, null);
    }

    @ExceptionHandler(LimiteExcedidoException.class)
    public ResponseEntity<ApiError> limiteExcedido(LimiteExcedidoException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSegundos()));
        return resposta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request.getRequestURI(), null, headers);
    }

    // ---------- erros de banco e validação fora do corpo ----------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return resposta(HttpStatus.CONFLICT,
                "A operação viola uma regra de integridade dos dados (registro duplicado ou em uso).",
                request.getRequestURI(), null, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> violacaoDeRestricao(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> campos.put(v.getPropertyPath().toString(), v.getMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos na requisição.", request.getRequestURI(), campos, null);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ApiError> usoInvalidoDeDados(InvalidDataAccessApiUsageException ex, HttpServletRequest request) {
        Throwable causa = ex;
        while (causa != null) {
            if (causa instanceof PropertyReferenceException pre) {
                return resposta(HttpStatus.BAD_REQUEST,
                        "Campo de ordenação inválido: '" + pre.getPropertyName() + "'.",
                        request.getRequestURI(), null, null);
            }
            causa = causa.getCause();
        }
        return inesperado(ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente mais tarde.", request.getRequestURI(), null, null);
    }

    // ---------- erros padrão do Spring MVC ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return corpo(status, headers, "Dados inválidos na requisição.", caminho(request), campos);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return corpo(status, headers,
                "Corpo da requisição inválido ou mal formatado (verifique o JSON e os valores dos enums).",
                caminho(request), null);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatusCode status,
                                                        WebRequest request) {
        String nome = ex instanceof MethodArgumentTypeMismatchException m ? m.getName() : ex.getPropertyName();
        Class<?> tipo = ex.getRequiredType();
        String aceitos = (tipo != null && tipo.isEnum())
                ? " Valores aceitos: " + Arrays.toString(tipo.getEnumConstants()) + "."
                : "";
        return corpo(status, headers,
                "Valor inválido '" + ex.getValue() + "' para o parâmetro '" + nome + "'." + aceitos,
                caminho(request), null);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        return corpo(statusCode, headers, mensagemPara(ex, statusCode, body), caminho(request), null);
    }

    // ---------- utilitários ----------

    private String mensagemPara(Exception ex, HttpStatusCode status, Object body) {
        if (ex instanceof MissingRequestHeaderException m) {
            return "O cabeçalho '" + m.getHeaderName() + "' é obrigatório.";
        }
        if (ex instanceof MissingServletRequestParameterException m) {
            return "O parâmetro '" + m.getParameterName() + "' é obrigatório.";
        }
        return switch (status.value()) {
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "Formato de resposta solicitado não é suportado.";
            case 415 -> "Tipo de conteúdo não suportado. Use application/json.";
            default -> (body instanceof ProblemDetail pd && pd.getDetail() != null)
                    ? pd.getDetail()
                    : "Não foi possível processar a requisição.";
        };
    }

    private String caminho(WebRequest request) {
        return request instanceof ServletWebRequest s ? s.getRequest().getRequestURI() : null;
    }

    private ResponseEntity<Object> corpo(HttpStatusCode status, HttpHeaders headers, String mensagem,
                                         String caminho, Map<String, String> campos) {
        return ResponseEntity.status(status).headers(headers).body(montar(status, mensagem, caminho, campos));
    }

    private ResponseEntity<ApiError> resposta(HttpStatus status, String mensagem, String caminho,
                                              Map<String, String> campos, HttpHeaders headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (headers != null) {
            builder.headers(headers);
        }
        return builder.body(montar(status, mensagem, caminho, campos));
    }

    private ApiError montar(HttpStatusCode status, String mensagem, String caminho, Map<String, String> campos) {
        HttpStatus hs = HttpStatus.resolve(status.value());
        String erro = hs != null ? hs.getReasonPhrase() : String.valueOf(status.value());
        return new ApiError(LocalDateTime.now(), status.value(), erro, mensagem, caminho, campos);
    }
}
