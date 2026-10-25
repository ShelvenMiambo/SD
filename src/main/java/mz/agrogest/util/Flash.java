package mz.agrogest.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Mensagens que sobrevivem a um redirect (padrão Post/Redirect/Get).
 * São mostradas e removidas pelo cabeçalho das vistas.
 */
public final class Flash {
    public static final String OK = "flashOk";
    public static final String ERRO = "flashErro";

    private Flash() {
    }

    public static void ok(HttpServletRequest req, String mensagem) {
        req.getSession().setAttribute(OK, mensagem);
    }

    public static void erro(HttpServletRequest req, String mensagem) {
        req.getSession().setAttribute(ERRO, mensagem);
    }
}
