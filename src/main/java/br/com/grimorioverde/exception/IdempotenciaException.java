package br.com.grimorioverde.exception;

public class IdempotenciaException extends RuntimeException {

    public IdempotenciaException(String mensagem) {
        super(mensagem);
    }
}
