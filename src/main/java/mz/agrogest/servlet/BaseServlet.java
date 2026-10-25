package mz.agrogest.servlet;

import java.io.IOException;
import java.util.function.Supplier;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mz.agrogest.filter.AuthFilter;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.NegocioException;
import mz.agrogest.util.Flash;

/** Utilitários comuns aos controladores. */
public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String ATTR_CARRINHO = "carrinho";

    /** Encaminha para {@code /WEB-INF/views/<vista>.jsp}. */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String vista)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + vista + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String caminho) throws IOException {
        resp.sendRedirect(req.getContextPath() + caminho);
    }

    protected Utilizador utilizador(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (Utilizador) s.getAttribute(AuthFilter.ATTR_UTILIZADOR);
    }

    protected Carrinho carrinho(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Carrinho c = (Carrinho) s.getAttribute(ATTR_CARRINHO);
        if (c == null) {
            c = new Carrinho();
            s.setAttribute(ATTR_CARRINHO, c);
        }
        return c;
    }

    /**
     * Executa uma acção de escrita (POST) e redirecciona para {@code destino}.
     * A mensagem devolvida pela acção é mostrada como sucesso; uma {@link NegocioException} como erro.
     */
    protected void executar(HttpServletRequest req, HttpServletResponse resp, String destino,
                            Supplier<String> acao) throws IOException {
        try {
            String msg = acao.get();
            if (msg != null) {
                Flash.ok(req, msg);
            }
        } catch (NegocioException e) {
            Flash.erro(req, e.getMessage());
        }
        redirect(req, resp, destino);
    }

    protected void naoEncontrado(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
