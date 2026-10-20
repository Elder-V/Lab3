package modelo;

/** Diagnóstico: tarifa plana por zona, horas estimadas máximo 1.5. */
public class Diagnostico extends OrdenDeServicio {
    private static final double MAX_HORAS = 1.5;

    public Diagnostico(String codigo, String nombreCliente, String direccion,
                       double horasEstimadas, Zona zona) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (horasEstimadas > MAX_HORAS) {
            throw new IllegalArgumentException("Un diagnóstico no puede superar " + MAX_HORAS + " horas estimadas.");
        }
    }

    @Override
    public double calcularCosto() {
        return getZona() == Zona.METROPOLITANA ? 150.0 : 225.0;
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 1 : 2;
    }

    @Override
    public int getMaxVisitas() { return 3; }

    @Override
    public String getCategoria() { return "Diagnóstico"; }
}
