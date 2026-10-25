package mz.agrogest.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import mz.agrogest.model.Categoria;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.Produto;
import mz.agrogest.model.TipoFornecedor;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.DataStore.Sequencia;

/** Fornecedores e produtos. */
public class CatalogoService {

    // ---------- Fornecedores ----------

    public List<Fornecedor> fornecedores() {
        return DataStore.copia(DataStore.get().getFornecedores());
    }

    public Optional<Fornecedor> fornecedor(int id) {
        return fornecedores().stream().filter(f -> f.getId() == id).findFirst();
    }

    /** Mapa id → fornecedor, para as vistas mostrarem o nome do fornecedor de cada produto. */
    public Map<Integer, Fornecedor> fornecedoresPorId() {
        return fornecedores().stream().collect(Collectors.toMap(Fornecedor::getId, Function.identity()));
    }

    public Fornecedor criarFornecedor(String nome, TipoFornecedor tipo, String contacto, String provincia) {
        exigir(nome, "nome");
        exigir(contacto, "contacto");
        exigir(provincia, "província");
        if (tipo == null) {
            throw new NegocioException("O tipo de fornecedor é obrigatório.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            boolean duplicado = fornecedores().stream().anyMatch(f -> f.getNome().equalsIgnoreCase(nome.trim()));
            if (duplicado) {
                throw new NegocioException("Já existe um fornecedor chamado \"" + nome.trim() + "\".");
            }
            Fornecedor f = new Fornecedor(ds.proximoId(Sequencia.FORNECEDOR), nome.trim(), tipo,
                    contacto.trim(), provincia.trim());
            ds.getFornecedores().add(f);
            return f;
        }
    }

    public void removerFornecedor(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Fornecedor f = fornecedor(id).orElseThrow(() -> new NegocioException("Fornecedor não encontrado."));
            long nProdutos = produtos().stream().filter(p -> p.getFornecedorId() == id).count();
            if (nProdutos > 0) {
                throw new NegocioException("Não é possível remover \"" + f.getNome() + "\": tem "
                        + nProdutos + " produto(s) associado(s).");
            }
            ds.getFornecedores().removeIf(x -> x.getId() == id);
        }
    }

    // ---------- Produtos ----------

    public List<Produto> produtos() {
        return DataStore.copia(DataStore.get().getProdutos());
    }

    public List<Produto> produtosPorCategoria(Categoria categoria) {
        return produtos().stream()
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Produto> produto(int id) {
        return produtos().stream().filter(p -> p.getId() == id).findFirst();
    }

    public Produto criarProduto(Produto dados) {
        validarProduto(dados);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            dados.setId(ds.proximoId(Sequencia.PRODUTO));
            ds.getProdutos().add(dados);
            return dados;
        }
    }

    /** Atualiza todos os campos editáveis de um produto existente. */
    public Produto atualizarProduto(int id, Produto dados) {
        validarProduto(dados);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Produto p = produto(id).orElseThrow(() -> new NegocioException("Produto não encontrado."));
            p.setNome(dados.getNome());
            p.setCategoria(dados.getCategoria());
            p.setFornecedorId(dados.getFornecedorId());
            p.setPreco(dados.getPreco());
            p.setQuantidade(dados.getQuantidade());
            p.setUnidade(dados.getUnidade());
            p.setDescricao(dados.getDescricao());
            p.setFotoUrl(dados.getFotoUrl());
            p.setValidade(dados.getValidade());
            return p;
        }
    }

    /** Soma {@code quantidade} ao stock actual. */
    public Produto reporStock(int id, int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("A quantidade a repor deve ser maior que zero.");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Produto p = produto(id).orElseThrow(() -> new NegocioException("Produto não encontrado."));
            p.setQuantidade(p.getQuantidade() + quantidade);
            return p;
        }
    }

    public void removerProduto(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            if (!ds.getProdutos().removeIf(p -> p.getId() == id)) {
                throw new NegocioException("Produto não encontrado.");
            }
        }
    }

    private void validarProduto(Produto p) {
        exigir(p.getNome(), "nome");
        exigir(p.getUnidade(), "unidade");
        if (p.getCategoria() == null) {
            throw new NegocioException("A categoria é obrigatória.");
        }
        if (fornecedor(p.getFornecedorId()).isEmpty()) {
            throw new NegocioException("Fornecedor inválido.");
        }
        if (p.getPreco() == null || p.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O preço deve ser maior que zero.");
        }
        if (p.getQuantidade() < 0) {
            throw new NegocioException("A quantidade não pode ser negativa.");
        }
        String foto = p.getFotoUrl() == null ? "" : p.getFotoUrl().trim();
        if (!foto.isEmpty() && !foto.matches("(?i)^(https?://|/)\\S+$")) {
            throw new NegocioException("A foto deve ser um URL https:// ou um caminho da aplicação começado por / (ex.: /img/produtos/milho.jpg).");
        }
        p.setFotoUrl(foto);
        p.setNome(p.getNome().trim());
        p.setUnidade(p.getUnidade().trim());
    }

    private static void exigir(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("O campo \"" + campo + "\" é obrigatório.");
        }
    }
}
