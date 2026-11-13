package br.com.grimorioverde.security;

import br.com.grimorioverde.exception.LimiteExcedidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limitação de taxa por IP, em janela fixa e em memória.
 * Informa o uso nos cabeçalhos X-RateLimit-* e responde 429 + Retry-After ao exceder.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int TAMANHO_MAXIMO_MAPA = 10_000;

    private static final class Janela {
        final long fimEmMs;
        final AtomicInteger usadas = new AtomicInteger();

        Janela(long fimEmMs) {
            this.fimEmMs = fimEmMs;
        }
    }

    private final int limite;
    private final long janelaMs;
    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();

    public RateLimitInterceptor(@Value("${grimorio.rate-limit.requests:60}") int limite,
                                @Value("${grimorio.rate-limit.window-seconds:60}") long janelaSegundos) {
        this.limite = limite;
        this.janelaMs = janelaSegundos * 1000;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        long agora = System.currentTimeMillis();
        Janela janela = janelas.compute(request.getRemoteAddr(),
                (cliente, atual) -> atual == null || agora >= atual.fimEmMs ? new Janela(agora + janelaMs) : atual);

        int usadas = janela.usadas.incrementAndGet();
        long segundosAteReiniciar = Math.max(1, (janela.fimEmMs - agora + 999) / 1000);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limite));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limite - usadas)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(segundosAteReiniciar));

        if (janelas.size() > TAMANHO_MAXIMO_MAPA) {
            janelas.entrySet().removeIf(e -> agora >= e.getValue().fimEmMs);
        }
        if (usadas > limite) {
            throw new LimiteExcedidoException(segundosAteReiniciar);
        }
        return true;
    }
}
