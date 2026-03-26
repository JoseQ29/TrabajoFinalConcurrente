package parqueecologico;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Actividades.ActividadMundoAventura.ActMundoAventura;
import parqueecologico.Actividades.ActividadSnorkel.Snorkel;
import parqueecologico.Herramientas.Debuger;

public class HoraParque implements Runnable {

    private final Colectivo colectivo; // Referencia al colectivo para controlar su funcionamiento según el horario del
                                       // parque
    static int hora = 9;
    static int horaCierre = 17;
    private Lock lock = new ReentrantLock();
    private Condition siguienteHora = lock.newCondition();
    private final ActMundoAventura mundoAventura;
    private final Snorkel snorkel;

    public HoraParque(Colectivo colectivo, Lock lock, Condition siguienteHora, ActMundoAventura mundoAventura, Snorkel snorkel) {
        this.colectivo = colectivo;
        this.lock = lock;
        this.siguienteHora = siguienteHora;
        this.mundoAventura = mundoAventura;
        this.snorkel = snorkel;
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
                    snorkel.notificarCierre();
                    mundoAventura.notificarCierreTirolesa();
                    synchronized (colectivo) {
                        colectivo.notifyAll(); // Al cerrar el parque se les notifica a los colectivos paraque terminen
                                               // su funcionamiento
                    }
                    lock.lock();
                    try {
                        siguienteHora.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
            }
        } while (hora <= horaCierre);
        Debuger.log(Parque.MSJ_Salidas, "horaParque termino");
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
            siguienteHora.signalAll();
        } catch (Exception e) {
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