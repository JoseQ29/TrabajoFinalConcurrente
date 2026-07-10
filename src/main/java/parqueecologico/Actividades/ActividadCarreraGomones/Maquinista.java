package parqueecologico.Actividades.ActividadCarreraGomones;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class Maquinista implements Runnable {
    private final int idMaquinista; // Identificador del maquinista
    private final String nombre; // Nombre del maquinista
    private final Tren tren; // Referencia al tren

    public Maquinista(int idMaquinista, String nombre, Tren tren) {
        this.idMaquinista = idMaquinista;
        this.nombre = nombre;
        this.tren = tren;
    }

    public void run() {
        // logica para simular el trabajo del maquinista
        while (!Parque.estaCerrado()) { // El maquinista sigue trabajando mientras el parque no esté cerrado
            try {
                tren.iniciarViaje();
                Thread.sleep(500); // Simula un viaje de 5 segundos 
                tren.terminarViaje();
            } catch (InterruptedException e) {
            }
        }
        Debuger.log(Parque.MSJ_Salidas, nombre + idMaquinista + " se va a su casa porque el parque está cerrado.");
    }
    
}
