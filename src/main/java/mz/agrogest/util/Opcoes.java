package mz.agrogest.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Listas fechadas usadas nas caixas de selecção e validadas no servidor. */
public final class Opcoes {

    /** As 11 províncias de Moçambique. */
    public static final List<String> PROVINCIAS = List.of(
            "Niassa", "Cabo Delgado", "Nampula", "Zambézia", "Tete", "Manica",
            "Sofala", "Inhambane", "Gaza", "Maputo Província", "Maputo Cidade");

    /** Unidades de venda dos produtos. */
    public static final List<String> UNIDADES = List.of(
            "kg", "saco", "litro", "unidade", "caixa", "tonelada");

    /** Valor especial da lista de culturas que permite escrever outra. */
    public static final String OUTRA = "Outra";

    /** Culturas comuns em Moçambique e algumas variedades conhecidas de cada uma. */
    public static final Map<String, List<String>> VARIEDADES;

    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("Milho", List.of("Matuba", "Sussuma", "Djandza", "PAN 53", "SC 513"));
        m.put("Feijão nhemba", List.of("Macia", "IT-16", "IT-18", "Namuesse"));
        m.put("Feijão manteiga", List.of("Encarnado", "Catarina"));
        m.put("Feijão bóer", List.of("ICEAP 00554", "ICEAP 00557"));
        m.put("Mandioca", List.of("Nikwaha", "Mulaleia", "Eyope", "Orera"));
        m.put("Amendoim", List.of("Nametil", "Mamane", "ICGV-SM 99568"));
        m.put("Arroz", List.of("Chupa", "Limpopo", "ITA 312"));
        m.put("Algodão", List.of("CA 324", "ALBAR SZ 9314"));
        m.put("Caju", List.of("Local", "Enxertado"));
        m.put("Gergelim", List.of("Lindi", "Nicaragua"));
        m.put("Soja", List.of("Storm", "TGx 1740-2F"));
        m.put("Batata-doce", List.of("Irene", "Namanga", "Delvia"));
        m.put("Hortícolas", List.of("Tomate", "Couve", "Cebola", "Repolho", "Alface"));
        VARIEDADES = java.util.Collections.unmodifiableMap(m);
    }

    private Opcoes() {
    }

    public static List<String> getProvincias() { return PROVINCIAS; }
    public static List<String> getUnidades() { return UNIDADES; }
    public static List<String> getCulturas() { return List.copyOf(VARIEDADES.keySet()); }
    public static Map<String, List<String>> getVariedades() { return VARIEDADES; }

    /** As variedades em JSON, para o formulário sugerir as da cultura escolhida. */
    public static String variedadesJson() {
        StringBuilder b = new StringBuilder("{");
        VARIEDADES.forEach((cultura, lista) -> {
            if (b.length() > 1) {
                b.append(',');
            }
            b.append('"').append(cultura).append("\":[");
            b.append(String.join(",", lista.stream().map(v -> "\"" + v + "\"").toList()));
            b.append(']');
        });
        return b.append('}').toString();
    }
}
