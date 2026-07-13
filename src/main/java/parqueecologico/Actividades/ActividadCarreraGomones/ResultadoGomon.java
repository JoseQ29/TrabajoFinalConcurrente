package parqueecologico.Actividades.ActividadCarreraGomones;

public class ResultadoGomon {
    private final String idGomon;
    private final boolean esEncargadoLargada;

    public ResultadoGomon(String idGomon, boolean esEncargadoLargada) {
        this.idGomon = idGomon;
        this.esEncargadoLargada = esEncargadoLargada;
    }

    public String getIdGomon() { 
        return idGomon;
    }
    public boolean esEncargadoLargada() {
        return esEncargadoLargada;
    }
}