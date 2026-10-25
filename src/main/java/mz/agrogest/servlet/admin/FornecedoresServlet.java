package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.TipoFornecedor;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Parametros;

/** Criar, editar, listar e remover fornecedores. */
@WebServlet("/admin/fornecedores")
public class FornecedoresServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogo = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int editar = Parametros.inteiroOuPadrao(req, "editar", -1);
        if (editar > 0) {
            catalogo.fornecedor(editar).ifPresent(f -> req.setAttribute("fornecedor", f));
        }
        req.setAttribute("fornecedores", catalogo.fornecedores());
        req.setAttribute("tipos", TipoFornecedor.values());
        req.setAttribute("provincias", Opcoes.PROVINCIAS);
        render(req, resp, "admin/fornecedores");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        int id = Parametros.inteiroOuPadrao(req, "id", 0);
        String destino = "atualizar".equals(acao) ? "/admin/fornecedores?editar=" + id : "/admin/fornecedores";
        executar(req, resp, destino, () -> switch (acao) {
            case "criar" -> {
                Fornecedor f = catalogo.criarFornecedor(
                        Parametros.texto(req, "nome"),
                        Parametros.enumeracao(req, "tipo", TipoFornecedor.class, "tipo"),
                        Parametros.texto(req, "contacto"),
                        Parametros.texto(req, "provincia"));
                yield "Fornecedor \"" + f.getNome() + "\" criado.";
            }
            case "atualizar" -> {
                Fornecedor f = catalogo.atualizarFornecedor(id,
                        Parametros.texto(req, "nome"),
                        Parametros.enumeracao(req, "tipo", TipoFornecedor.class, "tipo"),
                        Parametros.texto(req, "contacto"),
                        Parametros.texto(req, "provincia"));
                yield "Fornecedor \"" + f.getNome() + "\" actualizado.";
            }
            case "remover" -> {
                catalogo.removerFornecedor(Parametros.inteiro(req, "id", "id"));
                yield "Fornecedor removido.";
            }
            default -> null;
        });
    }
}
