package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class Gomones {

    private final int hGomonesParaLargada;

    // Pools de gomones
    private int stockIndividuales;
    private int stockDobles;
    private int gomonesDoblesAMedias = 0; // 2) Control de gomones dobles con 1 solo pasajero

    // Conditions para esperar gomón disponible
    private final ReentrantLock lockGomonesIndividuales = new ReentrantLock();
    private final Condition hayGomonIndividual = lockGomonesIndividuales.newCondition();

    private final ReentrantLock lockGomonesDobles = new ReentrantLock();
    private final Condition hayGomonDoble = lockGomonesDobles.newCondition();

    // Latch de largada + lock que lo protege
    private CountDownLatch latchLargada;
    private int gomonesListosActuales = 0;
    private final ReentrantLock lockLargada = new ReentrantLock();

    public Gomones(int cantIndividuales, int cantDobles, int hGomonesParaLargada) {
        this.hGomonesParaLargada = hGomonesParaLargada;
        this.stockIndividuales = cantIndividuales;
        this.stockDobles = cantDobles;
        this.latchLargada = new CountDownLatch(hGomonesParaLargada);
    }

    // ─── Tomar gomones ───────────────────────────────────────────────────────

    public String tomarGomonIndividual() throws InterruptedException {
        lockGomonesIndividuales.lock();
        try {
            while (stockIndividuales == 0 && !Parque.estaCerrado()) {
                hayGomonIndividual.await();
            }
            if (Parque.estaCerrado()) {
                return null;
            }
            stockIndividuales--;
            String id = "GomónInd(stock=" + stockIndividuales + ")";
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " tomó " + id + Color.reset());
            return id;
        } finally {
            lockGomonesIndividuales.unlock();
        }
    }

    /**
     * 2) Nuevo método requerido por ActCarreraGomones
     * Permite unirse a un gomón con un solo pasajero, o iniciar uno nuevo.
     */
    public ResultadoGomon tomarOUnirseGomonDoble() throws InterruptedException {
        lockGomonesDobles.lock();
        try {
            // Si ya hay un gomon doble con una sola persona, se sube inmediatamente
            if (gomonesDoblesAMedias > 0) {
                gomonesDoblesAMedias--;
                String id = "GomónDoble_Compartido(Asiento2)";
                Debuger.log(Parque.MSJ_GomonesCGomones,
                        Color.violeta() + Thread.currentThread().getName()
                        + " se unió a un " + id + Color.reset());
                // IMPORTANTE: Al ser el segundo, NO se encarga de registrar el gomon en la largada
                return new ResultadoGomon(id, false);
            }

            // Si no hay compartidos, tiene que esperar a que haya stock de gomones vacíos
            while (stockDobles == 0 && !Parque.estaCerrado()) {
                hayGomonDoble.await();
                if (gomonesDoblesAMedias > 0 && !Parque.estaCerrado()) {
                    gomonesDoblesAMedias--;
                    return new ResultadoGomon("GomónDoble_Compartido(Asiento2)", false);
                }
            }
            if (Parque.estaCerrado()) {
                return null;
            }

            // Toma uno nuevo completo del stock (ocupa 1 de 2 asientos)
            stockDobles--;
            gomonesDoblesAMedias++;
            String id = "GomónDoble_Nuevo(stock=" + stockDobles + "_Asiento1)";
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " inició un nuevo " + id + Color.reset());
            // IMPORTANTE: Al ser el primero en tomarlo, SÍ es el encargado de registrarlo en la largada
            return new ResultadoGomon(id, true);
        } finally {
            lockGomonesDobles.unlock();
        }
    }

    // Mantener compatibilidad si se llama al método antiguo sin mutación
    public ResultadoGomon tomarGomonDoble() throws InterruptedException {
        return tomarOUnirseGomonDoble();
    }

    // ─── Devolver gomones ────────────────────────────────────────────────────

    public void devolverGomonIndividual(String idGomon) {
        lockGomonesIndividuales.lock();
        try {
            stockIndividuales++;
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " devolvió " + idGomon
                    + ". Individuales disponibles: " + stockIndividuales + Color.reset());
            hayGomonIndividual.signal();
        } finally {
            lockGomonesIndividuales.unlock();
        }
    }

    /**
     * 2) Lógica adaptada para las devoluciones de gomones compartidos/dobles
     */
    /**
     * Devuelve el gomón doble al stock principal.
     * Solo el pasajero responsable (el que inició el gomón) realiza la devolución física.
     */
    public void devolverGomonDoble(String idGomon, boolean esResponsable) {
        // Si es el acompañante (Asiento2), no altera el stock físico del parque
        if (!esResponsable) {
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " se bajó del " + idGomon + " (pasajero secundario)." + Color.reset());
            return;
        }

        // Solo el conductor/creador devuelve el gomon al stock general
        lockGomonesDobles.lock();
        try {
            stockDobles++;
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " devolvió el " + idGomon + " al stock."
                    + " Dobles disponibles: " + stockDobles + Color.reset());
            hayGomonDoble.signal();
        } finally {
            lockGomonesDobles.unlock();
        }
    }

    // ─── Largada ─────────────────────────────────────────────────────────────

    public CountDownLatch getLatchActual() {
        lockLargada.lock();
        try {
            return latchLargada;
        } finally {
            lockLargada.unlock();
        }
    }

    public void registrarGomonListo(String idGomon) {
        lockLargada.lock();
        try {
            gomonesListosActuales++;
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " (" + idGomon + ") listo. "
                    + gomonesListosActuales + "/" + hGomonesParaLargada + Color.reset());

            latchLargada.countDown();

            if (gomonesListosActuales == hGomonesParaLargada) {
                Debuger.log(Parque.MSJ_GomonesCGomones,
                        Color.amarillo() + "¡LARGADA con "
                        + hGomonesParaLargada + " gomones!" + Color.reset());
                gomonesListosActuales = 0;
                latchLargada = new CountDownLatch(hGomonesParaLargada);
            }
        } finally {
            lockLargada.unlock();
        }
    }

    public void esperarLargada(CountDownLatch miLatch) throws InterruptedException {
        Debuger.log(Parque.MSJ_GomonesCGomones,
                Color.violeta() + Thread.currentThread().getName()
                + " espera la largada..." + Color.reset());
        miLatch.await(); 
        Debuger.log(Parque.MSJ_GomonesCGomones,
                Color.violeta() + Thread.currentThread().getName()
                + (Parque.estaCerrado()
                        ? " sale de la espera por cierre del parque."
                        : " ¡A bajar el río!") + Color.reset());
    }

    // ─── Cierre del parque ───────────────────────────────────────────────────

    public void notificarCierre() {
        lockGomonesIndividuales.lock();
        try {
            hayGomonIndividual.signalAll();
        } finally {
            lockGomonesIndividuales.unlock();
        }

        lockGomonesDobles.lock();
        try {
            hayGomonDoble.signalAll();
        } finally {
            lockGomonesDobles.unlock();
        }

        lockLargada.lock();
        try {
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.amarillo() + "Gomones: cierre del parque. Forzando latch a 0 ("
                    + latchLargada.getCount() + " restantes)." + Color.reset());
            while (latchLargada.getCount() > 0) {
                latchLargada.countDown();
            }
        } finally {
            lockLargada.unlock();
        }
    }
}