package br.com.grimorioverde.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado(a) com id " + id);
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
