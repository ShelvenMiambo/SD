package mz.agrogest.service;

/** Violação de uma regra de negócio ou validação. A mensagem é mostrada ao utilizador. */
public class NegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
