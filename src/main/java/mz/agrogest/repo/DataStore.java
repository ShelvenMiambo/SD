package mz.agrogest.repo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import mz.agrogest.model.Campanha;
import mz.agrogest.model.Colheita;
import mz.agrogest.model.Cultivo;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.Produto;
import mz.agrogest.model.Utilizador;
import mz.agrogest.model.Venda;

/**
 * Armazenamento em memória de todo o estado da aplicação.
 *
 * <p>As listas são {@code Collections.synchronizedList}. Qualquer operação de escrita que
 * envolva mais de uma lista ou uma regra de negócio deve ser feita dentro de
 * {@code synchronized (DataStore.get())}, que é o "trinco" global usado pelos serviços
 * e pela persistência.</p>
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Sequências de IDs, uma por entidade. */
    public enum Sequencia {
        UTILIZADOR, FORNECEDOR, PRODUTO, CAMPANHA, CULTIVO, COLHEITA, VENDA
    }

    private static volatile DataStore instancia = new DataStore();

    private final List<Utilizador> utilizadores = Collections.synchronizedList(new ArrayList<>());
    private final List<Fornecedor> fornecedores = Collections.synchronizedList(new ArrayList<>());
    private final List<Produto> produtos = Collections.synchronizedList(new ArrayList<>());
    private final List<Campanha> campanhas = Collections.synchronizedList(new ArrayList<>());
    private final List<Cultivo> cultivos = Collections.synchronizedList(new ArrayList<>());
    private final List<Colheita> colheitas = Collections.synchronizedList(new ArrayList<>());
    private final List<Venda> vendas = Collections.synchronizedList(new ArrayList<>());

    private final Map<Sequencia, AtomicInteger> sequencias = new EnumMap<>(Sequencia.class);
    /** Numeração das facturas por ano: FT-&lt;ano&gt;-&lt;n&gt;. */
    private final Map<Integer, AtomicInteger> sequenciaFactura = new ConcurrentHashMap<>();

    DataStore() {
        for (Sequencia s : Sequencia.values()) {
            sequencias.put(s, new AtomicInteger());
        }
    }

    public static DataStore get() {
        return instancia;
    }

    /** Substitui o estado inteiro (usado ao restaurar do ficheiro e nos testes). */
    public static void substituir(DataStore novo) {
        instancia = novo;
    }

    /** Cria um estado vazio novo (usado nos testes). */
    public static DataStore novoVazio() {
        return new DataStore();
    }

    public int proximoId(Sequencia s) {
        return sequencias.get(s).incrementAndGet();
    }

    public int proximoNumeroFactura(int ano) {
        return sequenciaFactura.computeIfAbsent(ano, a -> new AtomicInteger()).incrementAndGet();
    }

    /** Cópia segura de uma lista sincronizada, para iterar sem bloquear. */
    public static <T> List<T> copia(List<T> lista) {
        synchronized (lista) {
            return new ArrayList<>(lista);
        }
    }

    public List<Utilizador> getUtilizadores() { return utilizadores; }
    public List<Fornecedor> getFornecedores() { return fornecedores; }
    public List<Produto> getProdutos() { return produtos; }
    public List<Campanha> getCampanhas() { return campanhas; }
    public List<Cultivo> getCultivos() { return cultivos; }
    public List<Colheita> getColheitas() { return colheitas; }
    public List<Venda> getVendas() { return vendas; }
}
