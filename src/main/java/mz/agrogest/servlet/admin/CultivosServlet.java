package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Campanha;
import mz.agrogest.model.Cultivo;
import mz.agrogest.service.AgricolaService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Parametros;

/** Registar e remover cultivos por campanha. */
@WebServlet("/admin/cultivos")
public class CultivosServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final AgricolaService agricola = new AgricolaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int campanhaId = Parametros.inteiroOuPadrao(req, "campanhaId", 0);
        List<Cultivo> cultivos = campanhaId > 0 ? agricola.cultivosDaCampanha(campanhaId) : agricola.cultivos();

        Map<Integer, BigDecimal> valorColhido = new HashMap<>();
        Map<Integer, Integer> nColheitas = new HashMap<>();
        for (Cultivo c : cultivos) {
            valorColhido.put(c.getId(), agricola.valorColhido(c.getId()));
            nColheitas.put(c.getId(), agricola.colheitasDoCultivo(c.getId()).size());
        }
        req.setAttribute("cultivos", cultivos);
        req.setAttribute("campanhas", agricola.campanhas());
        req.setAttribute("campanhasPorId", agricola.campanhas().stream()
                .collect(Collectors.toMap(Campanha::getId, Function.identity())));
        req.setAttribute("campanhaId", campanhaId);
        req.setAttribute("culturas", Opcoes.getCulturas());
        req.setAttribute("variedadesJson", Opcoes.variedadesJson());
        req.setAttribute("outra", Opcoes.OUTRA);
        req.setAttribute("valorColhido", valorColhido);
        req.setAttribute("nColheitas", nColheitas);
        render(req, resp, "admin/cultivos");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        int filtro = Parametros.inteiroOuPadrao(req, "filtroCampanha", 0);
        String destino = filtro > 0 ? "/admin/cultivos?campanhaId=" + filtro : "/admin/cultivos";
        executar(req, resp, destino, () -> switch (acao) {
            case "criar" -> {
                Cultivo c = agricola.criarCultivo(
                        Parametros.inteiro(req, "campanhaId", "campanha"),
                        Parametros.texto(req, "cultura"),
                        Parametros.texto(req, "culturaOutra"),
                        Parametros.texto(req, "variedade"),
                        Parametros.decimal(req, "hectares", "hectares"),
                        Parametros.data(req, "dataPlantio", "data de plantio"));
                yield "Cultivo \"" + c.getCultura() + "\" registado.";
            }
            case "remover" -> {
                agricola.removerCultivo(Parametros.inteiro(req, "id", "id"));
                yield "Cultivo removido.";
            }
            default -> null;
        });
    }
}
