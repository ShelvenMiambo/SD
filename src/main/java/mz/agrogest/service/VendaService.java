package mz.agrogest.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mz.agrogest.model.Carrinho;
import mz.agrogest.model.LinhaCarrinho;
import mz.agrogest.model.Perfil;
import mz.agrogest.model.Produto;
import mz.agrogest.model.Utilizador;
import mz.agrogest.model.Venda;
import mz.agrogest.model.VendaItem;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.DataStore.Sequencia;

/**
 * Carrinho, checkout e facturas.
 *
 * <p>Regras de negócio aplicadas aqui:</p>
 * <ul>
 *   <li>RN-a: não se vende mais do que o stock disponível;</li>
 *   <li>RN-b: a venda desconta o stock de todos os itens atomicamente (tudo ou nada);</li>
 *   <li>RN-c: um cliente só vê as suas facturas; o admin vê todas.</li>
 * </ul>
 */
public class VendaService {
    public static final BigDecimal TAXA_IVA = new BigDecimal("0.16");

    private final CatalogoService catalogo = new CatalogoService();

    // ---------- Carrinho ----------

    /** Adiciona ao carrinho, validando que o produto existe e que há stock para o total pedido. */
    public void adicionarAoCarrinho(Carrinho carrinho, int produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("A quantidade deve ser maior que zero.");
        }
        Produto p = catalogo.produto(produtoId)
                .orElseThrow(() -> new NegocioException("O produto pedido não existe."));
        int novoTotal = carrinho.quantidadeDe(produtoId) + quantidade;
        exigirStock(p, novoTotal);
        carrinho.definir(produtoId, novoTotal);
    }

    /** Define a quantidade de uma linha (0 remove a linha). */
    public void atualizarCarrinho(Carrinho carrinho, int produtoId, int quantidade) {
        if (quantidade < 0) {
            throw new NegocioException("A quantidade não pode ser negativa.");
        }
        if (quantidade > 0) {
            Produto p = catalogo.produto(produtoId)
                    .orElseThrow(() -> new NegocioException("O produto pedido não existe."));
            exigirStock(p, quantidade);
        }
        carrinho.definir(produtoId, quantidade);
    }

    /**
     * Linhas do carrinho com os dados actuais dos produtos.
     * Produtos que entretanto foram removidos do catálogo saem do carrinho.
     */
    public List<LinhaCarrinho> linhas(Carrinho carrinho) {
        List<LinhaCarrinho> linhas = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : carrinho.getItens().entrySet()) {
            Optional<Produto> p = catalogo.produto(e.getKey());
            if (p.isPresent()) {
                linhas.add(new LinhaCarrinho(p.get(), e.getValue()));
            } else {
                carrinho.remover(e.getKey());
            }
        }
        return linhas;
    }

    public static BigDecimal subtotal(List<LinhaCarrinho> linhas) {
        return linhas.stream().map(LinhaCarrinho::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal iva(BigDecimal subtotal) {
        return subtotal.multiply(TAXA_IVA).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------- Checkout ----------

    /**
     * Finaliza a compra do carrinho: valida tudo, desconta o stock e emite a factura.
     * Se qualquer item falhar, nada é alterado. O carrinho só é esvaziado em caso de sucesso.
     */
    public Venda finalizarCompra(Utilizador cliente, Carrinho carrinho) {
        Venda v = registarVenda(cliente, carrinho.getItens(), LocalDateTime.now());
        carrinho.esvaziar();
        return v;
    }

    /** Núcleo transaccional da venda. Também usado para carregar os dados de exemplo. */
    Venda registarVenda(Utilizador cliente, Map<Integer, Integer> itens, LocalDateTime data) {
        if (cliente == null || cliente.getPerfil() != Perfil.CLIENTE) {
            throw new NegocioException("Apenas clientes podem efetuar compras.");
        }
        if (itens.isEmpty()) {
            throw new NegocioException("O carrinho está vazio.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            // Fase 1: validar TODOS os itens sem alterar nada.
            List<String> erros = new ArrayList<>();
            List<Produto> produtos = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : itens.entrySet()) {
                Optional<Produto> op = catalogo.produto(e.getKey());
                int qtd = e.getValue();
                if (op.isEmpty()) {
                    erros.add("Um dos produtos do carrinho já não existe no catálogo.");
                } else if (qtd <= 0) {
                    erros.add("Quantidade inválida para " + op.get().getNome() + ".");
                } else if (qtd > op.get().getQuantidade()) {
                    Produto p = op.get();
                    erros.add("Stock insuficiente para " + p.getNome() + ": pedido " + qtd
                            + " " + p.getUnidade() + ", disponível " + p.getQuantidade() + ".");
                } else {
                    produtos.add(op.get());
                }
            }
            if (!erros.isEmpty()) {
                throw new NegocioException("Compra não efetuada. " + String.join(" ", erros));
            }

            // Fase 2: aplicar. Daqui em diante não há falhas possíveis.
            Venda v = new Venda();
            v.setId(ds.proximoId(Sequencia.VENDA));
            int ano = data.getYear();
            v.setNumeroFactura("FT-" + ano + "-" + ds.proximoNumeroFactura(ano));
            v.setClienteId(cliente.getId());
            v.setClienteNome(cliente.getNome());
            v.setClienteEmail(cliente.getEmail());
            v.setClienteTelefone(cliente.getTelefone());
            v.setClienteEndereco(cliente.getEndereco());
            v.setData(data);
            for (Produto p : produtos) {
                int qtd = itens.get(p.getId());
                p.setQuantidade(p.getQuantidade() - qtd);
                v.getItens().add(new VendaItem(p.getId(), p.getNome(), p.getUnidade(), qtd, p.getPreco()));
            }
            BigDecimal subtotal = v.getItens().stream().map(VendaItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            v.setSubtotal(subtotal);
            v.setIva(iva(subtotal));
            v.setTotal(subtotal.add(v.getIva()));
            ds.getVendas().add(v);
            return v;
        }
    }

    private static void exigirStock(Produto p, int quantidadePedida) {
        if (quantidadePedida > p.getQuantidade()) {
            throw new NegocioException("Indisponível: só há " + p.getQuantidade() + " " + p.getUnidade()
                    + " de " + p.getNome() + " em stock.");
        }
    }

    // ---------- Consultas ----------

    public List<Venda> vendas() {
        return DataStore.copia(DataStore.get().getVendas()).stream()
                .sorted(Comparator.comparing(Venda::getData).reversed())
                .toList();
    }

    public List<Venda> vendasDoCliente(int clienteId) {
        return vendas().stream().filter(v -> v.getClienteId() == clienteId).toList();
    }

    public Optional<Venda> venda(int id) {
        return DataStore.copia(DataStore.get().getVendas()).stream().filter(v -> v.getId() == id).findFirst();
    }

    /** RN-c: o admin vê todas as facturas; o cliente só as suas. */
    public boolean podeVer(Utilizador u, Venda v) {
        return u != null && (u.isAdmin() || v.getClienteId() == u.getId());
    }

    public BigDecimal faturacaoTotal() {
        return vendas().stream().map(Venda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
