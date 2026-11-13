package br.com.grimorioverde.security;

import br.com.grimorioverde.exception.RequisicaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Informa a versão atendida no cabeçalho de resposta X-API-Version e,
 * se o cliente enviar X-API-Version, confere se bate com a versão da rota.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-API-Version";
    private static final Pattern VERSAO_NA_ROTA = Pattern.compile("^/api/v(\\d+)(/.*)?$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Matcher m = VERSAO_NA_ROTA.matcher(request.getRequestURI());
        if (!m.matches()) {
            return true;
        }
        String versaoDaRota = m.group(1);
        response.setHeader(HEADER, versaoDaRota);

        String pedida = request.getHeader(HEADER);
        if (pedida != null && !pedida.isBlank() && !pedida.trim().equals(versaoDaRota)) {
            throw new RequisicaoInvalidaException("O cabeçalho " + HEADER + " (" + pedida.trim()
                    + ") não corresponde à versão da rota (v" + versaoDaRota + ").");
        }
        return true;
    }
}
