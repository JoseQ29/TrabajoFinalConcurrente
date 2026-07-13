package parqueecologico;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Actividades.ActividadCarreraGomones.ActCarreraGomones;
import parqueecologico.Actividades.ActividadCarreraGomones.Tren;
import parqueecologico.Actividades.ActividadFaroTobogan.ActFaroTobogan;
import parqueecologico.Actividades.ActividadMundoAventura.ActMundoAventura;
import parqueecologico.Actividades.ActividadSnorkel.Snorkel;
import parqueecologico.Herramientas.Debuger;

public class HoraParque implements Runnable {

    private final Colectivo colectivo;
    private final Tren tren;
    static int hora = 9;
    static int horaCierre = 17;
    private Lock lock = new ReentrantLock();
    private Condition siguienteHora = lock.newCondition();
    private final ActMundoAventura mundoAventura;
    private final Snorkel snorkel;
    private final ActCarreraGomones actCarreraGomones;
    private final ActFaroTobogan actFaroTobogan;

    public HoraParque(Colectivo colectivo, Tren tren, Lock lock, Condition siguienteHora,
            ActMundoAventura mundoAventura, Snorkel snorkel, ActCarreraGomones actCarreraGomones,
            ActFaroTobogan actFaroTobogan) {
                
        this.colectivo = colectivo;
        this.lock = lock;
        this.siguienteHora = siguienteHora;
        this.mundoAventura = mundoAventura;
        this.snorkel = snorkel;
        this.tren = tren;
        this.actCarreraGomones = actCarreraGomones;
        this.actFaroTobogan = actFaroTobogan; 
    }

    public void run() {
        Parque.abrirParque();
        do {
            try {
                Thread.sleep(1000); 
                sumarHora();
                System.out.println("Son las " + hora + ":00 pm");
                if (hora == horaCierre) {
                    Parque.cerrarParque();
                    snorkel.notificarCierre();
                    mundoAventura.notificarCierreTirolesa();
                    actCarreraGomones.notificarCierre(); 
                    actFaroTobogan.notificarCierre(); 
                    
                    synchronized (colectivo) {
                        colectivo.notifyAll();
                    }
                    synchronized (tren) {
                        tren.notifyAll();
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
        hora++;
        synchronized (colectivo) {
            colectivo.notifyAll();
        }
        synchronized (tren) {
            tren.notifyAll();
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
        return hora;
    }
}