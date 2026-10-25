package mz.agrogest.model;

import java.io.Serializable;

public class Fornecedor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private TipoFornecedor tipo;
    private String contacto;
    private String provincia;

    public Fornecedor() {
    }

    public Fornecedor(int id, String nome, TipoFornecedor tipo, String contacto, String provincia) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contacto = contacto;
        this.provincia = provincia;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public TipoFornecedor getTipo() { return tipo; }
    public void setTipo(TipoFornecedor tipo) { this.tipo = tipo; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
}
