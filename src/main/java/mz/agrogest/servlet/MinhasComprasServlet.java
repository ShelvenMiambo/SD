package mz.agrogest.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Venda;
import mz.agrogest.service.VendaService;

/** Histórico de compras do cliente autenticado. */
@WebServlet("/minhas-compras")
public class MinhasComprasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final VendaService vendas = new VendaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Venda> lista = vendas.vendasDoCliente(utilizador(req).getId());
        req.setAttribute("vendas", lista);
        req.setAttribute("totalGasto", lista.stream().filter(v -> !v.isAnulada()).map(Venda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        render(req, resp, "minhas-compras");
    }
}
