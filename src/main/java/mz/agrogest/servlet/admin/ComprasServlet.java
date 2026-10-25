package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Venda;
import mz.agrogest.service.RelatorioService;
import mz.agrogest.service.VendaService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Pagina;
import mz.agrogest.util.Parametros;

/**
 * Compras detalhadas: cliente, endereço, telefone, itens, quantidades, preços, subtotal, total e data.
 * POST acao=anular anula uma venda e devolve as quantidades ao stock.
 */
@WebServlet("/admin/compras")
public class ComprasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final int POR_PAGINA = 10;
    private final RelatorioService relatorios = new RelatorioService();
    private final VendaService vendas = new VendaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ordem = Parametros.texto(req, "ordem");
        boolean desc = !"asc".equals(Parametros.texto(req, "dir"));
        Pagina<Venda> pagina = Pagina.de(relatorios.compras(ordem, desc), Parametros.inteiroOuPadrao(req, "pagina", 1), POR_PAGINA);
        req.setAttribute("pagina", pagina);
        req.setAttribute("vendas", pagina.getItens());
        req.setAttribute("ordem", ordem.isEmpty() ? "data" : ordem);
        req.setAttribute("desc", desc);
        req.setAttribute("faturacao", vendas.faturacaoTotal());
        render(req, resp, "admin/compras");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        executar(req, resp, "/admin/compras", () -> {
            if (!"anular".equals(Parametros.texto(req, "acao"))) {
                return null;
            }
            Venda v = vendas.anularVenda(Parametros.inteiro(req, "id", "id"));
            return "Factura " + v.getNumeroFactura() + " anulada. As quantidades voltaram ao stock.";
        });
    }
}
