package mz.agrogest.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mz.agrogest.model.ClienteResumo;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.Produto;
import mz.agrogest.model.Utilizador;
import mz.agrogest.model.Venda;

/** Listagens ordenáveis e relatórios de clientes. */
public class RelatorioService {
    private final UtilizadorService utilizadores = new UtilizadorService();
    private final CatalogoService catalogo = new CatalogoService();
    private final VendaService vendas = new VendaService();

    // ---------- Listagens ordenáveis ----------

    public List<Produto> produtos(String ordem, boolean desc) {
        return ordenar(catalogo.produtos(), comparadorProduto(ordem), desc);
    }

    /** Produtos em armazém: stock &gt; 0. */
    public List<Produto> armazem(String ordem, boolean desc) {
        return produtos(ordem, desc).stream().filter(p -> p.getQuantidade() > 0).toList();
    }

    public List<Fornecedor> fornecedores(String ordem, boolean desc) {
        Comparator<Fornecedor> c = switch (ordem) {
            case "tipo" -> Comparator.comparing(Fornecedor::getTipo);
            case "provincia" -> Comparator.comparing(Fornecedor::getProvincia, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Fornecedor::getNome, String.CASE_INSENSITIVE_ORDER);
        };
        return ordenar(catalogo.fornecedores(), c, desc);
    }

    public List<Utilizador> clientes(String ordem, boolean desc) {
        Comparator<Utilizador> c = switch (ordem) {
            case "data" -> Comparator.comparing(Utilizador::getDataRegisto);
            case "email" -> Comparator.comparing(Utilizador::getEmail);
            default -> Comparator.comparing(Utilizador::getNome, String.CASE_INSENSITIVE_ORDER);
        };
        return ordenar(utilizadores.clientes(), c, desc);
    }

    public List<Venda> compras(String ordem, boolean desc) {
        Comparator<Venda> c = switch (ordem) {
            case "valor" -> Comparator.comparing(Venda::getTotal);
            case "nome" -> Comparator.comparing(Venda::getClienteNome, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Venda::getData);
        };
        return ordenar(vendas.vendas(), c, desc);
    }

    private static Comparator<Produto> comparadorProduto(String ordem) {
        return switch (ordem) {
            case "preco" -> Comparator.comparing(Produto::getPreco);
            case "stock" -> Comparator.comparingInt(Produto::getQuantidade);
            case "data" -> Comparator.comparing(Produto::getValidade, Comparator.nullsLast(Comparator.naturalOrder()));
            case "categoria" -> Comparator.comparing(Produto::getCategoria);
            default -> Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private static <T> List<T> ordenar(List<T> lista, Comparator<T> c, boolean desc) {
        return lista.stream().sorted(desc ? c.reversed() : c).toList();
    }

    // ---------- Relatórios de clientes ----------

    /** Clientes ordenados por nome (crescente). */
    public List<Utilizador> clientesPorNome() {
        return clientes("nome", false);
    }

    /** Resumo de compras de todos os clientes. */
    public List<ClienteResumo> resumos() {
        Map<Integer, List<Venda>> porCliente = vendas.vendasValidas().stream()
                .collect(Collectors.groupingBy(Venda::getClienteId));
        return utilizadores.clientes().stream().map(c -> {
            List<Venda> vs = porCliente.getOrDefault(c.getId(), List.of());
            BigDecimal total = vs.stream().map(Venda::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            LocalDateTime ultima = vs.stream().map(Venda::getData).max(Comparator.naturalOrder()).orElse(null);
            return new ClienteResumo(c, vs.size(), total, ultima);
        }).toList();
    }

    /** Clientes que compraram, ordenados por volume (valor total gasto, decrescente). */
    public List<ClienteResumo> clientesQueCompraram() {
        return resumos().stream()
                .filter(r -> r.getNumeroCompras() > 0)
                .sorted(Comparator.comparing(ClienteResumo::getTotalGasto).reversed())
                .toList();
    }

    /** Ranking por número de compras (desempate pelo valor gasto). */
    public List<ClienteResumo> maisAssiduos() {
        return resumos().stream()
                .filter(r -> r.getNumeroCompras() > 0)
                .sorted(Comparator.comparingInt(ClienteResumo::getNumeroCompras)
                        .thenComparing(ClienteResumo::getTotalGasto).reversed())
                .toList();
    }

    /** Clientes registados que nunca compraram. */
    public List<Utilizador> clientesSemCompras() {
        return resumos().stream()
                .filter(r -> r.getNumeroCompras() == 0)
                .map(ClienteResumo::getCliente)
                .sorted(Comparator.comparing(Utilizador::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
