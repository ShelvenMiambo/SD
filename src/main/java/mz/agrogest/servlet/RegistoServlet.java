package mz.agrogest.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.NegocioException;
import mz.agrogest.service.UtilizadorService;
import mz.agrogest.util.Flash;
import mz.agrogest.util.Parametros;

/** Auto-registo de clientes. */
@WebServlet("/registo")
public class RegistoServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "registo");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String password = req.getParameter("password");
        String confirmacao = req.getParameter("confirmacao");
        try {
            if (password == null || !password.equals(confirmacao)) {
                throw new NegocioException("As palavras-passe não coincidem.");
            }
            Utilizador u = utilizadores.registarCliente(
                    Parametros.texto(req, "nome"),
                    Parametros.texto(req, "email"),
                    password,
                    Parametros.texto(req, "telefone"),
                    Parametros.texto(req, "endereco"));
            LoginServlet.iniciarSessao(req, u);
            Flash.ok(req, "Bem-vindo(a), " + u.getNome() + "! A sua conta foi criada.");
            redirect(req, resp, "/catalogo");
        } catch (NegocioException e) {
            req.setAttribute("erro", e.getMessage());
            render(req, resp, "registo");
        }
    }
}
