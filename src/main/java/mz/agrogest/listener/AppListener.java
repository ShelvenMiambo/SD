package mz.agrogest.listener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.Persistencia;
import mz.agrogest.service.DadosIniciais;

/**
 * Ciclo de vida da aplicação:
 * no arranque restaura o estado do ficheiro (ou carrega os dados de exemplo);
 * ao encerrar o Tomcat grava o estado automaticamente.
 */
@WebListener
public class AppListener implements ServletContextListener {
    public static final String ATTR_PERSISTENCIA = "persistencia";
    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        Persistencia persistencia = new Persistencia(ficheiroDeDados(ctx));
        ctx.setAttribute(ATTR_PERSISTENCIA, persistencia);

        DataStore restaurado = persistencia.carregar();
        if (restaurado != null) {
            DataStore.substituir(restaurado);
            LOG.info("AgroGest: estado restaurado de " + persistencia.getFicheiro());
        } else {
            DataStore.substituir(DataStore.novoVazio());
            DadosIniciais.carregar();
            LOG.info("AgroGest: sem ficheiro de dados válido; dados de exemplo carregados.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Persistencia persistencia = (Persistencia) sce.getServletContext().getAttribute(ATTR_PERSISTENCIA);
        if (persistencia == null) {
            return;
        }
        try {
            persistencia.gravar(DataStore.get());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "AgroGest: falha ao gravar o estado no encerramento", e);
        }
    }

    /**
     * Localização do ficheiro, por ordem de prioridade:
     * propriedade de sistema {@code agrogest.dados}, parâmetro de contexto {@code agrogest.dados},
     * {@code ${catalina.base}/data/agrogest.ser} e, por fim, {@code ~/.agrogest/agrogest.ser}.
     */
    private static Path ficheiroDeDados(ServletContext ctx) {
        String prop = System.getProperty("agrogest.dados");
        if (prop != null && !prop.isBlank()) {
            return Path.of(prop);
        }
        String param = ctx.getInitParameter("agrogest.dados");
        if (param != null && !param.isBlank()) {
            return Path.of(param);
        }
        String catalina = System.getProperty("catalina.base");
        if (catalina != null) {
            return Path.of(catalina, "data", "agrogest.ser");
        }
        return Path.of(System.getProperty("user.home"), ".agrogest", "agrogest.ser");
    }
}
