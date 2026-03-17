package parqueecologico.Herramientas;

public class Debuger {
    private static final boolean mensajesHabilitados = true;

    public static void log(boolean habilitado, String mensaje){
        if(habilitado && mensajesHabilitados){
            System.out.println(mensaje);
        }
    }
}
