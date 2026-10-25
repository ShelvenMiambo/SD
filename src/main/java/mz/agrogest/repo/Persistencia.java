package mz.agrogest.repo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Grava e restaura o {@link DataStore} num ficheiro, por serialização Java.
 *
 * <p>Tolerância a falhas:</p>
 * <ul>
 *   <li>Escreve primeiro num ficheiro temporário, força a escrita no disco e só depois o move
 *       por cima do ficheiro final (movimento atómico). Uma falha a meio nunca deixa o ficheiro
 *       principal corrompido.</li>
 *   <li>Antes de substituir, guarda a versão anterior em {@code .bak}. Ao arrancar, se o ficheiro
 *       principal estiver ilegível, tenta o {@code .bak}.</li>
 * </ul>
 */
public final class Persistencia {
    private static final Logger LOG = Logger.getLogger(Persistencia.class.getName());

    private final Path ficheiro;
    private final Path backup;
    private final Path temporario;

    public Persistencia(Path ficheiro) {
        this.ficheiro = ficheiro.toAbsolutePath();
        this.backup = this.ficheiro.resolveSibling(this.ficheiro.getFileName() + ".bak");
        this.temporario = this.ficheiro.resolveSibling(this.ficheiro.getFileName() + ".tmp");
    }

    public Path getFicheiro() {
        return ficheiro;
    }

    public boolean existe() {
        return Files.exists(ficheiro);
    }

    public LocalDateTime ultimaGravacao() {
        try {
            return existe()
                    ? LocalDateTime.ofInstant(Files.getLastModifiedTime(ficheiro).toInstant(), java.time.ZoneId.systemDefault())
                    : null;
        } catch (IOException e) {
            return null;
        }
    }

    public long tamanhoBytes() {
        try {
            return existe() ? Files.size(ficheiro) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    /** Grava o estado actual de forma atómica. */
    public void gravar(DataStore ds) throws IOException {
        Files.createDirectories(ficheiro.getParent());
        synchronized (ds) {
            try (FileOutputStream fos = new FileOutputStream(temporario.toFile());
                 ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(fos))) {
                out.writeObject(ds);
                out.flush();
                fos.getFD().sync();
            }
        }
        if (Files.exists(ficheiro)) {
            Files.copy(ficheiro, backup, StandardCopyOption.REPLACE_EXISTING);
        }
        try {
            Files.move(temporario, ficheiro, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporario, ficheiro, StandardCopyOption.REPLACE_EXISTING);
        }
        LOG.info(() -> "Estado gravado em " + ficheiro);
    }

    /**
     * Lê o estado do ficheiro principal ou, se falhar, do backup.
     *
     * @return o estado lido, ou {@code null} se não houver ficheiro válido.
     */
    public DataStore carregar() {
        for (Path p : new Path[] {ficheiro, backup}) {
            if (!Files.exists(p)) {
                continue;
            }
            try (InputStream in = new BufferedInputStream(Files.newInputStream(p));
                 ObjectInputStream ois = new ObjectInputStream(in)) {
                DataStore ds = (DataStore) ois.readObject();
                LOG.info(() -> "Estado restaurado de " + p);
                return ds;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                LOG.log(Level.WARNING, "Ficheiro de dados ilegível: " + p, e);
            }
        }
        return null;
    }

    /** Elimina o ficheiro gravado e o backup. O estado em memória não é alterado. */
    public boolean eliminar() throws IOException {
        boolean apagou = Files.deleteIfExists(ficheiro);
        Files.deleteIfExists(backup);
        Files.deleteIfExists(temporario);
        return apagou;
    }
}
