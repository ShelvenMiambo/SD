package mz.agrogest.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.Categoria;
import mz.agrogest.model.Produto;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.service.VendaService;
import mz.agrogest.util.Parametros;

/** Catálogo de produtos, com filtro por categoria e pesquisa por nome. */
@WebServlet("/catalogo")
public class CatalogoServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogo = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Categoria categoria = null;
        String cat = Parametros.texto(req, "categoria");
        if (!cat.isEmpty()) {
            try {
                categoria = Categoria.valueOf(cat);
            } catch (IllegalArgumentException e) {
                // Categoria desconhecida: mostra todos os produtos.
            }
        }
        String q = Parametros.texto(req, "q");
        if (q.length() > 60) {
            q = q.substring(0, 60);
        }
        List<Produto> produtos = catalogo.pesquisar(categoria, q);
        boolean admin = utilizador(req).isAdmin();
        Carrinho carrinho = admin ? null : carrinho(req);

        // Quanto ainda se pode adicionar de cada produto (stock − o que já está no carrinho).
        Map<Integer, Integer> disponivel = new HashMap<>();
        for (Produto p : produtos) {
            disponivel.put(p.getId(), VendaService.disponivelParaAdicionar(p, carrinho));
        }
        req.setAttribute("produtos", produtos);
        req.setAttribute("disponivel", disponivel);
        req.setAttribute("fornecedores", catalogo.fornecedoresPorId());
        req.setAttribute("categorias", Categoria.values());
        req.setAttribute("categoriaSel", categoria);
        req.setAttribute("q", q);
        req.setAttribute("carrinho", carrinho);
        req.setAttribute("modoAdmin", admin);
        render(req, resp, "catalogo");
    }
}
