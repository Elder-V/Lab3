package modelo;

/** Mantenimiento de aire acondicionado: solo zona metropolitana, 1 visita, requiere cuadrilla certificada. */
public class MantAireAcc extends OrdenDeServicio {
    private static final int BTU_MIN = 9000;
    private static final int BTU_MAX = 60000;
    private static final double COSTO_POR_HORA = 110.0;
    private static final double CARGO_GAS = 80.0;

    private final int btu;

    public MantAireAcc(String codigo, String nombreCliente, String direccion,
                       double horasEstimadas, Zona zona, int btu) {
        super(codigo, nombreCliente, direccion, horasEstimadas, zona);
        if (zona != Zona.METROPOLITANA) {
            throw new IllegalArgumentException("Los mantenimientos de aire acondicionado solo se aceptan en la zona metropolitana.");
        }
        if (btu < BTU_MIN || btu > BTU_MAX) {
            throw new IllegalArgumentException("La capacidad debe estar entre " + BTU_MIN + " y " + BTU_MAX + " BTU.");
        }
        this.btu = btu;
    }

    public int getBtu() { return btu; }

    @Override
    public double calcularCosto() {
        return tarifaBase() + COSTO_POR_HORA * getHorasEstimadas() + CARGO_GAS;
    }

    @Override
    public int calcularDiasAtencion() { return 2; }

    @Override
    public int getMaxVisitas() { return 1; }

    @Override
    public String getCategoria() { return "Mantenimiento de aire acondicionado"; }

    @Override
    public boolean esCompatibleCon(Cuadrilla cuadrilla) {
        return cuadrilla.isCertificadaRefrigeracion();
    }

    @Override
    protected String detallesEspecificos() { return "BTU: " + btu; }
}
