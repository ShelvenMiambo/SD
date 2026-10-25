package mz.agrogest.servlet;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Venda;
import mz.agrogest.service.VendaService;
import mz.agrogest.util.Parametros;

/** Mostra uma factura. RN-c: o cliente só vê as suas; o admin vê todas. */
@WebServlet("/factura")
public class FacturaServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final VendaService vendas = new VendaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<Venda> v = vendas.venda(Parametros.inteiroOuPadrao(req, "id", -1));
        if (v.isEmpty()) {
            naoEncontrado(resp);
            return;
        }
        if (!vendas.podeVer(utilizador(req), v.get())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Esta factura não lhe pertence.");
            return;
        }
        req.setAttribute("venda", v.get());
        req.setAttribute("taxaIva", 16);
        render(req, resp, "factura");
    }
}
