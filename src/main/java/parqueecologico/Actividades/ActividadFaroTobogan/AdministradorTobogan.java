package parqueecologico.Actividades.ActividadFaroTobogan;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class AdministradorTobogan implements Runnable {
    private String nombre;
    private ActFaroTobogan actividad;

    public AdministradorTobogan(ActFaroTobogan actividad, String nombre){
        this.nombre = nombre;
        this.actividad = actividad;
    }

    public void run(){
        Debuger.log(true, Thread.currentThread().getName() + " acaba de iniciar...");
        while (!Parque.estaCerrado() || !actividad.estaVacio()) { 
            actividad.atenderVisitantes();
        }
        Debuger.log(Parque.MSJ_Salidas,nombre +
                ", El admin de los toboganes se fué");
    }
}
