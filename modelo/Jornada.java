package modelo;

import java.util.*;

/**
 * Jornada de una cuadrilla. Aquí se valida capacidad y compatibilidad (delegando la
 * compatibilidad en la orden) y se determinan las visitas adicionales al iniciar.
 */
public class Jornada {
    private static final double EPS = 1e-9;

    private final String id;
    private final Cuadrilla cuadrilla;
    private final List<OrdenDeServicio> ordenes = new ArrayList<>();
    private final Set<OrdenDeServicio> conResultado = new HashSet<>();
    private EstadoJornada estado = EstadoJornada.EN_PREPARACION;

    public Jornada(String id, Cuadrilla cuadrilla) {
        if (cuadrilla == null) throw new IllegalArgumentException("Debe indicarse la cuadrilla.");
        this.id = id;
        this.cuadrilla = cuadrilla;
    }

    public void asignar(OrdenDeServicio orden) {
        exigirEstado(EstadoJornada.EN_PREPARACION, "asignar órdenes");
        if (orden.getEstado() == Estado.ASIGNADA || orden.getEstado() == Estado.EN_ATENCION) {
            throw new IllegalStateException("La orden " + orden.getCodigo() + " ya está asignada a una jornada.");
        }
        if (orden.getEstado() != Estado.PENDIENTE) {
            throw new IllegalStateException("La orden " + orden.getCodigo() + " está '" + orden.getEstado()
                    + "' y no puede asignarse.");
        }
        if (!orden.esCompatibleCon(cuadrilla)) {
            throw new IllegalArgumentException("La orden " + orden.getCodigo() + " (" + orden.getCategoria()
                    + ") no es compatible con la cuadrilla " + cuadrilla.getCodigo() + ".");
        }
        if (horasAsignadas() + orden.getHorasEstimadas() > cuadrilla.getCapacidadHoras() + EPS) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "La orden %s excede la capacidad de la cuadrilla %s (disponible: %.2f h, requiere: %.2f h).",
                    orden.getCodigo(), cuadrilla.getCodigo(), capacidadDisponible(), orden.getHorasEstimadas()));
        }
        orden.marcarAsignada();
        ordenes.add(orden);
    }

    public void retirar(OrdenDeServicio orden) {
        exigirEstado(EstadoJornada.EN_PREPARACION, "retirar órdenes");
        if (!ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden " + orden.getCodigo() + " no pertenece a la jornada " + id + ".");
        }
        orden.liberar();
        ordenes.remove(orden);
    }

    /**
     * Inicia la jornada. Devuelve las órdenes que generan cargo por visita adicional
     * (el Controlador registra el ingreso; la jornada solo lo determina).
     */
    public List<OrdenDeServicio> iniciar() {
        exigirEstado(EstadoJornada.EN_PREPARACION, "iniciarse");
        if (ordenes.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar una jornada vacía.");
        }
        List<OrdenDeServicio> conCargo = new ArrayList<>();
        for (OrdenDeServicio o : ordenes) {
            o.iniciarVisita();
            if (o.tieneVisitaAdicional()) conCargo.add(o);
        }
        estado = EstadoJornada.EN_ATENCION;
        return conCargo;
    }

    public void registrarResultado(OrdenDeServicio orden, boolean completada) {
        exigirEstado(EstadoJornada.EN_ATENCION, "registrar resultados");
        if (!ordenes.contains(orden)) {
            throw new IllegalArgumentException("La orden " + orden.getCodigo() + " no pertenece a la jornada " + id + ".");
        }
        if (conResultado.contains(orden)) {
            throw new IllegalStateException("La orden " + orden.getCodigo() + " ya tiene resultado en esta jornada.");
        }
        orden.registrarResultado(completada);
        conResultado.add(orden);
        if (conResultado.size() == ordenes.size()) {
            estado = EstadoJornada.CERRADA;
        }
    }

    public double horasAsignadas() {
        double total = 0;
        for (OrdenDeServicio o : ordenes) total += o.getHorasEstimadas();
        return total;
    }

    public double capacidadDisponible() { return cuadrilla.getCapacidadHoras() - horasAsignadas(); }

    private void exigirEstado(EstadoJornada requerido, String accion) {
        if (estado != requerido) {
            throw new IllegalStateException("La jornada " + id + " está '" + estado + "' y no puede "
                    + accion + " (se requiere estado '" + requerido + "').");
        }
    }

    public String getId() { return id; }
    public Cuadrilla getCuadrilla() { return cuadrilla; }
    public EstadoJornada getEstado() { return estado; }
    public List<OrdenDeServicio> getOrdenes() { return Collections.unmodifiableList(ordenes); }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s | Cuadrilla: %s | Estado: %s | Horas asignadas: %.2f | Capacidad disponible: %.2f | Órdenes: %d",
                id, cuadrilla.getCodigo(), estado, horasAsignadas(), capacidadDisponible(), ordenes.size());
    }
}
