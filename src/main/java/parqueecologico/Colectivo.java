package parqueecologico;

/**
 *
 * @author Razor-PC V.3
 */
public class Colectivo {
    
    private final int capacidadMaxima = 50; // Capacidad máxima del colectivo
    private int pasajerosActuales = 0; // Número de pasajeros actualmente en el colectivo
    private boolean viajeEnCurso = false; // Indica si el colectivo está en viaje o no
    private boolean viajeTerminado = false; // Indica si el viaje ha terminado o no

    public synchronized void iniciarViaje() throws InterruptedException {
        // Lógica para iniciar el viaje del colectivo
        while(pasajerosActuales < capacidadMaxima  || ((pasajerosActuales != 0) && viajeTerminado)){
            wait();
        }
        viajeEnCurso = true;
        viajeTerminado = false;
        System.out.println("Viaje iniciado con " + pasajerosActuales + " pasajeros.");
    }
    
    public synchronized void terminarViaje(){
        // Lógica para terminar el viaje del colectivo
        viajeEnCurso = false;
        viajeTerminado = true;
        System.out.println("Viaje terminado, pasajeros bajando...");
        notifyAll(); // Notificar a las personas para bajar
    }
    
    public synchronized void subir() throws InterruptedException {
        // Lógica para simular que una persona sube al colectivo
        while(viajeEnCurso || (pasajerosActuales >= capacidadMaxima) || ((pasajerosActuales != 0) && viajeTerminado)){
            wait();
        }
        pasajerosActuales++;
        System.out.println(Thread.currentThread().getName() + " subió al colectivo. Pasajeros actuales: " + pasajerosActuales);
        if(pasajerosActuales == capacidadMaxima){
            notifyAll(); // Notificar al conductor para iniciar el viaje
        }
    }
    
    public synchronized void bajar() throws InterruptedException {
        // Lógica para simular que una persona baja del colectivo
        while(!viajeTerminado){
            wait();
        }
        pasajerosActuales--;
        System.out.println(Thread.currentThread().getName() + " bajó del colectivo. Pasajeros actuales: " + pasajerosActuales);
        if(pasajerosActuales == 0){
            viajeTerminado = false; // Reiniciar el estado del viaje para el próximo grupo de pasajeros
            notifyAll(); // Notificar a las personas para subir
        }
    }
    
}
