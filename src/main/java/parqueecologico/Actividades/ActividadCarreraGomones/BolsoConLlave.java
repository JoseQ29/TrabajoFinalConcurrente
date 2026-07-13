package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;


public class BolsoConLlave {

    private int bolsosDisponibles;
    private final int totalBolsos;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition hayBolso = lock.newCondition();

    public BolsoConLlave(int cantidadBolsos) {
        this.bolsosDisponibles = cantidadBolsos;
        this.totalBolsos = cantidadBolsos;
    }

    public int tomarBolso() throws InterruptedException {
        lock.lock();
        try {
            while (bolsosDisponibles == 0 && !Parque.estaCerrado()) {
                hayBolso.await();
            }
            if (Parque.estaCerrado()) {
                return -1;
            }
            bolsosDisponibles--;
            int numeroBolso = totalBolsos - bolsosDisponibles; 
            Debuger.log(Parque.MSJ_BolsosCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " tomó un bolso. Bolsos disponibles: " + bolsosDisponibles + Color.reset());
            return numeroBolso;
        } finally {
            lock.unlock();
        }
    }

    public void devolverBolso(int numeroBolso) {
        lock.lock();
        try {
            bolsosDisponibles++;
            Debuger.log(Parque.MSJ_BolsosCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " devolvió el bolso #" + numeroBolso
                    + ". Bolsos disponibles: " + bolsosDisponibles + Color.reset());
            hayBolso.signal(); // Despertar a uno que espera bolso
        } finally {
            lock.unlock();
        }
    }

    public void notificarCierre() {
        lock.lock();
        try {
            hayBolso.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
