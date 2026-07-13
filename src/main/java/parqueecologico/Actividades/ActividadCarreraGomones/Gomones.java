package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class Gomones {

    private final int hGomonesParaLargada;

    // Cantidades de gomones
    private int stockIndividuales;
    private int stockDobles;
    private int gomonesDoblesAMedias = 0; // Control de gomones dobles con 1 solo pasajero

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
            String id = "GomónInd(cantidad=" + stockIndividuales + ")";
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " tomó " + id + Color.reset());
            return id;
        } finally {
            lockGomonesIndividuales.unlock();
        }
    }

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
                // Al ser el segundo, no se encarga de registrar el gomon en la largada
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
            String id = "GomónDoble_Nuevo(cantidad=" + stockDobles + "_Asiento1)";
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " inició un nuevo " + id + Color.reset());
            // Al ser el primero en tomarlo, si es el encargado de registrarlo en la largada
            return new ResultadoGomon(id, true);
        } finally {
            lockGomonesDobles.unlock();
        }
    }

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

    public void devolverGomonDoble(String idGomon, boolean esResponsable) {
        // Si es el acompañante (Asiento2), no altera el stock físico del parque
        if (!esResponsable) {
            Debuger.log(Parque.MSJ_GomonesCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " se bajó del " + idGomon + " (pasajero secundario)." + Color.reset());
            return;
        }

        // Solo el creador devuelve el gomon al stock general
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