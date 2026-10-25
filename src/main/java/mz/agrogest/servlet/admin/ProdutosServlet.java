package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Categoria;
import mz.agrogest.model.Produto;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/** Criar, listar, editar, repor stock e remover produtos. */
@WebServlet("/admin/produtos")
public class ProdutosServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogo = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int editar = Parametros.inteiroOuPadrao(req, "editar", -1);
        if (editar > 0) {
            catalogo.produto(editar).ifPresent(p -> req.setAttribute("produto", p));
        }
        req.setAttribute("produtos", catalogo.produtosPorCategoria(null));
        req.setAttribute("fornecedores", catalogo.fornecedores());
        req.setAttribute("fornecedoresPorId", catalogo.fornecedoresPorId());
        req.setAttribute("categorias", Categoria.values());
        render(req, resp, "admin/produtos");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        executar(req, resp, "/admin/produtos", () -> switch (acao) {
            case "criar" -> {
                Produto p = catalogo.criarProduto(lerFormulario(req));
                yield "Produto \"" + p.getNome() + "\" criado.";
            }
            case "atualizar" -> {
                Produto p = catalogo.atualizarProduto(Parametros.inteiro(req, "id", "id"), lerFormulario(req));
                yield "Produto \"" + p.getNome() + "\" atualizado.";
            }
            case "repor" -> {
                int qtd = Parametros.inteiro(req, "quantidade", "quantidade a repor");
                Produto p = catalogo.reporStock(Parametros.inteiro(req, "id", "id"), qtd);
                yield "Stock de \"" + p.getNome() + "\" reposto: agora " + p.getQuantidade() + " " + p.getUnidade() + ".";
            }
            case "remover" -> {
                catalogo.removerProduto(Parametros.inteiro(req, "id", "id"));
                yield "Produto removido.";
            }
            default -> null;
        });
    }

    private static Produto lerFormulario(HttpServletRequest req) {
        Produto p = new Produto();
        p.setNome(Parametros.obrigatorio(req, "nome", "nome"));
        p.setCategoria(Parametros.enumeracao(req, "categoria", Categoria.class, "categoria"));
        p.setFornecedorId(Parametros.inteiro(req, "fornecedorId", "fornecedor"));
        p.setPreco(Parametros.decimal(req, "preco", "preço"));
        p.setQuantidade(Parametros.inteiro(req, "quantidade", "quantidade"));
        p.setUnidade(Parametros.obrigatorio(req, "unidade", "unidade"));
        p.setDescricao(Parametros.texto(req, "descricao"));
        p.setFotoUrl(Parametros.texto(req, "fotoUrl"));
        p.setValidade(Parametros.dataOpcional(req, "validade", "validade"));
        return p;
    }
}
