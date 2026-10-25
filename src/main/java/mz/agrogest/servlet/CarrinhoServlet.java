package mz.agrogest.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.LinhaCarrinho;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.service.VendaService;
import mz.agrogest.util.Formatos;
import mz.agrogest.util.Parametros;

/**
 * Carrinho em sessão.
 * GET /carrinho mostra; POST /carrinho/adicionar | atualizar | remover | esvaziar | ajustar altera.
 */
@WebServlet(urlPatterns = {"/carrinho", "/carrinho/*"})
public class CarrinhoServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final VendaService vendas = new VendaService();
    private final CatalogoService catalogo = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<LinhaCarrinho> linhas = vendas.linhas(carrinho(req));
        BigDecimal subtotal = VendaService.subtotal(linhas);
        BigDecimal iva = VendaService.iva(subtotal);
        req.setAttribute("linhas", linhas);
        req.setAttribute("subtotal", subtotal);
        req.setAttribute("iva", iva);
        req.setAttribute("total", subtotal.add(iva));
        req.setAttribute("stockOk", linhasValidas(linhas));
        req.setAttribute("maximoLinha", VendaService.MAXIMO_POR_LINHA);
        render(req, resp, "carrinho");
    }

    /** Todas as linhas têm stock e estão dentro da validade. */
    static boolean linhasValidas(List<LinhaCarrinho> linhas) {
        return linhas.stream().allMatch(l -> l.isStockSuficiente() && !Formatos.expirado(l.getProduto().getValidade()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = req.getPathInfo() == null ? "" : req.getPathInfo();
        Carrinho carrinho = carrinho(req);
        switch (acao) {
            case "/adicionar" -> {
                // Volta ao catálogo, mantendo o filtro de categoria e a pesquisa.
                String categoria = Parametros.texto(req, "categoria");
                String q = Parametros.texto(req, "q");
                StringBuilder destino = new StringBuilder("/catalogo?");
                if (categoria.matches("[A-Z]+")) {
                    destino.append("categoria=").append(categoria).append("&");
                }
                if (!q.isEmpty()) {
                    destino.append("q=").append(URLEncoder.encode(q, StandardCharsets.UTF_8)).append("&");
                }
                destino.append("#p").append(Parametros.inteiroOuPadrao(req, "produtoId", 0));
                executar(req, resp, destino.toString(), () -> {
                    int id = Parametros.inteiro(req, "produtoId", "produto");
                    int qtd = Parametros.inteiro(req, "quantidade", "quantidade");
                    vendas.adicionarAoCarrinho(carrinho, id, qtd);
                    String nome = catalogo.produto(id).map(p -> p.getNome()).orElse("Produto");
                    return nome + " adicionado ao carrinho (" + qtd + ").";
                });
            }
            case "/atualizar" -> executar(req, resp, "/carrinho", () -> {
                vendas.atualizarCarrinho(carrinho,
                        Parametros.inteiro(req, "produtoId", "produto"),
                        Parametros.inteiro(req, "quantidade", "quantidade"));
                return "Carrinho atualizado.";
            });
            case "/remover" -> executar(req, resp, "/carrinho", () -> {
                carrinho.remover(Parametros.inteiro(req, "produtoId", "produto"));
                return "Item removido do carrinho.";
            });
            case "/esvaziar" -> executar(req, resp, "/carrinho", () -> {
                carrinho.esvaziar();
                return "Carrinho esvaziado.";
            });
            case "/ajustar" -> executar(req, resp, "/carrinho", () -> {
                int n = vendas.ajustarAoStock(carrinho);
                return n == 0 ? "O carrinho já estava de acordo com o stock."
                        : n + " linha(s) ajustada(s) ao stock disponível.";
            });
            default -> naoEncontrado(resp);
        }
    }
}
