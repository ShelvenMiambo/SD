package mz.agrogest.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mz.agrogest.filter.AuthFilter;
import mz.agrogest.filter.CsrfFilter;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.NegocioException;
import mz.agrogest.util.Flash;

/** Utilitários comuns aos controladores. */
public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String ATTR_CARRINHO = "carrinho";
    /** Valores do formulário que falhou (JSON) e o campo com erro, para a vista os repor. */
    public static final String ATTR_FORM_ANTERIOR = "formAnterior";
    public static final String ATTR_ERRO_CAMPO = "erroCampo";
    /** Campos que nunca são devolvidos ao browser. */
    private static final Set<String> NAO_REPOR = Set.of(CsrfFilter.PARAM, "password", "confirmacao", "atual", "nova");

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
     * A mensagem devolvida pela acção é mostrada como sucesso. Numa {@link NegocioException}
     * a mensagem é mostrada como erro e os valores escritos são guardados para o formulário os repor.
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
            guardarFormulario(req, e.getCampo());
        }
        redirect(req, resp, destino);
    }

    /** Guarda os parâmetros do pedido (em JSON) e o campo com erro na sessão, para a próxima página. */
    protected static void guardarFormulario(HttpServletRequest req, String campoErro) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            if (NAO_REPOR.contains(e.getKey()) || e.getValue().length == 0) {
                continue;
            }
            if (json.length() > 1) {
                json.append(',');
            }
            json.append(jsonTexto(e.getKey())).append(':').append(jsonTexto(e.getValue()[0]));
        }
        json.append('}');
        HttpSession s = req.getSession();
        s.setAttribute(ATTR_FORM_ANTERIOR, json.toString());
        if (campoErro != null) {
            s.setAttribute(ATTR_ERRO_CAMPO, campoErro);
        }
    }

    private static String jsonTexto(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }

    protected void naoEncontrado(HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
