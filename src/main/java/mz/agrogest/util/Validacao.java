package mz.agrogest.util;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.regex.Pattern;

import mz.agrogest.service.NegocioException;

/** Regras de validação partilhadas pelos serviços. */
public final class Validacao {
    /** Telemóvel (82–87 + 7 dígitos) ou fixo (2 + 7 dígitos), com ou sem o indicativo 258. */
    private static final Pattern TELEMOVEL = Pattern.compile("8[2-7]\\d{7}");
    private static final Pattern FIXO = Pattern.compile("2\\d{7}");
    /** Letras (com acentos), espaço, hífen e apóstrofo. */
    private static final Pattern NOME_PESSOA = Pattern.compile("[\\p{L}][\\p{L} '’-]*[\\p{L}]");

    private Validacao() {
    }

    /**
     * Valida um telefone moçambicano e devolve-o normalizado: "+258 84 123 4567" ou "+258 21 123 456".
     */
    public static String telefone(String valor, String campo) {
        String digitos = valor == null ? "" : valor.replaceAll("[\\s()./-]", "");
        if (digitos.startsWith("+")) {
            digitos = digitos.substring(1);
        }
        if (digitos.startsWith("00258")) {
            digitos = digitos.substring(5);
        } else if (digitos.startsWith("258") && digitos.length() > 9) {
            digitos = digitos.substring(3);
        }
        if (TELEMOVEL.matcher(digitos).matches()) {
            return "+258 " + digitos.substring(0, 2) + " " + digitos.substring(2, 5) + " " + digitos.substring(5);
        }
        if (FIXO.matcher(digitos).matches()) {
            return "+258 " + digitos.substring(0, 2) + " " + digitos.substring(2, 5) + " " + digitos.substring(5);
        }
        throw new NegocioException("Telefone inválido. Use um número moçambicano, por exemplo +258 84 123 4567.", campo);
    }

    /** Nome de pessoa: pelo menos duas palavras, só letras, espaços, hífen e apóstrofo. */
    public static String nomePessoa(String valor, String campo) {
        String nome = valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
        if (nome.length() < 3 || nome.length() > 100) {
            throw new NegocioException("O nome deve ter entre 3 e 100 caracteres.", campo);
        }
        if (!NOME_PESSOA.matcher(nome).matches()) {
            throw new NegocioException("O nome só pode ter letras, espaços, hífen e apóstrofo.", campo);
        }
        if (!nome.contains(" ")) {
            throw new NegocioException("Indique o nome e o apelido.", campo);
        }
        return nome;
    }

    /** Texto obrigatório com comprimento máximo. */
    public static String texto(String valor, int maximo, String rotulo, String campo) {
        String t = valor == null ? "" : valor.trim();
        if (t.isEmpty()) {
            throw new NegocioException("O campo \"" + rotulo + "\" é obrigatório.", campo);
        }
        if (t.length() > maximo) {
            throw new NegocioException("O campo \"" + rotulo + "\" tem no máximo " + maximo + " caracteres.", campo);
        }
        return t;
    }

    /** Valor decimal positivo, até {@code maximo} e com no máximo 2 casas decimais. */
    public static BigDecimal positivo(BigDecimal valor, BigDecimal maximo, String rotulo, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("O campo \"" + rotulo + "\" deve ser maior que zero.", campo);
        }
        if (valor.compareTo(maximo) > 0) {
            throw new NegocioException("O campo \"" + rotulo + "\" não pode passar de " + Formatos.num(maximo) + ".", campo);
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new NegocioException("O campo \"" + rotulo + "\" aceita no máximo 2 casas decimais.", campo);
        }
        return valor;
    }

    /** Normaliza para comparações: minúsculas e sem acentos ("Feijão" → "feijao"). */
    public static String chave(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase().trim();
    }
}
