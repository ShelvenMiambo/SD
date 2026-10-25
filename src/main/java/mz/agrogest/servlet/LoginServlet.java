package mz.agrogest.servlet;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.filter.AuthFilter;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.UtilizadorService;
import mz.agrogest.util.Parametros;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (utilizador(req) != null) {
            redirect(req, resp, "/");
            return;
        }
        render(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = Parametros.texto(req, "email");
        String password = req.getParameter("password");

        Optional<Utilizador> u = utilizadores.autenticar(email, password);
        if (u.isEmpty()) {
            req.setAttribute("erro", "Email ou palavra-passe incorretos.");
            req.setAttribute("email", email);
            render(req, resp, "login");
            return;
        }
        iniciarSessao(req, u.get());
        redirect(req, resp, u.get().isAdmin() ? "/admin" : "/catalogo");
    }

    /** Cria a sessão e muda o seu ID (protecção contra fixação de sessão). */
    static void iniciarSessao(HttpServletRequest req, Utilizador u) {
        req.getSession(true);
        req.changeSessionId();
        req.getSession().setAttribute(AuthFilter.ATTR_UTILIZADOR, u);
    }
}
