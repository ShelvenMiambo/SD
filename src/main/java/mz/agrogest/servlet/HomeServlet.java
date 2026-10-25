package mz.agrogest.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.service.UtilizadorService;

/** Raiz da aplicação: página inicial pública; utilizadores autenticados vão para a sua área. */
@WebServlet("")
public class HomeServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogo = new CatalogoService();
    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Utilizador u = utilizador(req);
        if (u != null) {
            redirect(req, resp, u.isAdmin() ? "/admin" : "/catalogo");
            return;
        }
        req.setAttribute("nProdutos", catalogo.produtos().size());
        req.setAttribute("nFornecedores", catalogo.fornecedores().size());
        req.setAttribute("nClientes", utilizadores.clientes().size());
        render(req, resp, "inicio");
    }
}
