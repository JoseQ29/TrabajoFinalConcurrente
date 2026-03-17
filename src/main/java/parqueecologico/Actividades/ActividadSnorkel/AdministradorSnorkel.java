package parqueecologico.Actividades.ActividadSnorkel;

import parqueecologico.Parque;
import parqueecologico.Herramientas.Debuger;

public class AdministradorSnorkel implements Runnable {
    private static String nombre;
    private static Snorkel actividad;

    public AdministradorSnorkel(Snorkel standSnorkel, String nombre){
        this.actividad = standSnorkel;
        this.nombre = nombre;
    }

    public void run(){
        Debuger.log(true, Thread.currentThread().getName() + " acaba de iniciar...");
        while (!Parque.estaCerrado()) {             
            actividad.atenderVisitante();
        }
    }
}
