package mz.agrogest.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.servlet.http.HttpServletRequest;
import mz.agrogest.service.NegocioException;

/**
 * Leitura e validação dos parâmetros dos formulários.
 * Os erros viram {@link NegocioException} com o nome do campo, para a vista o assinalar.
 */
public final class Parametros {

    private Parametros() {
    }

    public static String texto(HttpServletRequest req, String nome) {
        String v = req.getParameter(nome);
        return v == null ? "" : v.trim();
    }

    public static String obrigatorio(HttpServletRequest req, String nome, String rotulo) {
        String v = texto(req, nome);
        if (v.isEmpty()) {
            throw new NegocioException("O campo \"" + rotulo + "\" é obrigatório.", nome);
        }
        return v;
    }

    public static int inteiro(HttpServletRequest req, String nome, String rotulo) {
        String v = obrigatorio(req, nome, rotulo).replace(" ", "");
        if (!v.matches("-?\\d+")) {
            throw new NegocioException("O campo \"" + rotulo + "\" deve ser um número inteiro (sem casas decimais).", nome);
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new NegocioException("O valor de \"" + rotulo + "\" é demasiado grande.", nome);
        }
    }

    /** Inteiro opcional; devolve {@code padrao} se estiver vazio ou inválido. */
    public static int inteiroOuPadrao(HttpServletRequest req, String nome, int padrao) {
        try {
            return Integer.parseInt(texto(req, nome));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    /** Aceita "320", "320.5" ou "320,5". */
    public static BigDecimal decimal(HttpServletRequest req, String nome, String rotulo) {
        String v = obrigatorio(req, nome, rotulo).replace(" ", "").replace(',', '.');
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new NegocioException("O campo \"" + rotulo + "\" deve ser um número.", nome);
        }
    }

    public static LocalDate data(HttpServletRequest req, String nome, String rotulo) {
        String v = obrigatorio(req, nome, rotulo);
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new NegocioException("O campo \"" + rotulo + "\" tem uma data inválida.", nome);
        }
    }

    public static LocalDate dataOpcional(HttpServletRequest req, String nome, String rotulo) {
        return texto(req, nome).isEmpty() ? null : data(req, nome, rotulo);
    }

    public static <E extends Enum<E>> E enumeracao(HttpServletRequest req, String nome, Class<E> tipo, String rotulo) {
        String v = obrigatorio(req, nome, rotulo);
        try {
            return Enum.valueOf(tipo, v);
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Valor inválido para \"" + rotulo + "\".", nome);
        }
    }
}
