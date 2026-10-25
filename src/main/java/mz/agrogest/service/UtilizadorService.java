package mz.agrogest.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import mz.agrogest.model.Perfil;
import mz.agrogest.model.Utilizador;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.DataStore.Sequencia;
import mz.agrogest.util.PasswordUtil;

public class UtilizadorService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int PASSWORD_MINIMA = 4;

    /** Devolve o utilizador se o email e a palavra-passe estiverem corretos e a conta estiver ativa. */
    public Optional<Utilizador> autenticar(String email, String password) {
        return porEmail(email)
                .filter(Utilizador::isAtivo)
                .filter(u -> PasswordUtil.verificar(password, u.getPasswordHash()));
    }

    public Utilizador registarCliente(String nome, String email, String password,
                                      String telefone, String endereco) {
        return criar(nome, email, password, telefone, endereco, Perfil.CLIENTE);
    }

    public Utilizador criarAdmin(String nome, String email, String password) {
        return criar(nome, email, password, "", "", Perfil.ADMIN);
    }

    private Utilizador criar(String nome, String email, String password, String telefone,
                             String endereco, Perfil perfil) {
        String emailNorm = email == null ? "" : email.trim().toLowerCase();
        if (nome == null || nome.isBlank()) {
            throw new NegocioException("O nome é obrigatório.");
        }
        if (!EMAIL.matcher(emailNorm).matches()) {
            throw new NegocioException("Email inválido.");
        }
        if (password == null || password.length() < PASSWORD_MINIMA) {
            throw new NegocioException("A palavra-passe deve ter pelo menos " + PASSWORD_MINIMA + " caracteres.");
        }
        if (perfil == Perfil.CLIENTE) {
            if (telefone == null || telefone.isBlank()) {
                throw new NegocioException("O telefone é obrigatório.");
            }
            if (endereco == null || endereco.isBlank()) {
                throw new NegocioException("O endereço é obrigatório.");
            }
        }
        DataStore ds = DataStore.get();
        synchronized (ds) {
            if (porEmail(emailNorm).isPresent()) {
                throw new NegocioException("Já existe uma conta com o email " + emailNorm + ".");
            }
            Utilizador u = new Utilizador(ds.proximoId(Sequencia.UTILIZADOR), nome.trim(), emailNorm,
                    PasswordUtil.hash(password), perfil, telefone.trim(), endereco.trim());
            ds.getUtilizadores().add(u);
            return u;
        }
    }

    public Optional<Utilizador> porEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String e = email.trim().toLowerCase();
        return DataStore.copia(DataStore.get().getUtilizadores()).stream()
                .filter(u -> u.getEmail().equals(e))
                .findFirst();
    }

    public Optional<Utilizador> porId(int id) {
        return DataStore.copia(DataStore.get().getUtilizadores()).stream()
                .filter(u -> u.getId() == id)
                .findFirst();
    }

    public List<Utilizador> clientes() {
        return DataStore.copia(DataStore.get().getUtilizadores()).stream()
                .filter(u -> u.getPerfil() == Perfil.CLIENTE)
                .sorted(Comparator.comparing(Utilizador::getId))
                .toList();
    }

    /** Ativa ou desativa a conta de um cliente. Um cliente desativado não consegue entrar. */
    public Utilizador alternarAtivo(int id) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Utilizador u = porId(id).orElseThrow(() -> new NegocioException("Cliente não encontrado."));
            if (u.isAdmin()) {
                throw new NegocioException("Não é possível desativar o administrador.");
            }
            u.setAtivo(!u.isAtivo());
            return u;
        }
    }
}
