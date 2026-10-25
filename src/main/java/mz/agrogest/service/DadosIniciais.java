package mz.agrogest.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import mz.agrogest.model.Categoria;
import mz.agrogest.model.Cultivo;
import mz.agrogest.model.Campanha;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.Produto;
import mz.agrogest.model.TipoFornecedor;
import mz.agrogest.model.Utilizador;

/**
 * Dados de exemplo carregados no primeiro arranque (quando não há ficheiro gravado):
 * administrador fixo, 3 fornecedores, 3 produtos, 5 clientes, 4 vendas e 1 campanha.
 *
 * <p>As 4 vendas de exemplo são de 2025 (FT-2025-1 a FT-2025-4), para que a primeira
 * compra feita na demonstração seja a FT-2026-1.</p>
 */
public final class DadosIniciais {
    public static final String ADMIN_EMAIL = "admin@agro.mz";
    public static final String ADMIN_PASSWORD = "admin234";
    public static final String CLIENTE_PASSWORD = "1234";

    private DadosIniciais() {
    }

    public static void carregar() {
        UtilizadorService us = new UtilizadorService();
        CatalogoService cs = new CatalogoService();
        AgricolaService as = new AgricolaService();
        VendaService vs = new VendaService();

        us.criarAdmin("Administrador", ADMIN_EMAIL, ADMIN_PASSWORD);

        Fornecedor f1 = cs.criarFornecedor("AgroSementes Nampula", TipoFornecedor.SEMENTES, "+258 84 123 4567", "Nampula");
        Fornecedor f2 = cs.criarFornecedor("FertiMoz, Lda", TipoFornecedor.FERTILIZANTES, "+258 82 765 4321", "Sofala");
        Fornecedor f3 = cs.criarFornecedor("MecAgro Comercial", TipoFornecedor.FERRAMENTAS, "+258 87 555 0101", "Maputo Cidade");

        Produto milho = cs.criarProduto(produto("Semente de Milho", Categoria.SEMENTE, f1, "85", 500, "kg",
                "Milho branco de polinização aberta (variedade Matuba), adaptado ao clima de Nampula.",
                LocalDate.now().plusMonths(10), "/img/produtos/milho.jpg"));
        Produto adubo = cs.criarProduto(produto("Adubo NPK 12-24-12", Categoria.FERTILIZANTE, f2, "2350", 120, "saco",
                "Saco de 50 kg. Adubo de fundo para milho, feijão e hortícolas.",
                LocalDate.now().plusYears(2), "/img/produtos/adubo.jpg"));
        Produto enxada = cs.criarProduto(produto("Enxada de cabo longo", Categoria.FERRAMENTA, f3, "450", 60, "unidade",
                "Enxada de aço forjado com cabo de madeira de 1,2 m.", null, "/img/produtos/enxada.jpg"));

        Utilizador ana = us.registarCliente("Ana Mussa", "ana@teste.mz", CLIENTE_PASSWORD, "+258 84 111 2233", "Nampula", "Bairro Muahivire, Rua 4");
        Utilizador carlos = us.registarCliente("Carlos Nhantumbo", "carlos@teste.mz", CLIENTE_PASSWORD, "+258 82 222 3344", "Maputo Cidade", "Av. de Moçambique, 1520");
        Utilizador fatima = us.registarCliente("Fátima Abdala", "fatima@teste.mz", CLIENTE_PASSWORD, "+258 86 333 4455", "Nampula", "Rua dos Continuadores, Nacala");
        us.registarCliente("Pedro Cossa", "pedro@teste.mz", CLIENTE_PASSWORD, "+258 84 444 5566", "Nampula", "Bairro Namutequeliua");
        us.registarCliente("Rosa Langa", "rosa@teste.mz", CLIENTE_PASSWORD, "+258 87 555 6677", "Sofala", "Av. Eduardo Mondlane, Beira");

        vs.registarVenda(ana, itens(milho, 20, adubo, 2), LocalDateTime.of(2025, 10, 14, 9, 30));
        vs.registarVenda(carlos, itens(enxada, 3), LocalDateTime.of(2025, 11, 2, 15, 10));
        vs.registarVenda(ana, itens(adubo, 1, enxada, 1), LocalDateTime.of(2025, 11, 20, 11, 45));
        vs.registarVenda(fatima, itens(milho, 50), LocalDateTime.of(2025, 12, 5, 8, 0));

        Campanha c = as.criarCampanha("Campanha 2025/2026", LocalDate.of(2025, 10, 1), LocalDate.of(2026, 6, 30));
        Cultivo cv = as.criarCultivo(c.getId(), "Milho", null, "Matuba", new BigDecimal("5"), LocalDate.of(2025, 11, 10));
        as.criarColheita(cv.getId(), LocalDate.of(2026, 4, 15), new BigDecimal("12000"), new BigDecimal("18"));
    }

    private static Produto produto(String nome, Categoria cat, Fornecedor f, String preco, int qtd, String unidade,
                                   String descricao, LocalDate validade, String foto) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(cat);
        p.setFornecedorId(f.getId());
        p.setPreco(new BigDecimal(preco));
        p.setQuantidade(qtd);
        p.setUnidade(unidade);
        p.setDescricao(descricao);
        p.setFotoUrl(foto);
        p.setValidade(validade);
        return p;
    }

    private static Map<Integer, Integer> itens(Object... pares) {
        Map<Integer, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put(((Produto) pares[i]).getId(), (Integer) pares[i + 1]);
        }
        return m;
    }
}
