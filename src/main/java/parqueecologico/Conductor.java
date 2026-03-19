package parqueecologico;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;

/**
 *
 * @author Razor-PC V.3
 */

public class Conductor implements Runnable {

    private final int idConductor; // Identificador del conductor
    private final String nombre; // Nombre del conductor
    private final Colectivo colectivo; // Referencia al colectivo

    public Conductor(int idConductor, String nombre, Colectivo colectivo) {
        this.idConductor = idConductor;
        this.nombre = nombre;
        this.colectivo = colectivo;
    }

    public void run() {
        // logica para simular el trabajo del conductor
        while (!Parque.estaCerrado()) { // El conductor sigue trabajando mientras el parque no esté cerrado
            try {
                colectivo.iniciarViaje();
                Thread.sleep(500); // Simula un viaje de 5 segundos 
                colectivo.terminarViaje();
            } catch (InterruptedException e) {
            }
        }
        Debuger.log(Parque.MSJ_Salidas, Color.rojo() + nombre + " se va a su casa porque el parque está cerrado." + Color.reset());
    }

}
