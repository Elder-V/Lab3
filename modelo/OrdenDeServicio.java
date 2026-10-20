package modelo;

import java.util.Locale;

/**
 * Clase padre abstracta de todas las categorías de servicio.
 * Guarda los datos comunes y protege el estado: las transiciones son
 * package-private, de modo que solo Jornada (mismo paquete) puede invocarlas.
 * Cada decisión que depende de la categoría es un método abstracto/sobrescribible.
 */
public abstract class OrdenDeServicio {
    private static final double CARGO_VISITA_ADICIONAL = 40.0;

    private final String codigo;
    private final String nombreCliente;
    private final String direccion;
    private final double horasEstimadas;
    private final Zona zona;
    private Estado estado;
    private int visitas;

    protected OrdenDeServicio(String codigo, String nombreCliente, String direccion,
                              double horasEstimadas, Zona zona) {
        this.codigo = textoObligatorio(codigo, "El código");
        this.nombreCliente = textoObligatorio(nombreCliente, "El nombre del cliente");
        this.direccion = textoObligatorio(direccion, "La dirección");
        if (!Double.isFinite(horasEstimadas) || horasEstimadas <= 0) {
            throw new IllegalArgumentException("Las horas estimadas deben ser un número positivo.");
        }
        if (zona == null) {
            throw new IllegalArgumentException("Debe indicarse la zona.");
        }
        this.horasEstimadas = horasEstimadas;
        this.zona = zona;
        this.estado = Estado.PENDIENTE;
        this.visitas = 0;
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    // ---------- Decisiones que dependen de la categoría ----------
    public abstract double calcularCosto();
    public abstract int calcularDiasAtencion();
    public abstract int getMaxVisitas();
    public abstract String getCategoria();

    /** Por defecto cualquier cuadrilla sirve; las categorías con restricción lo sobrescriben. */
    public boolean esCompatibleCon(Cuadrilla cuadrilla) { return true; }

    /** Datos propios de la categoría para mostrarlos (vacío por defecto). */
    protected String detallesEspecificos() { return ""; }

    /** Tarifa base según la zona (compartida por instalaciones y mantenimientos). */
    protected double tarifaBase() { return zona.getTarifaBase(); }

    public double getCargoVisitaAdicional() { return CARGO_VISITA_ADICIONAL; }

    // ---------- Transiciones de estado (solo Jornada) ----------
    void marcarAsignada() {
        exigirEstado(Estado.PENDIENTE, "asignarse a una jornada");
        estado = Estado.ASIGNADA;
    }

    void liberar() {
        exigirEstado(Estado.ASIGNADA, "retirarse de una jornada");
        estado = Estado.PENDIENTE;
    }

    void iniciarVisita() {
        exigirEstado(Estado.ASIGNADA, "iniciar una visita");
        visitas++;
        estado = Estado.EN_ATENCION;
    }

    void registrarResultado(boolean completada) {
        exigirEstado(Estado.EN_ATENCION, "registrar un resultado");
        if (completada) {
            estado = Estado.COMPLETADA;
        } else if (visitas >= getMaxVisitas()) {
            estado = Estado.CANCELADA;
        } else {
            estado = Estado.PENDIENTE;
        }
    }

    boolean tieneVisitaAdicional() { return visitas > 1; }

    private void exigirEstado(Estado requerido, String accion) {
        if (estado != requerido) {
            throw new IllegalStateException("La orden " + codigo + " está '" + estado
                    + "' y no puede " + accion + " (se requiere estado '" + requerido + "').");
        }
    }

    // ---------- Getters ----------
    public String getCodigo() { return codigo; }
    public String getNombreCliente() { return nombreCliente; }
    public String getDireccion() { return direccion; }
    public double getHorasEstimadas() { return horasEstimadas; }
    public Zona getZona() { return zona; }
    public Estado getEstado() { return estado; }
    public int getVisitas() { return visitas; }

    @Override
    public String toString() {
        String det = detallesEspecificos();
        return String.format(Locale.US,
                "%s | %s | Cliente: %s | Dir: %s | Zona: %s | Horas est.: %.2f%s | Estado: %s | Visitas: %d/%d | Costo: Q%.2f | Atención: %d día(s)",
                codigo, getCategoria(), nombreCliente, direccion, zona, horasEstimadas,
                det.isEmpty() ? "" : " | " + det, estado, visitas, getMaxVisitas(),
                calcularCosto(), calcularDiasAtencion());
    }
}
