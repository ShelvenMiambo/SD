package mz.agrogest.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.filter.AuthFilter;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.UtilizadorService;
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Parametros;

/** O cliente consulta e altera os seus dados e a palavra-passe. */
@WebServlet("/perfil")
public class PerfilServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("provincias", Opcoes.PROVINCIAS);
        render(req, resp, "perfil");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Utilizador u = utilizador(req);
        String acao = Parametros.texto(req, "acao");
        executar(req, resp, "/perfil", () -> {
            if ("password".equals(acao)) {
                utilizadores.alterarPassword(u.getId(), req.getParameter("atual"),
                        req.getParameter("nova"), req.getParameter("confirmacao"));
                return "Palavra-passe alterada.";
            }
            Utilizador atualizado = utilizadores.atualizarPerfil(u.getId(),
                    Parametros.texto(req, "nome"), Parametros.texto(req, "telefone"),
                    Parametros.texto(req, "provincia"), Parametros.texto(req, "endereco"));
            req.getSession().setAttribute(AuthFilter.ATTR_UTILIZADOR, atualizado);
            return "Dados actualizados. As facturas já emitidas mantêm os dados da altura da compra.";
        });
    }
}
