package parqueecologico.Actividades.ActividadNadoConDelfines;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class AdministradorNadoDelfines implements Runnable {
    private String nombre;
    private ActNadoDelfines actividad;
    private final Lock lock;
    private final Condition cambioHora;

    public AdministradorNadoDelfines(ActNadoDelfines actividad, String nombre, Lock lock, Condition cambioHora){
        this.actividad = actividad;
        this.nombre = nombre;
        this.lock = lock;
        this.cambioHora = cambioHora;
    }

    public void run(){
        Debuger.log(true, Thread.currentThread().getName() + " acaba de iniciar...");
        while (!Parque.estaCerrado()) {             
            lock.lock();
            try {
                cambioHora.await(); // La primera actividad se da a las 10:00.
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
            if(!Parque.estaCerrado()){
                actividad.atenderVisitante();
            }
        }
        actividad.terminarActividad(); // Si el parque cierra mientras la actividad esta en curso, se termina la actividad para que los visitantes puedan salir
        Debuger.log(Parque.MSJ_Salidas, Thread.currentThread().getName() + " se va a su casa porque el parque está cerrado.");
    }
    

}
