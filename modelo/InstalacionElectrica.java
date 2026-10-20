package modelo;

import java.util.Locale;

/** Instalación eléctrica certificada: instalación + certificado (Q120) + garantía (5 % de materiales). */
public class InstalacionElectrica extends Instalacion {
    private static final double COSTO_CERTIFICADO = 120.0;
    private static final double PORCENTAJE_GARANTIA = 5.0;

    private final double valorMateriales;

    public InstalacionElectrica(String codigo, String nombreCliente, String direccion,
                                double horasEstimadas, Zona zona, int puntos, double valorMateriales) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona, puntos);
        if (!Double.isFinite(valorMateriales) || valorMateriales < 0) {
            throw new IllegalArgumentException("El valor de los materiales no puede ser negativo.");
        }
        this.valorMateriales = valorMateriales;
    }

    public double getValorMateriales() { return valorMateriales; }

    @Override
    public double calcularCosto() {
        return super.calcularCosto() + COSTO_CERTIFICADO + valorMateriales * PORCENTAJE_GARANTIA / 100.0;
    }

    @Override
    public int calcularDiasAtencion() { return super.calcularDiasAtencion() + 2; }

    @Override
    public int getMaxVisitas() { return 2; }

    @Override
    public String getCategoria() { return "Instalación eléctrica certificada"; }

    @Override
    protected String detallesEspecificos() {
        return super.detallesEspecificos() + String.format(Locale.US, " | Materiales: Q%.2f", valorMateriales);
    }
}
