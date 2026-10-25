package mz.agrogest.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.LinhaCarrinho;
import mz.agrogest.model.Venda;
import mz.agrogest.service.NegocioException;
import mz.agrogest.service.VendaService;
import mz.agrogest.util.Flash;

/** GET: página de confirmação da compra. POST: confirma, desconta o stock e emite a factura. */
@WebServlet("/compra")
public class CompraServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final VendaService vendas = new VendaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Carrinho carrinho = carrinho(req);
        List<LinhaCarrinho> linhas = vendas.linhas(carrinho);
        if (linhas.isEmpty()) {
            Flash.erro(req, "O carrinho está vazio.");
            redirect(req, resp, "/catalogo");
            return;
        }
        BigDecimal subtotal = VendaService.subtotal(linhas);
        BigDecimal iva = VendaService.iva(subtotal);
        req.setAttribute("linhas", linhas);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("iva", iva);
        req.setAttribute("total", subtotal.add(iva));
        req.setAttribute("stockOk", linhas.stream().allMatch(LinhaCarrinho::isStockSuficiente));
        req.setAttribute("cliente", utilizador(req));
        render(req, resp, "compra");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Venda v = vendas.finalizarCompra(utilizador(req), carrinho(req));
            Flash.ok(req, "Compra confirmada. Factura " + v.getNumeroFactura() + " emitida.");
            redirect(req, resp, "/factura?id=" + v.getId());
        } catch (NegocioException e) {
            Flash.erro(req, e.getMessage());
            redirect(req, resp, "/carrinho");
        }
    }
}
