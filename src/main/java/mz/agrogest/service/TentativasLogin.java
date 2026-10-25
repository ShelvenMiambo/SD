package mz.agrogest.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita tentativas de login falhadas por email: após {@value #MAXIMO} falhas seguidas,
 * a conta fica bloqueada durante {@link #BLOQUEIO}. Protege contra adivinhar palavras-passe.
 */
public final class TentativasLogin {
    public static final int MAXIMO = 5;
    public static final Duration BLOQUEIO = Duration.ofMinutes(1);

    private static final Map<String, Registo> REGISTOS = new ConcurrentHashMap<>();

    private TentativasLogin() {
    }

    private static final class Registo {
        int falhas;
        Instant bloqueadoAte;
    }

    private static String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    /** Segundos que faltam de bloqueio, ou 0 se pode tentar. */
    public static long segundosBloqueado(String email) {
        Registo r = REGISTOS.get(chave(email));
        if (r == null || r.bloqueadoAte == null) {
            return 0;
        }
        long s = Duration.between(Instant.now(), r.bloqueadoAte).getSeconds();
        return Math.max(0, s + 1);
    }

    /** Regista uma falha; devolve quantas tentativas ainda restam antes do bloqueio. */
    public static int registarFalha(String email) {
        Registo r = REGISTOS.computeIfAbsent(chave(email), k -> new Registo());
        synchronized (r) {
            if (r.bloqueadoAte != null && Instant.now().isAfter(r.bloqueadoAte)) {
                r.falhas = 0;
                r.bloqueadoAte = null;
            }
            r.falhas++;
            if (r.falhas >= MAXIMO) {
                r.bloqueadoAte = Instant.now().plus(BLOQUEIO);
                return 0;
            }
            return MAXIMO - r.falhas;
        }
    }

    public static void limpar(String email) {
        REGISTOS.remove(chave(email));
    }
}
