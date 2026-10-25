package mz.agrogest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.Validacao;

/** Fornecedores e produtos. */
public class CatalogoService {
    public static final BigDecimal PRECO_MAXIMO = new BigDecimal("10000000");
    public static final int STOCK_MAXIMO = 1_000_000;

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
        Fornecedor dados = validarFornecedor(nome, tipo, contacto, provincia);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            exigirNomeFornecedorLivre(dados.getNome(), 0);
            dados.setId(ds.proximoId(Sequencia.FORNECEDOR));
            ds.getFornecedores().add(dados);
            return dados;
        }
    }

    public Fornecedor atualizarFornecedor(int id, String nome, TipoFornecedor tipo, String contacto, String provincia) {
        Fornecedor dados = validarFornecedor(nome, tipo, contacto, provincia);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Fornecedor f = fornecedor(id).orElseThrow(() -> new NegocioException("Fornecedor não encontrado."));
            exigirNomeFornecedorLivre(dados.getNome(), id);
            if (f.getTipo() != tipo) {
                List<String> incompativeis = produtos().stream()
                        .filter(p -> p.getFornecedorId() == id && !tipo.forneceCategoria(p.getCategoria()))
                        .map(Produto::getNome).toList();
                if (!incompativeis.isEmpty()) {
                    throw new NegocioException("O tipo " + tipo + " não é compatível com os produtos deste fornecedor: "
                            + String.join(", ", incompativeis) + ".", "tipo");
                }
            }
            f.setNome(dados.getNome());
            f.setTipo(tipo);
            f.setContacto(dados.getContacto());
            f.setProvincia(dados.getProvincia());
            return f;
        }
    }

    private static Fornecedor validarFornecedor(String nome, TipoFornecedor tipo, String contacto, String provincia) {
        String nomeOk = Validacao.texto(nome, 100, "nome", "nome");
        if (nomeOk.length() < 3) {
            throw new NegocioException("O nome do fornecedor deve ter pelo menos 3 caracteres.", "nome");
        }
        if (tipo == null) {
            throw new NegocioException("O tipo de fornecedor é obrigatório.", "tipo");
        }
        String contactoOk = Validacao.telefone(contacto, "contacto");
        if (provincia == null || !Opcoes.PROVINCIAS.contains(provincia)) {
            throw new NegocioException("Escolha a província da lista.", "provincia");
        }
        return new Fornecedor(0, nomeOk, tipo, contactoOk, provincia);
    }

    private void exigirNomeFornecedorLivre(String nome, int idProprio) {
        String chave = Validacao.chave(nome);
        boolean duplicado = fornecedores().stream()
                .anyMatch(f -> f.getId() != idProprio && Validacao.chave(f.getNome()).equals(chave));
        if (duplicado) {
            throw new NegocioException("Já existe um fornecedor chamado \"" + nome + "\".", "nome");
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
        return pesquisar(categoria, null);
    }

    /** Filtra por categoria (opcional) e por texto no nome ou descrição, sem ligar a acentos nem maiúsculas. */
    public List<Produto> pesquisar(Categoria categoria, String texto) {
        String q = Validacao.chave(texto);
        return produtos().stream()
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .filter(p -> q.isEmpty()
                        || Validacao.chave(p.getNome()).contains(q)
                        || Validacao.chave(p.getDescricao()).contains(q))
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<Produto> produto(int id) {
        return produtos().stream().filter(p -> p.getId() == id).findFirst();
    }

    public Produto criarProduto(Produto dados) {
        validarProduto(dados, null);
        DataStore ds = DataStore.get();
        synchronized (ds) {
            exigirNomeProdutoLivre(dados, 0);
            dados.setId(ds.proximoId(Sequencia.PRODUTO));
            ds.getProdutos().add(dados);
            return dados;
        }
    }

    /** Atualiza todos os campos editáveis de um produto existente. */
    public Produto atualizarProduto(int id, Produto dados) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Produto p = produto(id).orElseThrow(() -> new NegocioException("Produto não encontrado."));
            validarProduto(dados, p);
            exigirNomeProdutoLivre(dados, id);
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
            throw new NegocioException("A quantidade a repor deve ser maior que zero.", "quantidade");
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Produto p = produto(id).orElseThrow(() -> new NegocioException("Produto não encontrado."));
            if ((long) p.getQuantidade() + quantidade > STOCK_MAXIMO) {
                throw new NegocioException("O stock de " + p.getNome() + " não pode passar de "
                        + STOCK_MAXIMO + " " + p.getUnidade() + ".", "quantidade");
            }
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

    /** Produtos com validade até {@code dias} dias a partir de hoje (inclui os já expirados). */
    public List<Produto> aExpirar(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        return produtos().stream()
                .filter(p -> p.getValidade() != null && !p.getValidade().isAfter(limite))
                .sorted(Comparator.comparing(Produto::getValidade))
                .toList();
    }

    /**
     * @param atual o produto que está a ser editado, ou {@code null} ao criar
     */
    private void validarProduto(Produto p, Produto atual) {
        p.setNome(Validacao.texto(p.getNome(), 100, "nome", "nome"));
        if (p.getNome().length() < 3) {
            throw new NegocioException("O nome do produto deve ter pelo menos 3 caracteres.", "nome");
        }
        if (p.getCategoria() == null) {
            throw new NegocioException("A categoria é obrigatória.", "categoria");
        }
        Fornecedor f = fornecedor(p.getFornecedorId())
                .orElseThrow(() -> new NegocioException("Fornecedor inválido.", "fornecedorId"));
        if (!f.getTipo().forneceCategoria(p.getCategoria())) {
            throw new NegocioException("\"" + f.getNome() + "\" é um fornecedor de " + f.getTipo()
                    + " e não pode fornecer produtos da categoria " + p.getCategoria() + ".", "fornecedorId");
        }
        Validacao.positivo(p.getPreco(), PRECO_MAXIMO, "preço", "preco");
        if (p.getQuantidade() < 0) {
            throw new NegocioException("A quantidade não pode ser negativa.", "quantidade");
        }
        if (p.getQuantidade() > STOCK_MAXIMO) {
            throw new NegocioException("A quantidade não pode passar de " + STOCK_MAXIMO + ".", "quantidade");
        }
        if (p.getUnidade() == null || !Opcoes.UNIDADES.contains(p.getUnidade())) {
            throw new NegocioException("Escolha a unidade da lista.", "unidade");
        }
        boolean validadeMudou = atual == null || !java.util.Objects.equals(atual.getValidade(), p.getValidade());
        if (validadeMudou && p.getValidade() != null && p.getValidade().isBefore(LocalDate.now())) {
            throw new NegocioException("A validade não pode ser uma data passada.", "validade");
        }
        if (p.getValidade() != null && p.getValidade().isAfter(LocalDate.now().plusYears(20))) {
            throw new NegocioException("A validade parece errada (mais de 20 anos).", "validade");
        }
        String descricao = p.getDescricao() == null ? "" : p.getDescricao().trim();
        if (descricao.length() > 1000) {
            throw new NegocioException("A descrição tem no máximo 1000 caracteres.", "descricao");
        }
        p.setDescricao(descricao);
        String foto = p.getFotoUrl() == null ? "" : p.getFotoUrl().trim();
        if (!foto.isEmpty() && !foto.matches("(?i)^(https?://|/)\\S+$")) {
            throw new NegocioException("A foto deve ser um URL https:// ou um caminho da aplicação começado por / (ex.: /img/produtos/milho.jpg).", "fotoUrl");
        }
        p.setFotoUrl(foto);
    }

    private void exigirNomeProdutoLivre(Produto p, int idProprio) {
        String chave = Validacao.chave(p.getNome());
        boolean duplicado = produtos().stream().anyMatch(x -> x.getId() != idProprio
                && x.getFornecedorId() == p.getFornecedorId()
                && Validacao.chave(x.getNome()).equals(chave));
        if (duplicado) {
            throw new NegocioException("Este fornecedor já tem um produto chamado \"" + p.getNome() + "\".", "nome");
        }
    }
}
