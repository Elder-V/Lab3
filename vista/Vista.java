package vista;

import controlador.Controlador;
import modelo.*;

import java.util.*;

/** Vista de consola: muestra el menú, lee entradas de forma segura y presenta resultados. */
public class Vista {
    private final Scanner in = new Scanner(System.in);
    private final Controlador ctl;

    public Vista(Controlador ctl) { this.ctl = ctl; }

    public void iniciar() {
        System.out.println("=== TecniHogar - Gestión de servicios técnicos ===");
        boolean salir = false;
        try {
            while (!salir) {
                mostrarMenu();
                int op = leerEntero("Opción");
                try {
                    salir = ejecutar(op);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println(">> Operación rechazada: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nFin de la entrada. Cerrando el programa.");
        }
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println("\n----- MENÚ -----");
        System.out.println(" 1. Cotizar servicio");
        System.out.println(" 2. Registrar orden");
        System.out.println(" 3. Registrar cuadrilla");
        System.out.println(" 4. Crear jornada");
        System.out.println(" 5. Asignar orden a jornada");
        System.out.println(" 6. Retirar orden de jornada");
        System.out.println(" 7. Iniciar jornada");
        System.out.println(" 8. Registrar resultado de visita");
        System.out.println(" 9. Consultar órdenes");
        System.out.println("10. Consultar cuadrillas");
        System.out.println("11. Reporte: órdenes por categoría y estado");
        System.out.println("12. Reporte: ingresos");
        System.out.println("13. Reporte: jornadas no cerradas");
        System.out.println(" 0. Salir");
    }

    private boolean ejecutar(int op) {
        switch (op) {
            case 1: cotizar(); break;
            case 2: registrarOrden(); break;
            case 3: registrarCuadrilla(); break;
            case 4: crearJornada(); break;
            case 5: asignar(); break;
            case 6: retirar(); break;
            case 7: iniciarJornada(); break;
            case 8: registrarResultado(); break;
            case 9: consultarOrdenes(); break;
            case 10: consultarCuadrillas(); break;
            case 11: reporteConteo(); break;
            case 12: reporteIngresos(); break;
            case 13: reporteJornadas(); break;
            case 0: return true;
            default: System.out.println("Opción no válida.");
        }
        return false;
    }

    // ---------- Operaciones ----------

    private void cotizar() {
        OrdenDeServicio o = pedirOrden(false);
        mostrarCotizacion(o);
        System.out.println("(La cotización no registra la orden ni genera ingresos.)");
    }

    private void registrarOrden() {
        OrdenDeServicio o = pedirOrden(true);
        mostrarCotizacion(o);
        if (leerSiNo("¿Confirmar el registro y el cobro?")) {
            ctl.registrarOrden(o);
            System.out.println("Orden " + o.getCodigo() + " registrada (Pendiente). Cobrado: " + monto(o.calcularCosto()));
        } else {
            System.out.println("Registro cancelado. No se modificó nada.");
        }
    }

    private OrdenDeServicio pedirOrden(boolean conCodigo) {
        System.out.println("Categorías: 1) Diagnóstico  2) Instalación  3) Instalación eléctrica certificada  4) Mantenimiento de aire acondicionado");
        int cat = leerEntero("Categoría (1-4)");
        if (cat < 1 || cat > 4) throw new IllegalArgumentException("Categoría inválida.");
        String codigo = "COTIZACION";
        if (conCodigo) {
            codigo = leerTexto("Código de la orden");
            ctl.verificarCodigoOrdenDisponible(codigo);
        }
        String cliente = leerTexto("Nombre del cliente");
        String dir = leerTexto("Dirección");
        double horas = leerDecimal("Horas estimadas");
        Zona zona = leerZona();
        double[] extras = new double[2];
        switch (cat) {
            case Controlador.CAT_INSTALACION:
                extras[0] = leerEntero("Cantidad de puntos"); break;
            case Controlador.CAT_ELECTRICA:
                extras[0] = leerEntero("Cantidad de puntos");
                extras[1] = leerDecimal("Valor declarado de materiales (Q)"); break;
            case Controlador.CAT_MANT_AIRE:
                extras[0] = leerEntero("Capacidad del equipo (BTU)"); break;
            default: break;
        }
        return ctl.construirOrden(cat, codigo, cliente, dir, horas, zona, extras);
    }

    private void mostrarCotizacion(OrdenDeServicio o) {
        System.out.println("--- " + o.getCategoria() + " ---");
        System.out.println("Costo total: " + monto(o.calcularCosto()));
        System.out.println("Tiempo estimado de atención: " + o.calcularDiasAtencion() + " día(s) hábil(es)");
    }

    private void registrarCuadrilla() {
        String codigo = leerTexto("Código de la cuadrilla");
        double cap = leerDecimal("Capacidad máxima de horas por jornada");
        boolean cert = leerSiNo("¿Certificada en refrigeración?");
        ctl.registrarCuadrilla(codigo, cap, cert);
        System.out.println("Cuadrilla registrada.");
    }

    private void crearJornada() {
        consultarCuadrillas();
        Jornada j = ctl.crearJornada(leerTexto("Código de la cuadrilla"));
        System.out.println("Jornada creada: " + j.getId() + " (En preparación).");
    }

    private void asignar() {
        reporteJornadas();
        System.out.println("Órdenes pendientes:");
        listar(ctl.ordenesPendientes());
        String j = leerTexto("ID de la jornada");
        String o = leerTexto("Código de la orden");
        ctl.asignarOrden(j, o);
        System.out.println("Orden asignada. " + ctl.buscarJornada(j));
    }

    private void retirar() {
        String j = leerTexto("ID de la jornada");
        String o = leerTexto("Código de la orden");
        ctl.retirarOrden(j, o);
        System.out.println("Orden retirada; vuelve a estar pendiente.");
    }

    private void iniciarJornada() {
        String j = leerTexto("ID de la jornada");
        double cargos = ctl.iniciarJornada(j);
        System.out.println("Jornada iniciada; sus órdenes están En atención.");
        if (cargos > 0) System.out.println("Cargos por visitas adicionales registrados: " + monto(cargos));
    }

    private void registrarResultado() {
        String j = leerTexto("ID de la jornada");
        Jornada jor = ctl.buscarJornada(j);
        System.out.println("Órdenes de la jornada:");
        listar(jor.getOrdenes());
        String o = leerTexto("Código de la orden");
        boolean ok = leerSiNo("¿La visita fue completada? (n = visita fallida)");
        OrdenDeServicio orden = ctl.registrarResultado(j, o, ok);
        System.out.println("Resultado registrado. Estado de la orden: " + orden.getEstado()
                + (orden.getEstado() == Estado.CANCELADA ? " (agotó sus visitas)" : ""));
        if (jor.getEstado() == EstadoJornada.CERRADA) {
            System.out.println("La jornada " + jor.getId() + " quedó cerrada; la cuadrilla está libre.");
        }
    }

    private void consultarOrdenes() {
        if (ctl.getOrdenes().isEmpty()) { System.out.println("No hay órdenes."); return; }
        listar(ctl.getOrdenes());
    }

    private void consultarCuadrillas() {
        if (ctl.getCuadrillas().isEmpty()) { System.out.println("No hay cuadrillas."); return; }
        for (Cuadrilla c : ctl.getCuadrillas()) {
            Optional<Jornada> j = ctl.jornadaAbiertaDe(c);
            System.out.println(" - " + c + " | " + (j.isPresent() ? "Ocupada en " + j.get().getId() : "Libre"));
        }
    }

    // ---------- Reportes ----------

    private void reporteConteo() {
        Map<String, Map<Estado, Integer>> r = ctl.conteoPorCategoriaYEstado();
        System.out.println("--- Órdenes por categoría y estado ---");
        for (Map.Entry<String, Map<Estado, Integer>> e : r.entrySet()) {
            int total = 0;
            StringBuilder sb = new StringBuilder();
            for (Estado s : Estado.values()) {
                int n = e.getValue().getOrDefault(s, 0);
                total += n;
                sb.append(s).append(": ").append(n).append("  ");
            }
            System.out.println(e.getKey() + " (total " + total + ")\n    " + sb.toString().trim());
        }
    }

    private void reporteIngresos() {
        System.out.println("--- Ingresos ---");
        System.out.println("Por registro:           " + monto(ctl.getIngresoPorRegistro()));
        System.out.println("Por visitas adicionales: " + monto(ctl.getIngresoPorVisitas()));
        System.out.println("TOTAL:                  " + monto(ctl.getIngresoTotal()));
        System.out.println("Por categoría:");
        if (ctl.getIngresoPorCategoria().isEmpty()) System.out.println("  (sin ingresos)");
        for (Map.Entry<String, Double> e : ctl.getIngresoPorCategoria().entrySet()) {
            System.out.println("  " + e.getKey() + ": " + monto(e.getValue()));
        }
    }

    private void reporteJornadas() {
        List<Jornada> abiertas = ctl.jornadasNoCerradas();
        System.out.println("--- Jornadas no cerradas ---");
        if (abiertas.isEmpty()) System.out.println("  (ninguna)");
        for (Jornada j : abiertas) System.out.println(" - " + j);
    }

    // ---------- Utilidades de entrada y formato ----------

    private void listar(List<OrdenDeServicio> lista) {
        if (lista.isEmpty()) System.out.println("  (ninguna)");
        for (OrdenDeServicio o : lista) System.out.println(" - " + o);
    }

    private static String monto(double v) { return String.format(Locale.US, "Q%.2f", v); }

    private String leerTexto(String prompt) {
        System.out.print(prompt + ": ");
        return in.nextLine().trim();
    }

    private int leerEntero(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: ingrese un número entero.");
            }
        }
    }

    private double leerDecimal(String prompt) {
        while (true) {
            try {
                double v = Double.parseDouble(leerTexto(prompt).replace(',', '.'));
                if (Double.isFinite(v)) return v;
            } catch (NumberFormatException e) {
                // se vuelve a pedir
            }
            System.out.println("Entrada inválida: ingrese un número (puede tener decimales).");
        }
    }

    private boolean leerSiNo(String prompt) {
        while (true) {
            String s = leerTexto(prompt + " (s/n)").toLowerCase();
            if (s.equals("s") || s.equals("si") || s.equals("sí")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Responda 's' o 'n'.");
        }
    }

    private Zona leerZona() {
        while (true) {
            int z = leerEntero("Zona (1 = Metropolitana, 2 = Periférica)");
            if (z == 1) return Zona.METROPOLITANA;
            if (z == 2) return Zona.PERIFERICA;
            System.out.println("Zona inválida.");
        }
    }
}
