package mz.agrogest.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formatação para as vistas. Estes métodos estão expostos às JSP como funções EL
 * (ver {@code WEB-INF/agrogest.tld}): {@code ${ag:mt(valor)}}, {@code ${ag:data(d)}}, ...
 */
public final class Formatos {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private Formatos() {
    }

    private static DecimalFormat formato(String padrao) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setDecimalSeparator(',');
        s.setGroupingSeparator('.');
        return new DecimalFormat(padrao, s);
    }

    /** 1234.5 → "1.234,50 MT". */
    public static String mt(Object valor) {
        if (valor == null) {
            return "—";
        }
        return formato("#,##0.00").format(valor) + " MT";
    }

    /** Número com até 2 casas decimais: 3200 → "3.200", 8.5 → "8,5". */
    public static String num(Object valor) {
        if (valor == null) {
            return "—";
        }
        return formato("#,##0.##").format(valor);
    }

    public static String data(LocalDate d) {
        return d == null ? "—" : DATA.format(d);
    }

    public static String dataHora(LocalDateTime d) {
        return d == null ? "—" : DATA_HORA.format(d);
    }

    /** Valor para {@code <input type="date">}. */
    public static String iso(LocalDate d) {
        return d == null ? "" : ISO.format(d);
    }

    /**
     * URL da fotografia de um produto.
     * Vazio → fotografia da categoria ({@code /img/produtos/<categoria>.jpg});
     * começado por "/" → relativo à aplicação; caso contrário (http/https) → usado tal como está.
     */
    public static String foto(String fotoUrl, Object categoria, String contexto) {
        String ctx = contexto == null ? "" : contexto;
        if (fotoUrl == null || fotoUrl.isBlank()) {
            String cat = categoria == null ? "semente" : categoria.toString().toLowerCase();
            return ctx + "/img/produtos/" + cat + ".jpg";
        }
        return fotoUrl.startsWith("/") ? ctx + fotoUrl : fotoUrl;
    }

    /** Verdadeiro se a data já passou (produto fora de validade). */
    public static boolean expirado(LocalDate d) {
        return d != null && d.isBefore(LocalDate.now());
    }

    public static BigDecimal zeroSeNulo(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
