package mz.agrogest.servlet.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.model.Campanha;
import mz.agrogest.model.Colheita;
import mz.agrogest.model.Cultivo;
import mz.agrogest.service.AgricolaService;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/** Registar e remover colheitas por cultivo. O valor total é calculado pelo sistema. */
@WebServlet("/admin/colheitas")
public class ColheitasServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final AgricolaService agricola = new AgricolaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int cultivoId = Parametros.inteiroOuPadrao(req, "cultivoId", 0);
        List<Colheita> colheitas = cultivoId > 0
                ? agricola.colheitasDoCultivo(cultivoId)
                : agricola.colheitas().stream().sorted(Comparator.comparing(Colheita::getData).reversed()).toList();
        req.setAttribute("colheitas", colheitas);
        req.setAttribute("total", colheitas.stream().map(Colheita::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        req.setAttribute("cultivos", agricola.cultivos());
        req.setAttribute("cultivosPorId", agricola.cultivos().stream()
                .collect(Collectors.toMap(Cultivo::getId, Function.identity())));
        req.setAttribute("campanhasPorId", agricola.campanhas().stream()
                .collect(Collectors.toMap(Campanha::getId, Function.identity())));
        req.setAttribute("cultivoId", cultivoId);
        render(req, resp, "admin/colheitas");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = Parametros.texto(req, "acao");
        int filtro = Parametros.inteiroOuPadrao(req, "filtroCultivo", 0);
        String destino = filtro > 0 ? "/admin/colheitas?cultivoId=" + filtro : "/admin/colheitas";
        executar(req, resp, destino, () -> switch (acao) {
            case "criar" -> {
                Colheita c = agricola.criarColheita(
                        Parametros.inteiro(req, "cultivoId", "cultivo"),
                        Parametros.data(req, "data", "data"),
                        Parametros.decimal(req, "quantidadeKg", "quantidade (kg)"),
                        Parametros.decimal(req, "precoKg", "preço por kg"));
                yield "Colheita registada: valor total " + mz.agrogest.util.Formatos.mt(c.getValorTotal()) + ".";
            }
            case "remover" -> {
                agricola.removerColheita(Parametros.inteiro(req, "id", "id"));
                yield "Colheita removida.";
            }
            default -> null;
        });
    }
}
