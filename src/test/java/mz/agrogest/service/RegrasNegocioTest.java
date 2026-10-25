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
import java.util.Map;
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

    private Fornecedor sementes;
    private Utilizador joao;
    private Produto milho;
    private Produto adubo;

    @BeforeEach
    void preparar() {
        DataStore.substituir(DataStore.novoVazio());
        sementes = catalogo.criarFornecedor("Sementes do Sul, Lda", TipoFornecedor.SEMENTES, "84 000 0000", "Maputo Cidade");
        milho = catalogo.criarProduto(produto("Milho", sementes, "85", 10));
        adubo = catalogo.criarProduto(produto("Feijão nhemba", sementes, "2350", 2));
        joao = cliente("João Silva", "joao@teste.mz");
    }

    private Utilizador cliente(String nome, String email) {
        return utilizadores.registarCliente(nome, email, "1234", "84 111 2222", "Nampula", "Av. Eduardo Mondlane");
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

    // ---------- Compras e stock ----------

    @Test
    void compraDescontaStockECalculaIva() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 3);
        vendas.adicionarAoCarrinho(c, adubo.getId(), 1);

        Venda v = vendas.finalizarCompra(joao, c);

        assertEquals(7, catalogo.produto(milho.getId()).orElseThrow().getQuantidade());
        assertEquals(1, catalogo.produto(adubo.getId()).orElseThrow().getQuantidade());
        assertEquals(new BigDecimal("2605.00"), v.getSubtotal());
        assertEquals(new BigDecimal("416.80"), v.getIva());
        assertEquals(new BigDecimal("3021.80"), v.getTotal());
        assertTrue(c.isVazio(), "o carrinho é esvaziado após a compra");
        assertTrue(v.getNumeroFactura().matches("FT-\\d{4}-1"));
        assertEquals("Av. Eduardo Mondlane, Nampula", v.getClienteEndereco());
    }

    @Test
    void naoPermiteAdicionarMaisDoQueOStock() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, adubo.getId(), 2);
        NegocioException e = assertThrows(NegocioException.class, () -> vendas.adicionarAoCarrinho(c, adubo.getId(), 1));
        assertTrue(e.getMessage().contains("já tem 2 no carrinho"));
        assertEquals(2, c.quantidadeDe(adubo.getId()));
        assertEquals(0, VendaService.disponivelParaAdicionar(adubo, c));
    }

    @Test
    void vendaEAtomicaTudoOuNada() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 5);
        vendas.adicionarAoCarrinho(c, adubo.getId(), 2);
        adubo.setQuantidade(1); // outro cliente comprou entretanto

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
            clientes.add(cliente("Cliente " + (char) ('A' + i) + " Teste", "c" + i + "@teste.mz"));
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
    void duploCliqueNaoGeraDuasFacturas() throws Exception {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch partida = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                partida.await();
                try {
                    vendas.finalizarCompra(joao, c);
                    sucessos.incrementAndGet();
                } catch (NegocioException e) {
                    // "O carrinho está vazio." — esperado para o segundo pedido
                }
                return null;
            });
        }
        partida.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1, sucessos.get());
        assertEquals(1, vendas.vendas().size());
        assertEquals(8, milho.getQuantidade());
    }

    @Test
    void produtoForaDeValidadeNaoSeVende() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 1);
        milho.setValidade(LocalDate.now().minusDays(1)); // expirou entretanto

        assertThrows(NegocioException.class, () -> vendas.adicionarAoCarrinho(c, milho.getId(), 1));
        assertThrows(NegocioException.class, () -> vendas.finalizarCompra(joao, c));
        assertEquals(10, milho.getQuantidade());
    }

    @Test
    void limiteMaximoPorLinha() {
        milho.setQuantidade(50_000);
        Carrinho c = new Carrinho();
        assertThrows(NegocioException.class,
                () -> vendas.adicionarAoCarrinho(c, milho.getId(), VendaService.MAXIMO_POR_LINHA + 1));
    }

    @Test
    void ajustarAoStockReduzERemoveLinhas() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 8);
        vendas.adicionarAoCarrinho(c, adubo.getId(), 2);
        milho.setQuantidade(3);
        adubo.setQuantidade(0);

        assertEquals(2, vendas.ajustarAoStock(c));
        assertEquals(3, c.quantidadeDe(milho.getId()));
        assertEquals(0, c.quantidadeDe(adubo.getId()));
    }

    @Test
    void administradorNaoCompra() {
        Utilizador admin = utilizadores.criarAdmin("Admin", "admin@agro.mz", "admin234");
        Carrinho c = new Carrinho();
        c.definir(milho.getId(), 1);
        assertThrows(NegocioException.class, () -> vendas.finalizarCompra(admin, c));
    }

    @Test
    void anularVendaDevolveStockESaiDaFacturacao() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 4);
        Venda v = vendas.finalizarCompra(joao, c);
        assertEquals(6, milho.getQuantidade());

        vendas.anularVenda(v.getId());

        assertEquals(10, milho.getQuantidade());
        assertTrue(v.isAnulada());
        assertEquals(BigDecimal.ZERO, vendas.faturacaoTotal());
        assertTrue(relatorios.clientesQueCompraram().isEmpty());
        assertThrows(NegocioException.class, () -> vendas.anularVenda(v.getId()), "não se anula duas vezes");
    }

    @Test
    void facturasSaoSequenciaisPorAno() {
        Map<Integer, Integer> itens = Map.of(milho.getId(), 1);
        assertEquals("FT-2025-1", vendas.registarVenda(joao, itens, LocalDateTime.of(2025, 5, 1, 10, 0)).getNumeroFactura());
        assertEquals("FT-2025-2", vendas.registarVenda(joao, itens, LocalDateTime.of(2025, 6, 1, 10, 0)).getNumeroFactura());
        assertEquals("FT-2026-1", vendas.registarVenda(joao, itens, LocalDateTime.of(2026, 1, 1, 10, 0)).getNumeroFactura());
    }

    @Test
    void clienteSoVeAsSuasFacturas() {
        Carrinho c = new Carrinho();
        vendas.adicionarAoCarrinho(c, milho.getId(), 1);
        Venda v = vendas.finalizarCompra(joao, c);
        Utilizador outro = cliente("Maria Joana", "maria@teste.mz");
        Utilizador admin = utilizadores.criarAdmin("Admin", "admin@agro.mz", "admin234");

        assertTrue(vendas.podeVer(joao, v));
        assertFalse(vendas.podeVer(outro, v));
        assertTrue(vendas.podeVer(admin, v));
    }

    // ---------- Utilizadores ----------

    @Test
    void emailDeRegistoEUnico() {
        assertThrows(NegocioException.class, () -> cliente("Outro João", "JOAO@teste.mz"));
    }

    @Test
    void registoValidaNomeTelefoneEProvincia() {
        NegocioException nome = assertThrows(NegocioException.class, () -> cliente("Ana", "a@teste.mz"));
        assertEquals("nome", nome.getCampo());
        assertThrows(NegocioException.class, () -> cliente("Ana 123", "a@teste.mz"));

        NegocioException tel = assertThrows(NegocioException.class,
                () -> utilizadores.registarCliente("Ana Mussa", "a@teste.mz", "1234", "12345", "Nampula", "Rua 1"));
        assertEquals("telefone", tel.getCampo());

        NegocioException prov = assertThrows(NegocioException.class,
                () -> utilizadores.registarCliente("Ana Mussa", "a@teste.mz", "1234", "841112222", "Lisboa", "Rua 1"));
        assertEquals("provincia", prov.getCampo());

        Utilizador ok = utilizadores.registarCliente("Ana Mussa", "a@teste.mz", "1234", "+258841112222", "Nampula", "Rua 1");
        assertEquals("+258 84 111 2222", ok.getTelefone(), "o telefone é guardado num formato único");
    }

    @Test
    void passwordGuardadaComHashEAlteravel() {
        assertFalse(joao.getPasswordHash().contains("1234"));
        assertTrue(PasswordUtil.verificar("1234", joao.getPasswordHash()));
        assertThrows(NegocioException.class, () -> utilizadores.alterarPassword(joao.getId(), "errada", "nova123", "nova123"));
        assertThrows(NegocioException.class, () -> utilizadores.alterarPassword(joao.getId(), "1234", "nova123", "outra"));

        utilizadores.alterarPassword(joao.getId(), "1234", "nova123", "nova123");
        assertTrue(utilizadores.autenticar("joao@teste.mz", "nova123").isPresent());
        assertTrue(utilizadores.autenticar("joao@teste.mz", "1234").isEmpty());
    }

    @Test
    void loginBloqueadoAposCincoFalhas() {
        String email = "bloqueio@teste.mz";
        for (int i = 0; i < TentativasLogin.MAXIMO; i++) {
            TentativasLogin.registarFalha(email);
        }
        assertTrue(TentativasLogin.segundosBloqueado(email) > 0);
        TentativasLogin.limpar(email);
        assertEquals(0, TentativasLogin.segundosBloqueado(email));
    }

    // ---------- Catálogo ----------

    @Test
    void produtoValidaCategoriaUnidadeNomeEValidade() {
        Produto enxada = produto("Enxada", sementes, "450", 5);
        enxada.setCategoria(Categoria.FERRAMENTA);
        NegocioException cat = assertThrows(NegocioException.class, () -> catalogo.criarProduto(enxada));
        assertEquals("fornecedorId", cat.getCampo(), "um fornecedor de SEMENTES não vende ferramentas");

        Produto unidade = produto("Soja", sementes, "90", 5);
        unidade.setUnidade("pacotinho");
        assertThrows(NegocioException.class, () -> catalogo.criarProduto(unidade));

        assertThrows(NegocioException.class, () -> catalogo.criarProduto(produto("MILHO", sementes, "90", 5)),
                "nome repetido no mesmo fornecedor");

        Produto velho = produto("Arroz", sementes, "90", 5);
        velho.setValidade(LocalDate.now().minusDays(1));
        assertThrows(NegocioException.class, () -> catalogo.criarProduto(velho));

        Produto caro = produto("Gergelim", sementes, "99999999", 5);
        assertThrows(NegocioException.class, () -> catalogo.criarProduto(caro));
    }

    @Test
    void reporStockRespeitaOMaximo() {
        catalogo.reporStock(milho.getId(), 40);
        assertEquals(50, milho.getQuantidade());
        assertThrows(NegocioException.class, () -> catalogo.reporStock(milho.getId(), CatalogoService.STOCK_MAXIMO));
    }

    @Test
    void pesquisaIgnoraAcentosEMaiusculas() {
        assertEquals(List.of(adubo), catalogo.pesquisar(null, "FEIJAO"));
        assertEquals(2, catalogo.pesquisar(Categoria.SEMENTE, "").size());
    }

    @Test
    void fornecedorValidaProvinciaETelefoneEPodeSerEditado() {
        assertThrows(NegocioException.class,
                () -> catalogo.criarFornecedor("AgroNorte", TipoFornecedor.SEMENTES, "84 000 0000", "Algarve"));
        assertThrows(NegocioException.class,
                () -> catalogo.criarFornecedor("AgroNorte", TipoFornecedor.SEMENTES, "abc", "Niassa"));
        assertThrows(NegocioException.class,
                () -> catalogo.atualizarFornecedor(sementes.getId(), "Sementes do Sul, Lda", TipoFornecedor.FERRAMENTAS, "84 000 0000", "Maputo Cidade"),
                "não pode mudar para um tipo incompatível com os produtos que já tem");

        catalogo.atualizarFornecedor(sementes.getId(), "Sementes do Sul", TipoFornecedor.SEMENTES, "82 123 4567", "Gaza");
        assertEquals("Gaza", sementes.getProvincia());
        assertEquals("+258 82 123 4567", sementes.getContacto());
    }

    // ---------- Campanhas, cultivos e colheitas ----------

    @Test
    void naoRemoveCampanhaComCultivosNemCultivoComColheitas() {
        Campanha camp = agricola.criarCampanha("Campanha 2026/2027", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 6, 30));
        Cultivo cv = agricola.criarCultivo(camp.getId(), "Feijão nhemba", null, "Macia", new BigDecimal("8"), LocalDate.of(2026, 11, 15));
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
        Campanha camp = agricola.criarCampanha("C1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        agricola.definirFechada(camp.getId(), true);
        assertThrows(NegocioException.class,
                () -> agricola.criarCultivo(camp.getId(), "Milho", null, "", BigDecimal.ONE, LocalDate.of(2026, 2, 1)));
        agricola.definirFechada(camp.getId(), false);
        agricola.criarCultivo(camp.getId(), "Milho", null, "", BigDecimal.ONE, LocalDate.of(2026, 2, 1));
    }

    @Test
    void datasDosCultivosEColheitasDentroDaCampanha() {
        Campanha camp = agricola.criarCampanha("C2", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 6, 30));
        NegocioException plantio = assertThrows(NegocioException.class,
                () -> agricola.criarCultivo(camp.getId(), "Milho", null, "", BigDecimal.ONE, LocalDate.of(2026, 9, 1)));
        assertEquals("dataPlantio", plantio.getCampo());

        Cultivo cv = agricola.criarCultivo(camp.getId(), "Milho", null, "", BigDecimal.ONE, LocalDate.of(2026, 11, 1));
        assertThrows(NegocioException.class, () -> agricola.criarColheita(cv.getId(), LocalDate.of(2028, 3, 1),
                BigDecimal.TEN, BigDecimal.ONE), "mais de 6 meses depois do fim");
        assertThrows(NegocioException.class, () -> agricola.criarColheita(cv.getId(), LocalDate.of(2027, 3, 1),
                new BigDecimal("1.555"), BigDecimal.ONE), "no máximo 2 casas decimais");
        assertThrows(NegocioException.class,
                () -> agricola.atualizarCampanha(camp.getId(), "C2", LocalDate.of(2026, 12, 1), LocalDate.of(2027, 6, 30)),
                "o cultivo de 01/11 ficaria fora das novas datas");
    }

    @Test
    void campanhaValidaNomeDuracaoESobreposicao() {
        Campanha a = agricola.criarCampanha("2026/2027", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 6, 30));
        assertThrows(NegocioException.class,
                () -> agricola.criarCampanha("2026/2027", LocalDate.of(2028, 1, 1), LocalDate.of(2028, 6, 1)));
        assertThrows(NegocioException.class,
                () -> agricola.criarCampanha("Longa", LocalDate.of(2026, 1, 1), LocalDate.of(2029, 1, 1)));
        Campanha b = agricola.criarCampanha("Hortícolas", LocalDate.of(2027, 3, 1), LocalDate.of(2027, 9, 30));
        assertEquals(List.of(a), agricola.sobreposicoes(b));
    }

    @Test
    void culturaDaListaOuOutra() {
        Campanha camp = agricola.criarCampanha("C3", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        assertThrows(NegocioException.class,
                () -> agricola.criarCultivo(camp.getId(), "Inventada", null, "", BigDecimal.ONE, LocalDate.of(2026, 2, 1)));
        Cultivo cv = agricola.criarCultivo(camp.getId(), "Outra", "Pimento", "", BigDecimal.ONE, LocalDate.of(2026, 2, 1));
        assertEquals("Pimento", cv.getCultura());
    }

    @Test
    void relatoriosOrdenamPorVolumeEAssiduidade() {
        Utilizador ana = cliente("Ana Mussa", "ana@teste.mz");
        cliente("Zé Langa", "ze@teste.mz");
        Map<Integer, Integer> umMilho = Map.of(milho.getId(), 1);
        vendas.registarVenda(joao, umMilho, LocalDateTime.now());
        vendas.registarVenda(joao, umMilho, LocalDateTime.now());
        vendas.registarVenda(ana, Map.of(adubo.getId(), 1), LocalDateTime.now());

        assertEquals("Ana Mussa", relatorios.clientesQueCompraram().get(0).getCliente().getNome());
        assertEquals("João Silva", relatorios.maisAssiduos().get(0).getCliente().getNome());
        assertEquals(List.of("Zé Langa"), relatorios.clientesSemCompras().stream().map(Utilizador::getNome).toList());
        assertEquals("Ana Mussa", relatorios.clientesPorNome().get(0).getNome());
    }

    // ---------- Persistência ----------

    @Test
    void persistenciaGravaERestaura(@TempDir Path dir) throws Exception {
        Path ficheiro = dir.resolve("agrogest.ser");
        Persistencia p = new Persistencia(ficheiro);
        p.gravar(DataStore.get());
        p.gravar(DataStore.get());

        assertTrue(Files.exists(ficheiro));
        assertTrue(Files.exists(dir.resolve("agrogest.ser.bak")));
        assertFalse(Files.exists(dir.resolve("agrogest.ser.tmp")));

        DataStore restaurado = p.carregar();
        assertEquals(2, restaurado.getProdutos().size());
        assertEquals("joao@teste.mz", restaurado.getUtilizadores().get(0).getEmail());
        assertEquals(3, restaurado.proximoId(DataStore.Sequencia.PRODUTO));
    }

    @Test
    void persistenciaUsaBackupSeOFicheiroPrincipalEstiverCorrompido(@TempDir Path dir) throws Exception {
        Path ficheiro = dir.resolve("agrogest.ser");
        Persistencia p = new Persistencia(ficheiro);
        p.gravar(DataStore.get());
        p.gravar(DataStore.get());
        Files.writeString(ficheiro, "lixo");

        assertEquals(2, p.carregar().getProdutos().size());
    }

    @Test
    void dadosDeExemploRespeitamTodasAsRegras() {
        DataStore.substituir(DataStore.novoVazio());
        DadosIniciais.carregar();
        assertEquals(3, catalogo.fornecedores().size());
        assertEquals(3, catalogo.produtos().size());
        assertEquals(5, utilizadores.clientes().size());
        assertEquals(4, vendas.vendas().size());
        assertEquals(1, agricola.campanhas().size());
    }
}
