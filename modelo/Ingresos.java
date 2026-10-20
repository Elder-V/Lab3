package modelo;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/** Acumula ingresos separados por registro, por visitas adicionales y por categoría. */
public class Ingresos {
    private double porRegistro;
    private double porVisitas;
    private final Map<String, Double> porCategoria = new TreeMap<>();

    public void registrarCobroRegistro(String categoria, double monto) {
        validar(monto);
        porRegistro += monto;
        porCategoria.merge(categoria, monto, Double::sum);
    }

    public void registrarCobroVisitaAdicional(String categoria, double monto) {
        validar(monto);
        porVisitas += monto;
        porCategoria.merge(categoria, monto, Double::sum);
    }

    private static void validar(double monto) {
        if (!Double.isFinite(monto) || monto <= 0) throw new IllegalArgumentException("Monto inválido.");
    }

    public double getPorRegistro() { return porRegistro; }
    public double getPorVisitas() { return porVisitas; }
    public double getTotal() { return porRegistro + porVisitas; }
    public Map<String, Double> getPorCategoria() { return Collections.unmodifiableMap(porCategoria); }
}
