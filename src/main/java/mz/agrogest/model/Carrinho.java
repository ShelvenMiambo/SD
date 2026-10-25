package mz.agrogest.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Carrinho de compras guardado na sessão HTTP: produtoId → quantidade. */
public class Carrinho implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, Integer> itens = new LinkedHashMap<>();

    public synchronized void definir(int produtoId, int quantidade) {
        if (quantidade <= 0) {
            itens.remove(produtoId);
        } else {
            itens.put(produtoId, quantidade);
        }
    }

    public synchronized int quantidadeDe(int produtoId) {
        return itens.getOrDefault(produtoId, 0);
    }

    public synchronized void remover(int produtoId) {
        itens.remove(produtoId);
    }

    public synchronized void esvaziar() {
        itens.clear();
    }

    public synchronized boolean isVazio() {
        return itens.isEmpty();
    }

    public synchronized int getTotalLinhas() {
        return itens.size();
    }

    /** Cópia imutável dos itens. */
    public synchronized Map<Integer, Integer> getItens() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(itens));
    }
}
