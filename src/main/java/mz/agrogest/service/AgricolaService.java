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

/** Campanhas agrícolas, cultivos e colheitas. */
public class AgricolaService {

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
        if (nome == null || nome.isBlank()) {
            throw new NegocioException("O nome da campanha é obrigatório.");
        }
        if (inicio == null || fim == null) {
            throw new NegocioException("As datas de início e fim são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new NegocioException("A data de fim não pode ser anterior à data de início.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = new Campanha(ds.proximoId(Sequencia.CAMPANHA), nome.trim(), inicio, fim);
            ds.getCampanhas().add(c);
            return c;
        }
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

    public Cultivo criarCultivo(int campanhaId, String cultura, String variedade, BigDecimal hectares,
                                LocalDate dataPlantio) {
        if (cultura == null || cultura.isBlank()) {
            throw new NegocioException("A cultura é obrigatória.");
        }
        if (hectares == null || hectares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("Os hectares devem ser maiores que zero.");
        }
        if (dataPlantio == null) {
            throw new NegocioException("A data de plantio é obrigatória.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Campanha c = campanha(campanhaId).orElseThrow(() -> new NegocioException("Campanha não encontrada."));
            if (c.isFechada()) {
                throw new NegocioException("A campanha \"" + c.getNome() + "\" está fechada. Reabra-a para registar cultivos.");
            }
            Cultivo cv = new Cultivo(ds.proximoId(Sequencia.CULTIVO), campanhaId, cultura.trim(),
                    variedade == null ? "" : variedade.trim(), hectares, dataPlantio);
            ds.getCultivos().add(cv);
            return cv;
        }
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
            throw new NegocioException("A data da colheita é obrigatória.");
        }
        if (quantidadeKg == null || quantidadeKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("A quantidade (kg) deve ser maior que zero.");
        }
        if (precoKg == null || precoKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O preço por kg deve ser maior que zero.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Cultivo cv = cultivo(cultivoId).orElseThrow(() -> new NegocioException("Cultivo não encontrado."));
            if (data.isBefore(cv.getDataPlantio())) {
                throw new NegocioException("A colheita não pode ser anterior ao plantio ("
                        + Formatos.data(cv.getDataPlantio()) + ").");
            }
            Campanha c = campanha(cv.getCampanhaId()).orElseThrow();
            if (c.isFechada()) {
                throw new NegocioException("A campanha \"" + c.getNome() + "\" está fechada. Reabra-a para registar colheitas.");
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
