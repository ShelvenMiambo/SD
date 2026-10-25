package mz.agrogest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mz.agrogest.model.Campanha;
import mz.agrogest.model.Carrinho;
import mz.agrogest.model.Categoria;
import mz.agrogest.model.Cultivo;
import mz.agrogest.model.Fornecedor;
import mz.agrogest.model.Produto;
import mz.agrogest.model.TipoFornecedor;
import mz.agrogest.model.Utilizador;
import mz.agrogest.model.Venda;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.Persistencia;
import mz.agrogest.util.PasswordUtil;

class RegrasNegocioTest {
    private final UtilizadorService utilizadores = new UtilizadorService();
    private final CatalogoService catalogo = new CatalogoService();
    private final AgricolaService agricola = new AgricolaService();
    private final VendaService vendas = new VendaService();
    private final RelatorioService relatorios = new RelatorioService();

    private Utilizador joao;
    private Produto milho;
    private Produto adubo;

    @BeforeEach
    void preparar() {
        DataStore.substituir(DataStore.novoVazio());
        Fornecedor f = catalogo.criarFornecedor("Sementes do Sul, Lda", TipoFornecedor.SEMENTES, "84 000 0000", "Maputo");
        milho = catalogo.criarProduto(produto("Milho", f, "85", 10));
        adubo = catalogo.criarProduto(produto("Adubo", f, "2350", 2));
        joao = utilizadores.registarCliente("João Silva", "joao@teste.mz", "1234", "84 111 2222", "Av. Eduardo Mondlane, Nampula");
    }

    private static Produto produto(String nome, Fornecedor f, String preco, int qtd) {
        Produto p = new Produto();
        p.setNome(nome);
        p.setCategoria(Categoria.SEMENTE);
        p.setFornecedorId(f.getId());
        p.setPreco(new BigDecimal(preco));
        p.setQuantidade(qtd);
        p.setUnidade("kg");
        return p;
    }

    @Test
    void compraDescontaStockECalculaIva() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 3);
        vendas.adicionarAoCarrinho(c, adubo.getId(), 1);

        Venda v = vendas.finalizarCompra(joao, c);

        assertEquals(7, catalogo.produto(milho.getId()).orElseThrow().getQuantidade());
        assertEquals(1, catalogo.produto(adubo.getId()).orElseThrow().getQuantidade());
        assertEquals(new BigDecimal("2605.00"), v.getSubtotal());   // 3×85 + 2350
        assertEquals(new BigDecimal("416.80"), v.getIva());         // 16%
        assertEquals(new BigDecimal("3021.80"), v.getTotal());
        assertTrue(c.isVazio(), "o carrinho é esvaziado após a compra");
        assertTrue(v.getNumeroFactura().matches("FT-\\d{4}-1"));
    }

    @Test
    void naoPermiteAdicionarMaisDoQueOStock() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, adubo.getId(), 2);
        assertThrows(NegocioException.class, () -> vendas.adicionarAoCarrinho(c, adubo.getId(), 1));
        assertEquals(2, c.quantidadeDe(adubo.getId()));
    }

    @Test
    void vendaEAtomicaTudoOuNada() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 5);
        vendas.adicionarAoCarrinho(c, adubo.getId(), 2);
        // Outro cliente compra o adubo entretanto.
        adubo.setQuantidade(1);

        assertThrows(NegocioException.class, () -> vendas.finalizarCompra(joao, c));

        assertEquals(10, milho.getQuantidade(), "o milho não pode ter sido descontado");
        assertEquals(1, adubo.getQuantidade());
        assertTrue(vendas.vendas().isEmpty());
        assertFalse(c.isVazio(), "o carrinho mantém-se para o cliente corrigir");
    }

    @Test
    void comprasConcorrentesNuncaVendemMaisDoQueOStock() throws Exception {
        List<Utilizador> clientes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            clientes.add(utilizadores.registarCliente("Cliente " + i, "c" + i + "@teste.mz", "1234", "84", "Nampula"));
        }
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        for (Utilizador cl : clientes) {
            pool.submit(() -> {
                Carrinho c = new Carrinho();
                c.definir(milho.getId(), 1);
                partida.await();
                try {
                    vendas.finalizarCompra(cl, c);
                    sucessos.incrementAndGet();
                } catch (NegocioException e) {
                    // sem stock: esperado para 10 dos 20 clientes
                }
                return null;
            });
        }
        partida.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(10, sucessos.get());
        assertEquals(0, milho.getQuantidade());
    }

    @Test
    void facturasSaoSequenciaisPorAno() {
        java.util.Map<Integer, Integer> itens = java.util.Map.of(milho.getId(), 1);
        assertEquals("FT-2025-1", vendas.registarVenda(joao, itens, LocalDateTime.of(2025, 5, 1, 10, 0)).getNumeroFactura());
        assertEquals("FT-2025-2", vendas.registarVenda(joao, itens, LocalDateTime.of(2025, 6, 1, 10, 0)).getNumeroFactura());
        assertEquals("FT-2026-1", vendas.registarVenda(joao, itens, LocalDateTime.of(2026, 1, 1, 10, 0)).getNumeroFactura());
    }

    @Test
    void clienteSoVeAsSuasFacturas() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 1);
        Venda v = vendas.finalizarCompra(joao, c);
        Utilizador outro = utilizadores.registarCliente("Maria", "maria@teste.mz", "1234", "84", "Beira");
        Utilizador admin = utilizadores.criarAdmin("Admin", "admin@agro.mz", "admin234");

        assertTrue(vendas.podeVer(joao, v));
        assertFalse(vendas.podeVer(outro, v));
        assertTrue(vendas.podeVer(admin, v));
    }

    @Test
    void emailDeRegistoEUnico() {
        assertThrows(NegocioException.class,
                () -> utilizadores.registarCliente("Outro João", "JOAO@teste.mz", "1234", "84", "Nampula"));
    }

    @Test
    void passwordGuardadaComHash() {
        assertFalse(joao.getPasswordHash().contains("1234"));
        assertTrue(PasswordUtil.verificar("1234", joao.getPasswordHash()));
        assertTrue(utilizadores.autenticar("joao@teste.mz", "1234").isPresent());
        assertTrue(utilizadores.autenticar("joao@teste.mz", "errada").isEmpty());
    }

    @Test
    void naoRemoveCampanhaComCultivosNemCultivoComColheitas() {
        Campanha camp = agricola.criarCampanha("Campanha 2026/2027", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 6, 30));
        Cultivo cv = agricola.criarCultivo(camp.getId(), "Feijão nhemba", "Macia", new BigDecimal("8"), LocalDate.of(2026, 11, 15));
        var col = agricola.criarColheita(cv.getId(), LocalDate.of(2027, 3, 1), new BigDecimal("3200"), new BigDecimal("22"));

        assertEquals(new BigDecimal("70400.00"), col.getValorTotal());
        assertThrows(NegocioException.class, () -> agricola.removerCampanha(camp.getId()));
        assertThrows(NegocioException.class, () -> agricola.removerCultivo(cv.getId()));

        agricola.removerColheita(col.getId());
        agricola.removerCultivo(cv.getId());
        agricola.removerCampanha(camp.getId());
        assertTrue(agricola.campanhas().isEmpty());
    }

    @Test
    void campanhaFechadaNaoAceitaCultivos() {
        Campanha camp = agricola.criarCampanha("C", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        agricola.definirFechada(camp.getId(), true);
        assertThrows(NegocioException.class,
                () -> agricola.criarCultivo(camp.getId(), "Milho", "", BigDecimal.ONE, LocalDate.of(2026, 2, 1)));
        agricola.definirFechada(camp.getId(), false);
        agricola.criarCultivo(camp.getId(), "Milho", "", BigDecimal.ONE, LocalDate.of(2026, 2, 1));
    }

    @Test
    void relatoriosOrdenamPorVolumeEAssiduidade() {
        Utilizador ana = utilizadores.registarCliente("Ana", "ana@teste.mz", "1234", "84", "Nampula");
        utilizadores.registarCliente("Zé", "ze@teste.mz", "1234", "84", "Nampula");
        java.util.Map<Integer, Integer> umMilho = java.util.Map.of(milho.getId(), 1);
        vendas.registarVenda(joao, umMilho, LocalDateTime.now());
        vendas.registarVenda(joao, umMilho, LocalDateTime.now());
        vendas.registarVenda(ana, java.util.Map.of(adubo.getId(), 1), LocalDateTime.now());

        assertEquals("Ana", relatorios.clientesQueCompraram().get(0).getCliente().getNome());    // maior volume
        assertEquals("João Silva", relatorios.maisAssiduos().get(0).getCliente().getNome());     // mais compras
        assertEquals(List.of("Zé"), relatorios.clientesSemCompras().stream().map(Utilizador::getNome).toList());
        assertEquals("Ana", relatorios.clientesPorNome().get(0).getNome());
    }

    @Test
    void persistenciaGravaERestaura(@TempDir Path dir) throws Exception {
        Path ficheiro = dir.resolve("agrogest.ser");
        Persistencia p = new Persistencia(ficheiro);
        p.gravar(DataStore.get());
        p.gravar(DataStore.get()); // segunda gravação cria o .bak

        assertTrue(Files.exists(ficheiro));
        assertTrue(Files.exists(dir.resolve("agrogest.ser.bak")));
        assertFalse(Files.exists(dir.resolve("agrogest.ser.tmp")));

        DataStore restaurado = p.carregar();
        assertEquals(2, restaurado.getProdutos().size());
        assertEquals("joao@teste.mz", restaurado.getUtilizadores().get(0).getEmail());
        // As sequências também são restauradas: o próximo ID continua a numeração.
        assertEquals(3, restaurado.proximoId(DataStore.Sequencia.PRODUTO));
    }

    @Test
    void persistenciaUsaBackupSeOFicheiroPrincipalEstiverCorrompido(@TempDir Path dir) throws Exception {
        Path ficheiro = dir.resolve("agrogest.ser");
        Persistencia p = new Persistencia(ficheiro);
        p.gravar(DataStore.get());
        p.gravar(DataStore.get());
        Files.writeString(ficheiro, "lixo");

        DataStore restaurado = p.carregar();
        assertEquals(2, restaurado.getProdutos().size());
    }
}
