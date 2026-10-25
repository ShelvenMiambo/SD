package mz.agrogest.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class Colheita implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int cultivoId;
    private LocalDate data;
    private BigDecimal quantidadeKg;
    private BigDecimal precoKg;

    public Colheita() {
    }

    public Colheita(int id, int cultivoId, LocalDate data, BigDecimal quantidadeKg, BigDecimal precoKg) {
        this.id = id;
        this.cultivoId = cultivoId;
        this.data = data;
        this.quantidadeKg = quantidadeKg;
        this.precoKg = precoKg;
    }

    /** Valor total = quantidade (kg) × preço por kg. */
    public BigDecimal getValorTotal() {
        return quantidadeKg.multiply(precoKg).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCultivoId() { return cultivoId; }
    public void setCultivoId(int cultivoId) { this.cultivoId = cultivoId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public BigDecimal getQuantidadeKg() { return quantidadeKg; }
    public void setQuantidadeKg(BigDecimal quantidadeKg) { this.quantidadeKg = quantidadeKg; }
    public BigDecimal getPrecoKg() { return precoKg; }
    public void setPrecoKg(BigDecimal precoKg) { this.precoKg = precoKg; }
}
