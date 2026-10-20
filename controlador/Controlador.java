package controlador;

import modelo.*;

import java.util.*;

/**
 * Controlador del sistema: guarda las colecciones, coordina las operaciones entre
 * órdenes, cuadrillas y jornadas, y es la ÚNICA clase que modifica los ingresos.
 * Toda operación valida antes de modificar; si lanza excepción, el estado previo no cambia.
 */
public class Controlador {
    public static final int CAT_DIAGNOSTICO = 1;
    public static final int CAT_INSTALACION = 2;
    public static final int CAT_ELECTRICA = 3;
    public static final int CAT_MANT_AIRE = 4;

    private final List<OrdenDeServicio> ordenes = new ArrayList<>();
    private final List<Cuadrilla> cuadrillas = new ArrayList<>();
    private final List<Jornada> jornadas = new ArrayList<>();
    private final Ingresos ingresos = new Ingresos();
    private int contadorJornadas = 0;

    // ---------- Órdenes ----------

    /**
     * Construye una orden SIN registrarla (sirve para cotizar y para previsualizar).
     * extras: [0]=puntos (instalaciones) o BTU (aire); [1]=valor de materiales (eléctrica).
     * Este es el único punto de creación: una nueva categoría se agrega aquí.
     */
    public OrdenDeServicio construirOrden(int categoria, String codigo, String cliente, String direccion,
                                          double horas, Zona zona, double... extras) {
        switch (categoria) {
            case CAT_DIAGNOSTICO:
                return new Diagnostico(codigo, cliente, direccion, horas, zona);
            case CAT_INSTALACION:
                return new Instalacion(codigo, cliente, direccion, horas, zona, (int) extras[0]);
            case CAT_ELECTRICA:
                return new InstalacionElectrica(codigo, cliente, direccion, horas, zona, (int) extras[0], extras[1]);
            case CAT_MANT_AIRE:
                return new MantAireAcc(codigo, cliente, direccion, horas, zona, (int) extras[0]);
            default:
                throw new IllegalArgumentException("Categoría inválida.");
        }
    }

    public void verificarCodigoOrdenDisponible(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la orden no puede estar vacío.");
        }
        if (buscarOrdenOpcional(codigo).isPresent()) {
            throw new IllegalArgumentException("Ya existe una orden con el código " + codigo.trim() + ".");
        }
    }

    /** Registra la orden y cobra el monto completo. */
    public void registrarOrden(OrdenDeServicio orden) {
        verificarCodigoOrdenDisponible(orden.getCodigo());
        ordenes.add(orden);
        ingresos.registrarCobroRegistro(orden.getCategoria(), orden.calcularCosto());
    }

    /** Carga datos iniciales: orden pendiente SIN ingresos. */
    public void cargarOrdenInicial(OrdenDeServicio orden) {
        verificarCodigoOrdenDisponible(orden.getCodigo());
        ordenes.add(orden);
    }

    public OrdenDeServicio buscarOrden(String codigo) {
        return buscarOrdenOpcional(codigo).orElseThrow(
                () -> new IllegalArgumentException("No existe una orden con el código " + codigo + "."));
    }

    private Optional<OrdenDeServicio> buscarOrdenOpcional(String codigo) {
        String c = codigo == null ? "" : codigo.trim();
        return ordenes.stream().filter(o -> o.getCodigo().equalsIgnoreCase(c)).findFirst();
    }

    public List<OrdenDeServicio> getOrdenes() { return Collections.unmodifiableList(ordenes); }

    public List<OrdenDeServicio> ordenesPendientes() {
        List<OrdenDeServicio> r = new ArrayList<>();
        for (OrdenDeServicio o : ordenes) if (o.getEstado() == Estado.PENDIENTE) r.add(o);
        return r;
    }

    // ---------- Cuadrillas ----------

    public void registrarCuadrilla(String codigo, double capacidad, boolean certificada) {
        Cuadrilla nueva = new Cuadrilla(codigo, capacidad, certificada);
        if (buscarCuadrillaOpcional(nueva.getCodigo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuadrilla con el código " + nueva.getCodigo() + ".");
        }
        cuadrillas.add(nueva);
    }

    public Cuadrilla buscarCuadrilla(String codigo) {
        return buscarCuadrillaOpcional(codigo).orElseThrow(
                () -> new IllegalArgumentException("No existe una cuadrilla con el código " + codigo + "."));
    }

    private Optional<Cuadrilla> buscarCuadrillaOpcional(String codigo) {
        String c = codigo == null ? "" : codigo.trim();
        return cuadrillas.stream().filter(q -> q.getCodigo().equalsIgnoreCase(c)).findFirst();
    }

    public List<Cuadrilla> getCuadrillas() { return Collections.unmodifiableList(cuadrillas); }

    /** Jornada sin cerrar de la cuadrilla, si existe. */
    public Optional<Jornada> jornadaAbiertaDe(Cuadrilla cuadrilla) {
        return jornadas.stream()
                .filter(j -> j.getCuadrilla() == cuadrilla && j.getEstado() != EstadoJornada.CERRADA)
                .findFirst();
    }

    // ---------- Jornadas ----------

    public Jornada crearJornada(String codigoCuadrilla) {
        Cuadrilla c = buscarCuadrilla(codigoCuadrilla);
        if (jornadaAbiertaDe(c).isPresent()) {
            throw new IllegalStateException("La cuadrilla " + c.getCodigo() + " ya tiene una jornada sin cerrar.");
        }
        Jornada j = new Jornada("J-" + (++contadorJornadas), c);
        jornadas.add(j);
        return j;
    }

    public Jornada buscarJornada(String id) {
        String x = id == null ? "" : id.trim();
        return jornadas.stream().filter(j -> j.getId().equalsIgnoreCase(x)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe la jornada " + id + "."));
    }

    public void asignarOrden(String idJornada, String codigoOrden) {
        buscarJornada(idJornada).asignar(buscarOrden(codigoOrden));
    }

    public void retirarOrden(String idJornada, String codigoOrden) {
        buscarJornada(idJornada).retirar(buscarOrden(codigoOrden));
    }

    /** Inicia la jornada y registra los cargos por visitas adicionales. Devuelve el total cobrado. */
    public double iniciarJornada(String idJornada) {
        List<OrdenDeServicio> conCargo = buscarJornada(idJornada).iniciar();
        double total = 0;
        for (OrdenDeServicio o : conCargo) {
            ingresos.registrarCobroVisitaAdicional(o.getCategoria(), o.getCargoVisitaAdicional());
            total += o.getCargoVisitaAdicional();
        }
        return total;
    }

    public OrdenDeServicio registrarResultado(String idJornada, String codigoOrden, boolean completada) {
        OrdenDeServicio o = buscarOrden(codigoOrden);
        buscarJornada(idJornada).registrarResultado(o, completada);
        return o;
    }

    // ---------- Reportes ----------

    /** Cantidad de órdenes por categoría y estado (agrupa con getCategoria(), sin instanceof). */
    public Map<String, Map<Estado, Integer>> conteoPorCategoriaYEstado() {
        Map<String, Map<Estado, Integer>> r = new TreeMap<>();
        for (OrdenDeServicio o : ordenes) {
            Map<Estado, Integer> m = r.computeIfAbsent(o.getCategoria(), k -> new EnumMap<>(Estado.class));
            m.merge(o.getEstado(), 1, Integer::sum);
        }
        return r;
    }

    public double getIngresoPorRegistro() { return ingresos.getPorRegistro(); }
    public double getIngresoPorVisitas() { return ingresos.getPorVisitas(); }
    public double getIngresoTotal() { return ingresos.getTotal(); }
    public Map<String, Double> getIngresoPorCategoria() { return ingresos.getPorCategoria(); }

    public List<Jornada> jornadasNoCerradas() {
        List<Jornada> r = new ArrayList<>();
        for (Jornada j : jornadas) if (j.getEstado() != EstadoJornada.CERRADA) r.add(j);
        return r;
    }
}
