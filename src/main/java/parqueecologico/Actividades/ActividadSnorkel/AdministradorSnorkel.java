package parqueecologico.Actividades.ActividadSnorkel;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class AdministradorSnorkel implements Runnable {
    private String nombre;
    private Snorkel actividad;

    public AdministradorSnorkel(Snorkel standSnorkel, String nombre){
        this.actividad = standSnorkel;
        this.nombre = nombre;
    }

    public void run(){
        Debuger.log(true, Thread.currentThread().getName() + " acaba de iniciar...");
         while (!actividad.actividadFinalizada) {             
            actividad.atenderVisitante(); // atiende a los visitantes hasta que cierra el parque
        } 
        Debuger.log(Parque.MSJ_Salidas, Thread.currentThread().getName() + " se fue del parque");
    }
}