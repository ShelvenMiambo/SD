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
import mz.agrogest.util.Parametros;

@WebServlet("/admin/fornecedores")
public class FornecedoresServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogoService catalogo = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("fornecedores", catalogo.fornecedores());
        req.setAttribute("tipos", TipoFornecedor.values());
        render(req, resp, "admin/fornecedores");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        executar(req, resp, "/admin/fornecedores", () -> switch (acao) {
            case "criar" -> {
                Fornecedor f = catalogo.criarFornecedor(
                        Parametros.obrigatorio(req, "nome", "nome"),
                        Parametros.enumeracao(req, "tipo", TipoFornecedor.class, "tipo"),
                        Parametros.obrigatorio(req, "contacto", "contacto"),
                        Parametros.obrigatorio(req, "provincia", "província"));
                yield "Fornecedor \"" + f.getNome() + "\" criado.";
            }
            case "remover" -> {
                catalogo.removerFornecedor(Parametros.inteiro(req, "id", "id"));
                yield "Fornecedor removido.";
            }
            default -> null;
        });
    }
}
