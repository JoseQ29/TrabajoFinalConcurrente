package parqueecologico;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Actividades.ActividadMundoAventura.ActMundoAventura;


public class HoraParque implements Runnable {

    private final Colectivo colectivo; // Referencia al colectivo para controlar su funcionamiento según el horario del
                                       // parque
    static int hora = 9;
    static int horaCierre = 17;
    private Lock lock = new ReentrantLock();
    private Condition siguienteHora = lock.newCondition();
    private static ActMundoAventura mundoAventura;
    
    public HoraParque(Colectivo colectivo, Lock lock, Condition siguienteHora, ActMundoAventura mundoAventura) {
        this.colectivo = colectivo;
        this.lock = lock;
        this.siguienteHora = siguienteHora;
        this.mundoAventura = mundoAventura;
    }

    public void run() {
        // logica para simular el horario de apertura y cierre del parque
        Parque.abrirParque();
        do { 
            try {
                Thread.sleep(1000); // Simula el tiempo que el pasa entre hora y hora
                sumarHora();
                System.out.println("Son las " + hora + ":00 pm");
                if (hora == horaCierre) {
                    Parque.cerrarParque();
                    mundoAventura.notificarCierreTirolesa();
                    synchronized (colectivo) {
                        colectivo.notifyAll();      // Al cerrar el parque se les notifica a los colectivos paraque terminen su funcionamiento
                    }                    
                }
            } catch (InterruptedException e) {}
        } while (hora < horaCierre);

    }

    private void sumarHora() {
        // Un metodo sincronizado que aumenta en 1 la hora
        hora++;
        synchronized (colectivo) {
            colectivo.notifyAll(); // Notificar a los hilos que están esperando en el colectivo para que puedan
                                   // verificar la hora y actuar en consecuencia
        }
        lock.lock();
        try {
                siguienteHora.signalAll(); // Notificar a los hilos que cambio la hora
        } finally {
            lock.unlock();
        }
        
    }

    public static synchronized int getHora() {
        // Un metodo sincronizado que puede consultar la hora actual del parque
        return hora;
    }
    // Ambos metodos son sincronizados para que no se pueda consultar la hora
    // mientras se cambie la hora

}