package mz.agrogest.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Utilizador implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String email;
    private String passwordHash;
    private Perfil perfil;
    private boolean ativo = true;
    private String telefone;
    private String endereco;
    private String provincia;
    private LocalDateTime dataRegisto;

    public Utilizador() {
    }

    public Utilizador(int id, String nome, String email, String passwordHash, Perfil perfil,
                      String telefone, String endereco) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.passwordHash = passwordHash;
        this.perfil = perfil;
        this.telefone = telefone;
        this.endereco = endereco;
        this.dataRegisto = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    /** Endereço com a província, como aparece na factura. */
    public String getEnderecoCompleto() {
        if (provincia == null || provincia.isBlank()) {
            return endereco;
        }
        return endereco == null || endereco.isBlank() ? provincia : endereco + ", " + provincia;
    }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public LocalDateTime getDataRegisto() { return dataRegisto; }
    public void setDataRegisto(LocalDateTime dataRegisto) { this.dataRegisto = dataRegisto; }
}
