package mz.agrogest.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mz.agrogest.util.Flash;

/**
 * Protecção contra CSRF (pedidos forjados a partir de outros sites).
 * Cada sessão tem um token aleatório que as JSP põem num campo escondido {@code _csrf}
 * de todos os formulários POST. Um POST sem o token certo é recusado.
 */
@WebFilter(urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
public class CsrfFilter implements Filter {
    public static final String ATTR = "csrf";
    public static final String PARAM = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String caminho = req.getRequestURI().substring(req.getContextPath().length());
        if (caminho.startsWith("/css/") || caminho.startsWith("/js/") || caminho.startsWith("/img/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession sessao = req.getSession();
        String token = (String) sessao.getAttribute(ATTR);
        if (token == null) {
            byte[] b = new byte[32];
            RANDOM.nextBytes(b);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            sessao.setAttribute(ATTR, token);
        }
        req.setAttribute(ATTR, token);

        if ("POST".equalsIgnoreCase(req.getMethod()) && !tokenValido(token, req.getParameter(PARAM))) {
            if (caminho.equals("/login") || caminho.equals("/registo")) {
                // Normalmente é só a sessão que expirou com a página aberta: pede para tentar de novo.
                Flash.erro(req, "A página expirou. Tente novamente.");
                resp.sendRedirect(req.getContextPath() + caminho);
            } else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Pedido inválido ou página expirada.");
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean tokenValido(String esperado, String enviado) {
        return enviado != null && MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.US_ASCII), enviado.getBytes(StandardCharsets.US_ASCII));
    }
}
