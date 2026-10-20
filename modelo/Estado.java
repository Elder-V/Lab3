package modelo;

/** Estados por los que pasa una orden de servicio. */
public enum Estado {
    PENDIENTE("Pendiente"),
    ASIGNADA("Asignada"),
    EN_ATENCION("En atención"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String nombre;

    Estado(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
