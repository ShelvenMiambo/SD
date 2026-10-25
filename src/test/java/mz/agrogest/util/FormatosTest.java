package mz.agrogest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import mz.agrogest.model.Categoria;

class FormatosTest {

    @Test
    void fotoVaziaUsaAFotografiaDaCategoria() {
        assertEquals("/agrogest/img/produtos/fertilizante.jpg", Formatos.foto("", Categoria.FERTILIZANTE, "/agrogest"));
    }

    @Test
    void caminhoDaAplicacaoRecebeOContexto() {
        assertEquals("/agrogest/img/produtos/milho.jpg", Formatos.foto("/img/produtos/milho.jpg", Categoria.SEMENTE, "/agrogest"));
    }

    @Test
    void urlExternoFicaIgual() {
        assertEquals("https://exemplo.mz/f.jpg", Formatos.foto("https://exemplo.mz/f.jpg", Categoria.SEMENTE, "/agrogest"));
    }

    @Test
    void formataMeticais() {
        assertEquals("70.400,00 MT", Formatos.mt(new BigDecimal("70400")));
    }
}
