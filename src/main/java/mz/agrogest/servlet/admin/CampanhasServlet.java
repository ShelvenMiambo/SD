package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.util.List;
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

/** Criar, editar, fechar, reabrir e remover campanhas agrícolas. */
@WebServlet("/admin/campanhas")
public class CampanhasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final AgricolaService agricola = new AgricolaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int editar = Parametros.inteiroOuPadrao(req, "editar", -1);
        if (editar > 0) {
            agricola.campanha(editar).ifPresent(c -> req.setAttribute("campanha", c));
        }
        Map<Integer, Long> nCultivos = agricola.cultivos().stream()
                .collect(Collectors.groupingBy(Cultivo::getCampanhaId, Collectors.counting()));
        req.setAttribute("campanhas", agricola.campanhas());
        req.setAttribute("nCultivos", nCultivos);
        render(req, resp, "admin/campanhas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        int id = Parametros.inteiroOuPadrao(req, "id", 0);
        String destino = "atualizar".equals(acao) ? "/admin/campanhas?editar=" + id : "/admin/campanhas";
        executar(req, resp, destino, () -> switch (acao) {
            case "criar" -> {
                Campanha c = agricola.criarCampanha(
                        Parametros.texto(req, "nome"),
                        Parametros.data(req, "inicio", "data de início"),
                        Parametros.data(req, "fim", "data de fim"));
                yield "Campanha \"" + c.getNome() + "\" criada." + avisoSobreposicao(c);
            }
            case "atualizar" -> {
                Campanha c = agricola.atualizarCampanha(id,
                        Parametros.texto(req, "nome"),
                        Parametros.data(req, "inicio", "data de início"),
                        Parametros.data(req, "fim", "data de fim"));
                yield "Campanha \"" + c.getNome() + "\" actualizada." + avisoSobreposicao(c);
            }
            case "fechar" -> {
                agricola.definirFechada(id, true);
                yield "Campanha fechada.";
            }
            case "reabrir" -> {
                agricola.definirFechada(id, false);
                yield "Campanha reaberta.";
            }
            case "remover" -> {
                agricola.removerCampanha(Parametros.inteiro(req, "id", "id"));
                yield "Campanha removida.";
            }
            default -> null;
        });
    }

    private String avisoSobreposicao(Campanha c) {
        List<Campanha> outras = agricola.sobreposicoes(c);
        if (outras.isEmpty()) {
            return "";
        }
        return " Atenção: as datas sobrepõem-se a "
                + outras.stream().map(o -> "\"" + o.getNome() + "\"").collect(Collectors.joining(", ")) + ".";
    }
}
