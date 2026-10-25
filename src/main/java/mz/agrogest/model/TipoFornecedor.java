package mz.agrogest.model;

import java.util.List;

/**
 * Tipo de fornecedor e as categorias de produto que pode fornecer.
 * FERRAMENTAS foi acrescentado para cobrir a categoria de produto FERRAMENTA.
 */
public enum TipoFornecedor {
    SEMENTES(Categoria.SEMENTE),
    FERTILIZANTES(Categoria.FERTILIZANTE),
    MAQUINARIA(Categoria.MAQUINARIA, Categoria.FERRAMENTA),
    FERRAMENTAS(Categoria.FERRAMENTA);

    private final List<Categoria> categorias;

    TipoFornecedor(Categoria... categorias) {
        this.categorias = List.of(categorias);
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public boolean forneceCategoria(Categoria c) {
        return categorias.contains(c);
    }

    /** Categorias separadas por espaço, para filtrar as opções no formulário. */
    public String getCategoriasTexto() {
        return String.join(" ", categorias.stream().map(Enum::name).toList());
    }
}
