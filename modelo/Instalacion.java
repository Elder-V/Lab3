package modelo;

/** Instalación: tarifa base + Q90 por hora facturable (máx. entre horas estimadas y horas por puntos). */
public class Instalacion extends OrdenDeServicio {
    private static final double HORAS_POR_PUNTO = 0.75;
    private static final double COSTO_POR_HORA = 90.0;

    private final int puntos;

    public Instalacion(String codigo, String nombreCliente, String direccion,
                       double horasEstimadas, Zona zona, int puntos) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (puntos <= 0) {
            throw new IllegalArgumentException("La cantidad de puntos debe ser un entero positivo.");
        }
        this.puntos = puntos;
    }

    public int getPuntos() { return puntos; }

    protected double horasPorPuntos() { return puntos * HORAS_POR_PUNTO; }

    /** Horas facturables, redondeadas hacia arriba (hora o fracción). */
    protected int horasFacturables() {
        return (int) Math.ceil(Math.max(getHorasEstimadas(), horasPorPuntos()));
    }

    @Override
    public double calcularCosto() {
        return tarifaBase() + COSTO_POR_HORA * horasFacturables();
    }

    @Override
    public int calcularDiasAtencion() {
        return getZona() == Zona.METROPOLITANA ? 3 : 5;
    }

    @Override
    public int getMaxVisitas() { return 3; }

    @Override
    public String getCategoria() { return "Instalación"; }

    @Override
    protected String detallesEspecificos() { return "Puntos: " + puntos; }
}
