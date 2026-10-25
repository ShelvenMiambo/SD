package mz.agrogest.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.UtilizadorService;

/**
 * Controlo de acesso por sessão e perfil.
 * <ul>
 *   <li>RN-e: as rotas do cliente exigem sessão iniciada (senão, redirecciona para o login);</li>
 *   <li>RN-d: as rotas {@code /admin/*} são exclusivas do administrador (HTTP 403 caso contrário).</li>
 * </ul>
 */
@WebFilter(urlPatterns = {
        "/catalogo", "/carrinho", "/carrinho/*", "/compra", "/factura", "/minhas-compras", "/perfil",
        "/admin", "/admin/*"
})
public class AuthFilter implements Filter {
    public static final String ATTR_UTILIZADOR = "utilizador";

    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession sessao = req.getSession(false);
        Utilizador u = sessao == null ? null : (Utilizador) sessao.getAttribute(ATTR_UTILIZADOR);

        // Relê a conta: se foi desativada (ou deixou de existir) a sessão deixa de ser válida.
        Utilizador atual = u == null ? null : utilizadores.porId(u.getId()).filter(Utilizador::isAtivo).orElse(null);
        if (atual == null) {
            if (sessao != null) {
                sessao.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        sessao.setAttribute(ATTR_UTILIZADOR, atual);
        u = atual;

        String caminho = req.getRequestURI().substring(req.getContextPath().length());
        boolean rotaAdmin = caminho.equals("/admin") || caminho.startsWith("/admin/");
        if (rotaAdmin && !u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso reservado ao administrador.");
            return;
        }
        // Carrinho, compra, histórico e perfil são do cliente: o administrador não compra.
        boolean rotaCliente = caminho.equals("/carrinho") || caminho.startsWith("/carrinho/")
                || caminho.equals("/compra") || caminho.equals("/minhas-compras") || caminho.equals("/perfil");
        if (rotaCliente && u.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "O administrador não efetua compras.");
            return;
        }

        // Evita que o browser mostre páginas privadas em cache depois do logout.
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
