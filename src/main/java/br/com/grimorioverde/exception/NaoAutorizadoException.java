package br.com.grimorioverde.exception;

public class NaoAutorizadoException extends RuntimeException {

    public NaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
