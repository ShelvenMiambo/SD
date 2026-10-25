package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.util.Comparator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Produto;
import mz.agrogest.service.AgricolaService;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.service.UtilizadorService;
import mz.agrogest.service.VendaService;
import mz.agrogest.servlet.BaseServlet;

@WebServlet("/admin")
public class DashboardServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final int STOCK_BAIXO = 20;
    private static final int DIAS_VALIDADE = 30;

    private final CatalogoService catalogo = new CatalogoService();
    private final UtilizadorService utilizadores = new UtilizadorService();
    private final VendaService vendas = new VendaService();
    private final AgricolaService agricola = new AgricolaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("nFornecedores", catalogo.fornecedores().size());
        req.setAttribute("nProdutos", catalogo.produtos().size());
        req.setAttribute("nClientes", utilizadores.clientes().size());
        req.setAttribute("nCampanhas", agricola.campanhas().size());
        req.setAttribute("faturacao", vendas.faturacaoTotal());
        req.setAttribute("ultimasVendas", vendas.vendas().stream().limit(5).toList());
        req.setAttribute("nVendas", vendas.vendasValidas().size());
        req.setAttribute("stockBaixo", catalogo.produtos().stream()
                .filter(p -> p.getQuantidade() <= STOCK_BAIXO)
                .sorted(Comparator.comparingInt(Produto::getQuantidade))
                .toList());
        req.setAttribute("limiteStock", STOCK_BAIXO);
        req.setAttribute("aExpirar", catalogo.aExpirar(DIAS_VALIDADE));
        req.setAttribute("diasValidade", DIAS_VALIDADE);
        render(req, resp, "admin/dashboard");
    }
}
