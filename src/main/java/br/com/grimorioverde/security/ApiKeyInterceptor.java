package br.com.grimorioverde.security;

import br.com.grimorioverde.exception.AcessoNegadoException;
import br.com.grimorioverde.exception.NaoAutorizadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Autenticação por chave de API (cabeçalho X-API-Key).
 * Só atua em métodos anotados com {@link ExigeNivel}; os demais continuam públicos.
 */
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-API-Key";
    public static final String ATRIBUTO_API_KEY = "br.com.grimorioverde.apiKey";

    private final ApiKeyService apiKeyService;

    public ApiKeyInterceptor(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        ExigeNivel exigencia = metodo.getMethodAnnotation(ExigeNivel.class);
        if (exigencia == null) {
            return true;
        }

        String chave = request.getHeader(HEADER);
        if (chave == null || chave.isBlank()) {
            throw new NaoAutorizadoException("Informe sua chave de API no cabeçalho " + HEADER + ".");
        }
        ApiKey apiKey = apiKeyService.autenticar(chave)
                .orElseThrow(() -> new NaoAutorizadoException("Chave de API inválida ou revogada."));

        if (apiKey.getNivel().ordinal() < exigencia.value().ordinal()) {
            throw new AcessoNegadoException("Sua chave tem nível " + apiKey.getNivel()
                    + ", mas este recurso exige nível " + exigencia.value() + ".");
        }
        request.setAttribute(ATRIBUTO_API_KEY, apiKey);
        return true;
    }
}
