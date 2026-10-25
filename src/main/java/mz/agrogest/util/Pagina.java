package mz.agrogest.util;

import java.util.List;

/** Uma página de resultados de uma listagem. */
public final class Pagina<T> {
    public static final int TAMANHO = 15;

    private final List<T> itens;
    private final int numero;
    private final int totalPaginas;
    private final int totalItens;

    private Pagina(List<T> itens, int numero, int totalPaginas, int totalItens) {
        this.itens = itens;
        this.numero = numero;
        this.totalPaginas = totalPaginas;
        this.totalItens = totalItens;
    }

    /** Corta a lista na página pedida (começa em 1; valores fora do intervalo são ajustados). */
    public static <T> Pagina<T> de(List<T> todos, int pedida, int tamanho) {
        int total = todos.size();
        int paginas = Math.max(1, (total + tamanho - 1) / tamanho);
        int n = Math.min(Math.max(1, pedida), paginas);
        int inicio = (n - 1) * tamanho;
        return new Pagina<>(todos.subList(inicio, Math.min(total, inicio + tamanho)), n, paginas, total);
    }

    public static <T> Pagina<T> de(List<T> todos, int pedida) {
        return de(todos, pedida, TAMANHO);
    }

    public List<T> getItens() { return itens; }
    public int getNumero() { return numero; }
    public int getTotalPaginas() { return totalPaginas; }
    public int getTotalItens() { return totalItens; }
    public boolean isTemAnterior() { return numero > 1; }
    public boolean isTemSeguinte() { return numero < totalPaginas; }
}
