package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.service.RelatorioService;
import mz.agrogest.servlet.BaseServlet;

@WebServlet("/admin/relatorios")
public class RelatoriosServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final RelatorioService relatorios = new RelatorioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("porNome", relatorios.clientesPorNome());
        req.setAttribute("compraram", relatorios.clientesQueCompraram());
        req.setAttribute("assiduos", relatorios.maisAssiduos());
        req.setAttribute("semCompras", relatorios.clientesSemCompras());
        render(req, resp, "admin/relatorios");
    }
}
