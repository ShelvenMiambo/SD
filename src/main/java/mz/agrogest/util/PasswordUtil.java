package mz.agrogest.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de palavras-passe com PBKDF2-HMAC-SHA256 e sal aleatório.
 * Formato guardado: {@code iteracoes:salBase64:hashBase64}.
 */
public final class PasswordUtil {
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 65_536;
    private static final int TAMANHO_SAL = 16;
    private static final int TAMANHO_CHAVE_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] sal = new byte[TAMANHO_SAL];
        RANDOM.nextBytes(sal);
        byte[] h = pbkdf2(password.toCharArray(), sal, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACOES + ":" + b64.encodeToString(sal) + ":" + b64.encodeToString(h);
    }

    public static boolean verificar(String password, String guardado) {
        if (password == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteracoes = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);
            byte[] calculado = pbkdf2(password.toCharArray(), sal, iteracoes);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, sal, iteracoes, TAMANHO_CHAVE_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível", e);
        }
    }
}
