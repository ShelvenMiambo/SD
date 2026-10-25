package mz.agrogest.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Categoria;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.util.Parametros;

/** Catálogo de produtos, com filtro por categoria. */
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
        req.setAttribute("produtos", catalogo.produtosPorCategoria(categoria));
        req.setAttribute("fornecedores", catalogo.fornecedoresPorId());
        req.setAttribute("categorias", Categoria.values());
        req.setAttribute("categoriaSel", categoria);
        req.setAttribute("carrinho", carrinho(req));
        render(req, resp, "catalogo");
    }
}
