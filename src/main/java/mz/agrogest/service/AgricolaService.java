package mz.agrogest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import mz.agrogest.model.Campanha;
import mz.agrogest.model.Colheita;
import mz.agrogest.model.Cultivo;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.DataStore.Sequencia;
import mz.agrogest.util.Formatos;
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Validacao;

/** Campanhas agrícolas, cultivos e colheitas. */
public class AgricolaService {
    public static final BigDecimal HECTARES_MAXIMO = new BigDecimal("100000");
    public static final BigDecimal KG_MAXIMO = new BigDecimal("10000000");
    public static final BigDecimal PRECO_KG_MAXIMO = new BigDecimal("1000000");
    /** Uma campanha dura no máximo 2 anos. */
    public static final int DURACAO_MAXIMA_MESES = 24;
    /** As colheitas podem ser registadas até 6 meses depois do fim da campanha (secagem, armazenamento). */
    public static final int MESES_APOS_FIM = 6;

    // ---------- Campanhas ----------

    public List<Campanha> campanhas() {
        return DataStore.copia(DataStore.get().getCampanhas()).stream()
                .sorted(Comparator.comparing(Campanha::getInicio).reversed())
                .toList();
    }

    public Optional<Campanha> campanha(int id) {
        return DataStore.copia(DataStore.get().getCampanhas()).stream().filter(c -> c.getId() == id).findFirst();
    }

    public Campanha criarCampanha(String nome, LocalDate inicio, LocalDate fim) {
        String nomeOk = validarCampanha(nome, inicio, fim);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            exigirNomeCampanhaLivre(nomeOk, 0);
            Campanha c = new Campanha(ds.proximoId(Sequencia.CAMPANHA), nomeOk, inicio, fim);
            ds.getCampanhas().add(c);
            return c;
        }
    }

    /** Edita o nome e as datas. Os cultivos e colheitas já registados têm de continuar dentro das novas datas. */
    public Campanha atualizarCampanha(int id, String nome, LocalDate inicio, LocalDate fim) {
        String nomeOk = validarCampanha(nome, inicio, fim);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = campanha(id).orElseThrow(() -> new NegocioException("Campanha não encontrada."));
            exigirNomeCampanhaLivre(nomeOk, id);
            for (Cultivo cv : cultivosDaCampanha(id)) {
                if (cv.getDataPlantio().isBefore(inicio) || cv.getDataPlantio().isAfter(fim)) {
                    throw new NegocioException("O cultivo \"" + cv.getCultura() + "\" foi plantado a "
                            + Formatos.data(cv.getDataPlantio()) + ", fora das novas datas.", "inicio");
                }
                for (Colheita col : colheitasDoCultivo(cv.getId())) {
                    if (col.getData().isAfter(fim.plusMonths(MESES_APOS_FIM))) {
                        throw new NegocioException("Há uma colheita a " + Formatos.data(col.getData())
                                + " que ficaria fora das novas datas.", "fim");
                    }
                }
            }
            c.setNome(nomeOk);
            c.setInicio(inicio);
            c.setFim(fim);
            return c;
        }
    }

    private static String validarCampanha(String nome, LocalDate inicio, LocalDate fim) {
        String nomeOk = Validacao.texto(nome, 100, "nome", "nome");
        if (inicio == null) {
            throw new NegocioException("A data de início é obrigatória.", "inicio");
        }
        if (fim == null) {
            throw new NegocioException("A data de fim é obrigatória.", "fim");
        }
        if (!fim.isAfter(inicio)) {
            throw new NegocioException("A data de fim tem de ser posterior à data de início.", "fim");
        }
        if (fim.isAfter(inicio.plusMonths(DURACAO_MAXIMA_MESES))) {
            throw new NegocioException("Uma campanha dura no máximo " + DURACAO_MAXIMA_MESES + " meses.", "fim");
        }
        return nomeOk;
    }

    private void exigirNomeCampanhaLivre(String nome, int idProprio) {
        String chave = Validacao.chave(nome);
        if (campanhas().stream().anyMatch(c -> c.getId() != idProprio && Validacao.chave(c.getNome()).equals(chave))) {
            throw new NegocioException("Já existe uma campanha chamada \"" + nome + "\".", "nome");
        }
    }

    /** Outras campanhas cujas datas se sobrepõem às desta (é permitido, mas o admin é avisado). */
    public List<Campanha> sobreposicoes(Campanha c) {
        return campanhas().stream()
                .filter(o -> o.getId() != c.getId())
                .filter(o -> !o.getFim().isBefore(c.getInicio()) && !o.getInicio().isAfter(c.getFim()))
                .toList();
    }

    public void definirFechada(int id, boolean fechada) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = campanha(id).orElseThrow(() -> new NegocioException("Campanha não encontrada."));
            c.setFechada(fechada);
        }
    }

    /** Não é permitido remover campanhas com cultivos associados. */
    public void removerCampanha(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = campanha(id).orElseThrow(() -> new NegocioException("Campanha não encontrada."));
            int n = cultivosDaCampanha(id).size();
            if (n > 0) {
                throw new NegocioException("Não é possível remover \"" + c.getNome() + "\": tem "
                        + n + " cultivo(s) associado(s).");
            }
            ds.getCampanhas().removeIf(x -> x.getId() == id);
        }
    }

    // ---------- Cultivos ----------

    public List<Cultivo> cultivos() {
        return DataStore.copia(DataStore.get().getCultivos());
    }

    public List<Cultivo> cultivosDaCampanha(int campanhaId) {
        return cultivos().stream().filter(c -> c.getCampanhaId() == campanhaId).toList();
    }

    public Optional<Cultivo> cultivo(int id) {
        return cultivos().stream().filter(c -> c.getId() == id).findFirst();
    }

    /**
     * @param cultura uma cultura da lista ou {@link Opcoes#OUTRA}
     * @param outra   o nome escrito pelo utilizador quando escolhe "Outra"
     */
    public Cultivo criarCultivo(int campanhaId, String cultura, String outra, String variedade,
                                BigDecimal hectares, LocalDate dataPlantio) {
        String culturaOk = validarCultura(cultura, outra);
        String variedadeOk = variedade == null ? "" : variedade.trim();
        if (variedadeOk.length() > 60) {
            throw new NegocioException("A variedade tem no máximo 60 caracteres.", "variedade");
        }
        Validacao.positivo(hectares, HECTARES_MAXIMO, "hectares", "hectares");
        if (dataPlantio == null) {
            throw new NegocioException("A data de plantio é obrigatória.", "dataPlantio");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = campanha(campanhaId)
                    .orElseThrow(() -> new NegocioException("Campanha não encontrada.", "campanhaId"));
            if (c.isFechada()) {
                throw new NegocioException("A campanha \"" + c.getNome() + "\" está fechada. Reabra-a para registar cultivos.", "campanhaId");
            }
            if (dataPlantio.isBefore(c.getInicio()) || dataPlantio.isAfter(c.getFim())) {
                throw new NegocioException("A data de plantio tem de estar dentro da campanha ("
                        + Formatos.data(c.getInicio()) + " a " + Formatos.data(c.getFim()) + ").", "dataPlantio");
            }
            Cultivo cv = new Cultivo(ds.proximoId(Sequencia.CULTIVO), campanhaId, culturaOk,
                    variedadeOk, hectares, dataPlantio);
            ds.getCultivos().add(cv);
            return cv;
        }
    }

    private static String validarCultura(String cultura, String outra) {
        if (cultura == null || cultura.isBlank()) {
            throw new NegocioException("Escolha a cultura.", "cultura");
        }
        if (Opcoes.OUTRA.equals(cultura)) {
            String o = outra == null ? "" : outra.trim();
            if (o.length() < 2 || o.length() > 60) {
                throw new NegocioException("Escreva o nome da cultura (2 a 60 caracteres).", "culturaOutra");
            }
            return o;
        }
        if (!Opcoes.VARIEDADES.containsKey(cultura)) {
            throw new NegocioException("Escolha a cultura da lista ou \"Outra\".", "cultura");
        }
        return cultura;
    }

    /** Não é permitido remover cultivos com colheitas associadas. */
    public void removerCultivo(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Cultivo cv = cultivo(id).orElseThrow(() -> new NegocioException("Cultivo não encontrado."));
            int n = colheitasDoCultivo(id).size();
            if (n > 0) {
                throw new NegocioException("Não é possível remover o cultivo \"" + cv.getCultura() + "\": tem "
                        + n + " colheita(s) associada(s).");
            }
            ds.getCultivos().removeIf(x -> x.getId() == id);
        }
    }

    // ---------- Colheitas ----------

    public List<Colheita> colheitas() {
        return DataStore.copia(DataStore.get().getColheitas());
    }

    public List<Colheita> colheitasDoCultivo(int cultivoId) {
        return colheitas().stream()
                .filter(c -> c.getCultivoId() == cultivoId)
                .sorted(Comparator.comparing(Colheita::getData))
                .toList();
    }

    public Colheita criarColheita(int cultivoId, LocalDate data, BigDecimal quantidadeKg, BigDecimal precoKg) {
        if (data == null) {
            throw new NegocioException("A data da colheita é obrigatória.", "data");
        }
        Validacao.positivo(quantidadeKg, KG_MAXIMO, "quantidade (kg)", "quantidadeKg");
        Validacao.positivo(precoKg, PRECO_KG_MAXIMO, "preço por kg", "precoKg");
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Cultivo cv = cultivo(cultivoId).orElseThrow(() -> new NegocioException("Cultivo não encontrado.", "cultivoId"));
            if (data.isBefore(cv.getDataPlantio())) {
                throw new NegocioException("A colheita não pode ser anterior ao plantio ("
                        + Formatos.data(cv.getDataPlantio()) + ").", "data");
            }
            Campanha c = campanha(cv.getCampanhaId()).orElseThrow();
            LocalDate limite = c.getFim().plusMonths(MESES_APOS_FIM);
            if (data.isAfter(limite)) {
                throw new NegocioException("A colheita tem de ser registada até " + Formatos.data(limite)
                        + " (6 meses depois do fim da campanha).", "data");
            }
            if (c.isFechada()) {
                throw new NegocioException("A campanha \"" + c.getNome() + "\" está fechada. Reabra-a para registar colheitas.", "cultivoId");
            }
            Colheita col = new Colheita(ds.proximoId(Sequencia.COLHEITA), cultivoId, data, quantidadeKg, precoKg);
            ds.getColheitas().add(col);
            return col;
        }
    }

    public void removerColheita(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            if (!ds.getColheitas().removeIf(c -> c.getId() == id)) {
                throw new NegocioException("Colheita não encontrada.");
            }
        }
    }

    /** Valor total colhido num cultivo. */
    public BigDecimal valorColhido(int cultivoId) {
        return colheitasDoCultivo(cultivoId).stream()
                .map(Colheita::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
