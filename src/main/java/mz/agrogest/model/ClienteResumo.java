package mz.agrogest.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Resumo de compras de um cliente, usado nos relatórios. */
public class ClienteResumo {
    private final Utilizador cliente;
    private final int numeroCompras;
    private final BigDecimal totalGasto;
    private final LocalDateTime ultimaCompra;

    public ClienteResumo(Utilizador cliente, int numeroCompras, BigDecimal totalGasto, LocalDateTime ultimaCompra) {
        this.cliente = cliente;
        this.numeroCompras = numeroCompras;
        this.totalGasto = totalGasto;
        this.ultimaCompra = ultimaCompra;
    }

    public Utilizador getCliente() { return cliente; }
    public int getNumeroCompras() { return numeroCompras; }
    public BigDecimal getTotalGasto() { return totalGasto; }
    public LocalDateTime getUltimaCompra() { return ultimaCompra; }
}
