package modelo;

/** Estados por los que pasa una jornada. */
public enum EstadoJornada {
    EN_PREPARACION("En preparación"),
    EN_ATENCION("En atención"),
    CERRADA("Cerrada");

    private final String nombre;

    EstadoJornada(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
