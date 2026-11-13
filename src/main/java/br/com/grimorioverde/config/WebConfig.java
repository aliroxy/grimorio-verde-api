package br.com.grimorioverde.config;

import br.com.grimorioverde.security.ApiKeyInterceptor;
import br.com.grimorioverde.security.ApiVersionInterceptor;
import br.com.grimorioverde.security.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiVersionInterceptor apiVersionInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final ApiKeyInterceptor apiKeyInterceptor;
    private final String[] origensPermitidas;

    public WebConfig(ApiVersionInterceptor apiVersionInterceptor,
                     RateLimitInterceptor rateLimitInterceptor,
                     ApiKeyInterceptor apiKeyInterceptor,
                     @Value("${grimorio.cors.allowed-origins}") String[] origensPermitidas) {
        this.apiVersionInterceptor = apiVersionInterceptor;
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.apiKeyInterceptor = apiKeyInterceptor;
        this.origensPermitidas = origensPermitidas;
    }

    /** Ordem: versão -> limite de requisições -> chave de API. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor).addPathPatterns("/api/**").order(1);
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**").order(2);
        registry.addInterceptor(apiKeyInterceptor).addPathPatterns("/api/**").order(3);
    }

    /** CORS: só as origens configuradas, só os métodos usados pela API. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept", "X-API-Key", "X-Idempotency-Key", "X-API-Version")
                .exposedHeaders("Location", "Retry-After", "X-API-Version", "Idempotency-Replayed",
                        "X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
