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
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Parametros;

/** Criar, listar, pesquisar, editar, repor stock e remover produtos. */
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
        String q = Parametros.texto(req, "q");
        req.setAttribute("q", q);
        req.setAttribute("produtos", catalogo.pesquisar(null, q));
        req.setAttribute("fornecedores", catalogo.fornecedores());
        req.setAttribute("fornecedoresPorId", catalogo.fornecedoresPorId());
        req.setAttribute("categorias", Categoria.values());
        req.setAttribute("unidades", Opcoes.UNIDADES);
        req.setAttribute("stockMaximo", CatalogoService.STOCK_MAXIMO);
        req.setAttribute("hoje", java.time.LocalDate.now());
        render(req, resp, "admin/produtos");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        int id = Parametros.inteiroOuPadrao(req, "id", 0);
        String destino = "atualizar".equals(acao) ? "/admin/produtos?editar=" + id : "/admin/produtos";
        executar(req, resp, destino, () -> switch (acao) {
            case "criar" -> {
                Produto p = catalogo.criarProduto(lerFormulario(req));
                yield "Produto \"" + p.getNome() + "\" criado.";
            }
            case "atualizar" -> {
                Produto p = catalogo.atualizarProduto(id, lerFormulario(req));
                yield "Produto \"" + p.getNome() + "\" atualizado.";
            }
            case "repor" -> {
                // Os botões rápidos (+10, +50, +100) enviam "rapido"; senão usa o campo "quantidade".
                String rapido = Parametros.texto(req, "rapido");
                int qtd = rapido.isEmpty()
                        ? Parametros.inteiro(req, "quantidade", "quantidade a repor")
                        : Parametros.inteiro(req, "rapido", "quantidade a repor");
                Produto p = catalogo.reporStock(id, qtd);
                yield "Stock de \"" + p.getNome() + "\" reposto (+" + qtd + "): agora " + p.getQuantidade() + " " + p.getUnidade() + ".";
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
