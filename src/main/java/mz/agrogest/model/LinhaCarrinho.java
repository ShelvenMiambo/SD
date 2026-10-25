package mz.agrogest.model;

import java.math.BigDecimal;

/** Linha do carrinho preparada para a vista (produto + quantidade pedida). */
public class LinhaCarrinho {
    private final Produto produto;
    private final int quantidade;

    public LinhaCarrinho(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }

    public BigDecimal getSubtotal() {
        return produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
    }

    public boolean isStockSuficiente() {
        return quantidade <= produto.getQuantidade();
    }
}
