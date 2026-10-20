package modelo;

/** Zona de servicio. Conoce su tarifa base (usada por instalaciones y mantenimientos). */
public enum Zona {
    METROPOLITANA("Metropolitana", 100.0),
    PERIFERICA("Periférica", 175.0);

    private final String nombre;
    private final double tarifaBase;

    Zona(String nombre, double tarifaBase) {
        this.nombre = nombre;
        this.tarifaBase = tarifaBase;
    }

    public double getTarifaBase() { return tarifaBase; }

    @Override
    public String toString() { return nombre; }
}
