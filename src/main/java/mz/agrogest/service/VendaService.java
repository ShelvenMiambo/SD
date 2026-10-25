package mz.agrogest.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
import mz.agrogest.util.Formatos;

/**
 * Carrinho, checkout e facturas.
 *
 * <p>Regras de negócio aplicadas aqui:</p>
 * <ul>
 *   <li>RN-a: não se vende mais do que o stock disponível;</li>
 *   <li>RN-b: a venda desconta o stock de todos os itens atomicamente (tudo ou nada);</li>
 *   <li>RN-c: um cliente só vê as suas facturas; o admin vê todas;</li>
 *   <li>não se vendem produtos fora de validade; no máximo {@value #MAXIMO_POR_LINHA} unidades por linha.</li>
 * </ul>
 */
public class VendaService {
    public static final BigDecimal TAXA_IVA = new BigDecimal("0.16");
    public static final int MAXIMO_POR_LINHA = 10_000;

    private final CatalogoService catalogo = new CatalogoService();

    // ---------- Carrinho ----------

    /** Adiciona ao carrinho, validando que o produto existe, está dentro da validade e há stock para o total pedido. */
    public void adicionarAoCarrinho(Carrinho carrinho, int produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("A quantidade deve ser maior que zero.", "quantidade");
        }
        Produto p = catalogo.produto(produtoId)
                .orElseThrow(() -> new NegocioException("O produto pedido não existe."));
        exigirVendavel(p, LocalDate.now());
        int novoTotal = carrinho.quantidadeDe(produtoId) + quantidade;
        exigirStock(p, novoTotal, carrinho.quantidadeDe(produtoId));
        carrinho.definir(produtoId, novoTotal);
    }

    /** Define a quantidade de uma linha (0 remove a linha). */
    public void atualizarCarrinho(Carrinho carrinho, int produtoId, int quantidade) {
        if (quantidade < 0) {
            throw new NegocioException("A quantidade não pode ser negativa.", "quantidade");
        }
        if (quantidade > 0) {
            Produto p = catalogo.produto(produtoId)
                    .orElseThrow(() -> new NegocioException("O produto pedido não existe."));
            exigirVendavel(p, LocalDate.now());
            exigirStock(p, quantidade, 0);
        }
        carrinho.definir(produtoId, quantidade);
    }

    /**
     * Ajusta cada linha ao stock actual: reduz o que excede e retira produtos esgotados ou fora de validade.
     *
     * @return número de linhas alteradas
     */
    public int ajustarAoStock(Carrinho carrinho) {
        int alteradas = 0;
        for (Map.Entry<Integer, Integer> e : carrinho.getItens().entrySet()) {
            Optional<Produto> op = catalogo.produto(e.getKey());
            int possivel = op.filter(p -> !Formatos.expirado(p.getValidade()))
                    .map(p -> Math.min(e.getValue(), Math.min(p.getQuantidade(), MAXIMO_POR_LINHA)))
                    .orElse(0);
            if (possivel != e.getValue()) {
                carrinho.definir(e.getKey(), possivel);
                alteradas++;
            }
        }
        return alteradas;
    }

    /** Quanto ainda se pode adicionar de um produto, descontando o que já está no carrinho. */
    public static int disponivelParaAdicionar(Produto p, Carrinho carrinho) {
        int noCarrinho = carrinho == null ? 0 : carrinho.quantidadeDe(p.getId());
        return Math.max(0, Math.min(p.getQuantidade(), MAXIMO_POR_LINHA) - noCarrinho);
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
        // Bloqueia o carrinho: dois pedidos simultâneos da mesma sessão (duplo clique) não geram
        // duas facturas — o segundo espera e encontra o carrinho já vazio.
        synchronized (carrinho) {
            Venda v = registarVenda(cliente, carrinho.getItens(), LocalDateTime.now());
            carrinho.esvaziar();
            return v;
        }
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
                    continue;
                }
                Produto p = op.get();
                if (qtd <= 0 || qtd > MAXIMO_POR_LINHA) {
                    erros.add("Quantidade inválida para " + p.getNome() + ".");
                } else if (p.getValidade() != null && p.getValidade().isBefore(data.toLocalDate())) {
                    erros.add(p.getNome() + " está fora de validade.");
                } else if (qtd > p.getQuantidade()) {
                    erros.add("Stock insuficiente para " + p.getNome() + ": pedido " + qtd
                            + " " + p.getUnidade() + ", disponível " + p.getQuantidade() + ".");
                } else {
                    produtos.add(p);
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
            v.setClienteEndereco(cliente.getEnderecoCompleto());
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

    /**
     * Anula uma venda (por exemplo, por engano) e devolve as quantidades ao stock.
     * A factura mantém o número, fica marcada como anulada e deixa de contar na facturação e nos relatórios.
     */
    public Venda anularVenda(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Venda v = venda(id).orElseThrow(() -> new NegocioException("Venda não encontrada."));
            if (v.isAnulada()) {
                throw new NegocioException("A factura " + v.getNumeroFactura() + " já está anulada.");
            }
            for (VendaItem i : v.getItens()) {
                catalogo.produto(i.getProdutoId()).ifPresent(p -> p.setQuantidade(p.getQuantidade() + i.getQuantidade()));
            }
            v.setAnulada(true);
            v.setDataAnulacao(LocalDateTime.now());
            return v;
        }
    }

    private static void exigirVendavel(Produto p, LocalDate dia) {
        if (p.getValidade() != null && p.getValidade().isBefore(dia)) {
            throw new NegocioException(p.getNome() + " está fora de validade desde "
                    + Formatos.data(p.getValidade()) + " e não pode ser vendido.");
        }
    }

    private static void exigirStock(Produto p, int quantidadePedida, int jaNoCarrinho) {
        if (quantidadePedida > MAXIMO_POR_LINHA) {
            throw new NegocioException("No máximo " + MAXIMO_POR_LINHA + " " + p.getUnidade()
                    + " de " + p.getNome() + " por compra.", "quantidade");
        }
        if (quantidadePedida > p.getQuantidade()) {
            String jaTem = jaNoCarrinho > 0 ? " (já tem " + jaNoCarrinho + " no carrinho)" : "";
            throw new NegocioException("Indisponível: só há " + p.getQuantidade() + " " + p.getUnidade()
                    + " de " + p.getNome() + " em stock" + jaTem + ".", "quantidade");
        }
    }

    // ---------- Consultas ----------

    public List<Venda> vendas() {
        return DataStore.copia(DataStore.get().getVendas()).stream()
                .sorted(Comparator.comparing(Venda::getData).reversed())
                .toList();
    }

    /** Vendas que contam para facturação e relatórios (não anuladas). */
    public List<Venda> vendasValidas() {
        return vendas().stream().filter(v -> !v.isAnulada()).toList();
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
        return vendasValidas().stream().map(Venda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
