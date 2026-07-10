package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

/**
 * Administra los bolsos con llave numerados para la Carrera de Gomones.
 *
 * Usa un ReentrantLock + Condition para bloquear a las personas que
 * esperan un bolso cuando no hay disponibles.
 *
 * Cuando el parque cierra, notificarCierre() hace signalAll() sobre la
 * condition, los hilos despiertan, evalúan el while(!parqueCerrado && ...)
 * y salen sin haber tomado bolso (devuelven -1).
 */
public class BolsoConLlave {

    private int bolsosDisponibles;
    private final int totalBolsos;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition hayBolso = lock.newCondition();

    public BolsoConLlave(int cantidadBolsos) {
        this.bolsosDisponibles = cantidadBolsos;
        this.totalBolsos = cantidadBolsos;
    }

    /**
     * La persona toma un bolso con llave al inicio del recorrido.
     * Bloquea si no hay bolsos disponibles.
     * Cuando el parque cierra, despierta y devuelve -1.
     *
     * @return número de bolso asignado, o -1 si el parque cerró mientras esperaba.
     */
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
            int numeroBolso = totalBolsos - bolsosDisponibles; // ID simbólico
            Debuger.log(Parque.MSJ_BolsosCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " tomó un bolso. Bolsos disponibles: " + bolsosDisponibles + Color.reset());
            return numeroBolso;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Al final del recorrido la persona devuelve el bolso al pool.
     */
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

    /**
     * Llamado por HoraParque al cierre del parque.
     * Despierta a todos los hilos bloqueados en tomarBolso() para que
     * evalúen Parque.estaCerrado() y salgan del while.
     */
    public void notificarCierre() {
        lock.lock();
        try {
            hayBolso.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
