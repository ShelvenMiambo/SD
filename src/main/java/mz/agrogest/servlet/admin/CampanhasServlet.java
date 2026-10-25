package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Campanha;
import mz.agrogest.model.Cultivo;
import mz.agrogest.service.AgricolaService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/** Criar, fechar, reabrir e remover campanhas agrícolas. */
@WebServlet("/admin/campanhas")
public class CampanhasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final AgricolaService agricola = new AgricolaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<Integer, Long> nCultivos = agricola.cultivos().stream()
                .collect(Collectors.groupingBy(Cultivo::getCampanhaId, Collectors.counting()));
        req.setAttribute("campanhas", agricola.campanhas());
        req.setAttribute("nCultivos", nCultivos);
        render(req, resp, "admin/campanhas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        executar(req, resp, "/admin/campanhas", () -> switch (acao) {
            case "criar" -> {
                Campanha c = agricola.criarCampanha(
                        Parametros.obrigatorio(req, "nome", "nome"),
                        Parametros.data(req, "inicio", "data de início"),
                        Parametros.data(req, "fim", "data de fim"));
                yield "Campanha \"" + c.getNome() + "\" criada.";
            }
            case "fechar" -> {
                agricola.definirFechada(Parametros.inteiro(req, "id", "id"), true);
                yield "Campanha fechada.";
            }
            case "reabrir" -> {
                agricola.definirFechada(Parametros.inteiro(req, "id", "id"), false);
                yield "Campanha reaberta.";
            }
            case "remover" -> {
                agricola.removerCampanha(Parametros.inteiro(req, "id", "id"));
                yield "Campanha removida.";
            }
            default -> null;
        });
    }
}
