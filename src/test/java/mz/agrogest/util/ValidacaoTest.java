package mz.agrogest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import mz.agrogest.service.NegocioException;

class ValidacaoTest {

    @Test
    void telefonesMocambicanosSaoNormalizados() {
        assertEquals("+258 84 123 4567", Validacao.telefone("841234567", "t"));
        assertEquals("+258 82 123 4567", Validacao.telefone("+258 82 123 4567", "t"));
        assertEquals("+258 87 123 4567", Validacao.telefone("00258 87-123-4567", "t"));
        assertEquals("+258 21 123 456", Validacao.telefone("21 123 456", "t"));
    }

    @Test
    void telefonesInvalidosSaoRecusados() {
        for (String t : List.of("", "12345", "891234567", "8412345678", "+351 912 345 678", "abc")) {
            assertThrows(NegocioException.class, () -> Validacao.telefone(t, "t"), t);
        }
    }

    @Test
    void nomesDePessoa() {
        assertEquals("Ana Mussa", Validacao.nomePessoa("  Ana   Mussa ", "n"));
        assertEquals("Maria-José D'Almeida", Validacao.nomePessoa("Maria-José D'Almeida", "n"));
        assertThrows(NegocioException.class, () -> Validacao.nomePessoa("Ana", "n"));
        assertThrows(NegocioException.class, () -> Validacao.nomePessoa("Ana 2", "n"));
        assertThrows(NegocioException.class, () -> Validacao.nomePessoa("<script>", "n"));
    }

    @Test
    void paginacao() {
        List<Integer> l = java.util.stream.IntStream.rangeClosed(1, 32).boxed().toList();
        Pagina<Integer> p = Pagina.de(l, 3, 15);
        assertEquals(List.of(31, 32), p.getItens());
        assertEquals(3, p.getTotalPaginas());
        assertEquals(1, Pagina.de(l, -5, 15).getNumero(), "página inválida volta à primeira");
        assertEquals(3, Pagina.de(l, 99, 15).getNumero(), "página a mais vai para a última");
    }
}
