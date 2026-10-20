import controlador.Controlador;
import modelo.*;
import vista.Vista;

/** Punto de entrada: carga los datos iniciales (sin ingresos) y abre el menú. */
public class Main {
    public static void main(String[] args) {
        Controlador ctl = new Controlador();
        cargarDatosIniciales(ctl);
        new Vista(ctl).iniciar();
    }

    private static void cargarDatosIniciales(Controlador c) {
        Zona M = Zona.METROPOLITANA, P = Zona.PERIFERICA;
        // Diagnósticos (150 / 225)
        c.cargarOrdenInicial(new Diagnostico("D-001", "Ana López", "Zona 10", 1.0, M));
        c.cargarOrdenInicial(new Diagnostico("D-002", "Carlos Pérez", "Mixco", 1.5, P));
        // Instalaciones: puntos > horas (Q460), puntos < horas, fracción de hora
        c.cargarOrdenInicial(new Instalacion("I-001", "María Gómez", "Zona 15", 2.5, M, 5));    // Q460
        c.cargarOrdenInicial(new Instalacion("I-002", "Luis Ramos", "Villa Nueva", 4.0, P, 2)); // 175+360 = Q535
        c.cargarOrdenInicial(new Instalacion("I-003", "Sofía Díaz", "Zona 4", 1.2, M, 1));      // 2 h fact. = Q280
        // Eléctricas certificadas
        c.cargarOrdenInicial(new InstalacionElectrica("E-001", "Jorge Castillo", "Zona 11", 2.5, M, 5, 2000)); // Q680
        c.cargarOrdenInicial(new InstalacionElectrica("E-002", "Elena Marroquín", "San Miguel Petapa", 3.0, P, 4, 1000)); // Q615
        // Mantenimientos de aire (solo metropolitana)
        c.cargarOrdenInicial(new MantAireAcc("M-001", "Pedro Morales", "Zona 9", 2.0, M, 12000)); // Q400
        c.cargarOrdenInicial(new MantAireAcc("M-002", "Rosa Hernández", "Zona 14", 1.5, M, 24000)); // Q345

        c.registrarCuadrilla("C-01", 8.0, false);
        c.registrarCuadrilla("C-02", 6.0, true);
        c.registrarCuadrilla("C-03", 4.0, false);
    }
}
