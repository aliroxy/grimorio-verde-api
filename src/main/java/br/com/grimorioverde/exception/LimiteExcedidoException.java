package br.com.grimorioverde.exception;

public class LimiteExcedidoException extends RuntimeException {

    private final long retryAfterSegundos;

    public LimiteExcedidoException(long retryAfterSegundos) {
        super("Limite de requisições excedido. Tente novamente em " + retryAfterSegundos + " segundo(s).");
        this.retryAfterSegundos = retryAfterSegundos;
    }

    public long getRetryAfterSegundos() {
        return retryAfterSegundos;
    }
}
