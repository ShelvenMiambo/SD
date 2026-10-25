package mz.agrogest.servlet.admin;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mz.agrogest.listener.AppListener;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.Persistencia;
import mz.agrogest.service.NegocioException;
import mz.agrogest.servlet.BaseServlet;
import mz.agrogest.util.Parametros;

/** Endpoint de gravação e eliminação manual do ficheiro de dados. */
@WebServlet("/admin/persistencia")
public class PersistenciaServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Persistencia p = persistencia();
        req.setAttribute("ficheiro", p.getFicheiro().toString());
        req.setAttribute("existe", p.existe());
        req.setAttribute("ultimaGravacao", p.ultimaGravacao());
        req.setAttribute("tamanhoKb", (p.tamanhoBytes() + 1023) / 1024);
        render(req, resp, "admin/persistencia");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Persistencia p = persistencia();
        String acao = Parametros.texto(req, "acao");
        executar(req, resp, "/admin/persistencia", () -> {
            try {
                return switch (acao) {
                    case "gravar" -> {
                        p.gravar(DataStore.get());
                        yield "Estado gravado em " + p.getFicheiro() + ".";
                    }
                    case "eliminar" -> p.eliminar()
                            ? "Ficheiro de dados eliminado. Os dados em memória mantêm-se até o Tomcat encerrar."
                            : "Não havia ficheiro gravado.";
                    default -> null;
                };
            } catch (IOException e) {
                throw new NegocioException("Falha na operação: " + e.getMessage());
            }
        });
    }

    private Persistencia persistencia() {
        return (Persistencia) getServletContext().getAttribute(AppListener.ATTR_PERSISTENCIA);
    }
}
