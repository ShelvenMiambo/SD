package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.service.RelatorioService;
import mz.agrogest.service.VendaService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/** Compras detalhadas: cliente, endereço, telefone, itens, quantidades, preços, subtotal, total e data. */
@WebServlet("/admin/compras")
public class ComprasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final RelatorioService relatorios = new RelatorioService();
    private final VendaService vendas = new VendaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ordem = Parametros.texto(req, "ordem");
        boolean desc = !"asc".equals(Parametros.texto(req, "dir"));
        req.setAttribute("vendas", relatorios.compras(ordem, desc));
        req.setAttribute("ordem", ordem.isEmpty() ? "data" : ordem);
        req.setAttribute("desc", desc);
        req.setAttribute("faturacao", vendas.faturacaoTotal());
        render(req, resp, "admin/compras");
    }
}
