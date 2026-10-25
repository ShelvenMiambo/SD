package mz.agrogest.service;

/**
 * Violação de uma regra de negócio ou validação. A mensagem é mostrada ao utilizador.
 * O {@code campo} opcional indica o campo do formulário com erro, para a vista o assinalar.
 */
public class NegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String campo;

    public NegocioException(String mensagem) {
        this(mensagem, null);
    }

    public NegocioException(String mensagem, String campo) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
