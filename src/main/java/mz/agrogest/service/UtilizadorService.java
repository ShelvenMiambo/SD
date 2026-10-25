package mz.agrogest.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import mz.agrogest.model.Perfil;
import mz.agrogest.model.Utilizador;
import mz.agrogest.repo.DataStore;
import mz.agrogest.repo.DataStore.Sequencia;
import mz.agrogest.util.Opcoes;
import mz.agrogest.util.PasswordUtil;
import mz.agrogest.util.Validacao;

public class UtilizadorService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    /** Mínimo de 4 caracteres: o exemplo do enunciado usa "1234". */
    public static final int PASSWORD_MINIMA = 4;
    private static final int PASSWORD_MAXIMA = 72;

    /** Devolve o utilizador se o email e a palavra-passe estiverem corretos e a conta estiver ativa. */
    public Optional<Utilizador> autenticar(String email, String password) {
        return porEmail(email)
                .filter(Utilizador::isAtivo)
                .filter(u -> PasswordUtil.verificar(password, u.getPasswordHash()));
    }

    public Utilizador registarCliente(String nome, String email, String password,
                                      String telefone, String provincia, String endereco) {
        String nomeOk = Validacao.nomePessoa(nome, "nome");
        String emailOk = validarEmail(email);
        validarPassword(password, "password");
        String telefoneOk = Validacao.telefone(telefone, "telefone");
        String provinciaOk = validarProvincia(provincia);
        String enderecoOk = Validacao.texto(endereco, 200, "endereço", "endereco");
        return criar(nomeOk, emailOk, password, telefoneOk, provinciaOk, enderecoOk, Perfil.CLIENTE);
    }

    public Utilizador criarAdmin(String nome, String email, String password) {
        return criar(Validacao.texto(nome, 100, "nome", "nome"), validarEmail(email), password, "", "", "", Perfil.ADMIN);
    }

    private Utilizador criar(String nome, String email, String password, String telefone,
                             String provincia, String endereco, Perfil perfil) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            if (porEmail(email).isPresent()) {
                throw new NegocioException("Já existe uma conta com o email " + email + ".", "email");
            }
            Utilizador u = new Utilizador(ds.proximoId(Sequencia.UTILIZADOR), nome, email,
                    PasswordUtil.hash(password), perfil, telefone, endereco);
            u.setProvincia(provincia);
            ds.getUtilizadores().add(u);
            return u;
        }
    }

    /** O cliente altera os seus dados de contacto. O email não muda (é o identificador da conta). */
    public Utilizador atualizarPerfil(int id, String nome, String telefone, String provincia, String endereco) {
        String nomeOk = Validacao.nomePessoa(nome, "nome");
        String telefoneOk = Validacao.telefone(telefone, "telefone");
        String provinciaOk = validarProvincia(provincia);
        String enderecoOk = Validacao.texto(endereco, 200, "endereço", "endereco");
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Utilizador u = porId(id).orElseThrow(() -> new NegocioException("Conta não encontrada."));
            u.setNome(nomeOk);
            u.setTelefone(telefoneOk);
            u.setProvincia(provinciaOk);
            u.setEndereco(enderecoOk);
            return u;
        }
    }

    public void alterarPassword(int id, String atual, String nova, String confirmacao) {
        DataStore ds = DataStore.get();
        synchronized (ds) {
            Utilizador u = porId(id).orElseThrow(() -> new NegocioException("Conta não encontrada."));
            if (!PasswordUtil.verificar(atual, u.getPasswordHash())) {
                throw new NegocioException("A palavra-passe actual está errada.", "atual");
            }
            validarPassword(nova, "nova");
            if (!nova.equals(confirmacao)) {
                throw new NegocioException("A confirmação não coincide com a nova palavra-passe.", "confirmacao");
            }
            if (PasswordUtil.verificar(nova, u.getPasswordHash())) {
                throw new NegocioException("A nova palavra-passe tem de ser diferente da actual.", "nova");
            }
            u.setPasswordHash(PasswordUtil.hash(nova));
        }
    }

    private static String validarEmail(String email) {
        String e = email == null ? "" : email.trim().toLowerCase();
        if (e.length() > 120 || !EMAIL.matcher(e).matches()) {
            throw new NegocioException("Email inválido.", "email");
        }
        return e;
    }

    private static void validarPassword(String password, String campo) {
        if (password == null || password.length() < PASSWORD_MINIMA) {
            throw new NegocioException("A palavra-passe deve ter pelo menos " + PASSWORD_MINIMA + " caracteres.", campo);
        }
        if (password.length() > PASSWORD_MAXIMA) {
            throw new NegocioException("A palavra-passe tem no máximo " + PASSWORD_MAXIMA + " caracteres.", campo);
        }
        if (password.isBlank()) {
            throw new NegocioException("A palavra-passe não pode ser só espaços.", campo);
        }
    }

    private static String validarProvincia(String provincia) {
        if (provincia == null || !Opcoes.PROVINCIAS.contains(provincia)) {
            throw new NegocioException("Escolha a província da lista.", "provincia");
        }
        return provincia;
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
