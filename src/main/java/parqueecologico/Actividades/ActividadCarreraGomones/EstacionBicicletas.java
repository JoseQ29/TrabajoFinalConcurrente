package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.concurrent.Semaphore;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class EstacionBicicletas {
    public Semaphore semaforoBicicletas; // Semáforo para controlar el acceso a las bicicletas
    
    public EstacionBicicletas(int cantidadBicicletas) {
        this.semaforoBicicletas = new Semaphore(cantidadBicicletas);
    }

    public void tomarBicicleta() throws InterruptedException {
        semaforoBicicletas.acquire(); // Adquirir un permiso del semáforo para tomar una bicicleta
        Debuger.log(Parque.MSJ_BicicletasActividadCarreraGomones, Color.cyan() + Thread.currentThread().getName() + " toma bicicleta. Bicicletas disponibles: " + semaforoBicicletas.availablePermits() + Color.reset());
    }

    public void devolverBicicleta() {
        semaforoBicicletas.release(); // Liberar un permiso del semáforo al devolver una bicicleta
        Debuger.log(Parque.MSJ_BicicletasActividadCarreraGomones, Color.cyan() + Thread.currentThread().getName() + " devuelve bicicleta. Bicicletas disponibles: " + semaforoBicicletas.availablePermits() + Color.reset());
    }
}
