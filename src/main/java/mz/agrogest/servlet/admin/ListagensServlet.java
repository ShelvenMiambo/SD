package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Utilizador;
import mz.agrogest.service.CatalogoService;
import mz.agrogest.service.RelatorioService;
import mz.agrogest.service.UtilizadorService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/**
 * Listagens ordenáveis: produtos, fornecedores, clientes, armazém (stock &gt; 0) e compras.
 * Parâmetros: {@code tipo}, {@code ordem} e {@code dir} (asc|desc).
 */
@WebServlet("/admin/listagens")
public class ListagensServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private static final Set<String> TIPOS = Set.of("produtos", "fornecedores", "clientes", "armazem", "compras");

    private final RelatorioService relatorios = new RelatorioService();
    private final CatalogoService catalogo = new CatalogoService();
    private final UtilizadorService utilizadores = new UtilizadorService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tipo = Parametros.texto(req, "tipo");
        if (!TIPOS.contains(tipo)) {
            tipo = "produtos";
        }
        String ordem = Parametros.texto(req, "ordem");
        if (ordem.isEmpty()) {
            ordem = "compras".equals(tipo) ? "data" : "nome";
        }
        boolean desc = "desc".equals(Parametros.texto(req, "dir"));

        switch (tipo) {
            case "fornecedores" -> req.setAttribute("lista", relatorios.fornecedores(ordem, desc));
            case "clientes" -> req.setAttribute("lista", relatorios.clientes(ordem, desc));
            case "armazem" -> req.setAttribute("lista", relatorios.armazem(ordem, desc));
            case "compras" -> req.setAttribute("lista", relatorios.compras(ordem, desc));
            default -> req.setAttribute("lista", relatorios.produtos(ordem, desc));
        }
        req.setAttribute("tipo", tipo);
        req.setAttribute("ordem", ordem);
        req.setAttribute("desc", desc);
        req.setAttribute("fornecedoresPorId", catalogo.fornecedoresPorId());
        render(req, resp, "admin/listagens");
    }

    /** Ativar / desativar a conta de um cliente. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        executar(req, resp, "/admin/listagens?tipo=clientes", () -> {
            Utilizador u = utilizadores.alternarAtivo(Parametros.inteiro(req, "id", "id"));
            return "Conta de " + u.getNome() + (u.isAtivo() ? " ativada." : " desativada.");
        });
    }
}
