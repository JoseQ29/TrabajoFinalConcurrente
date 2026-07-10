package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

/**
 * Clase principal que coordina la actividad "Carrera de Gomones por el Río".
 *
 * Flujo de cada participante:
 *   1. Traslado al inicio (bicicleta o tren).
 *   2. tomarBolso()                → espera si no hay; devuelve -1 si el parque cerró.
 *   3. tomarOUnirseGomonDoble()    → toma asiento en un gomón doble existente o
 *      tomarGomonIndividual()        saca un gomón individual del pool.
 *                                    Devuelve null si el parque cerró.
 *   4. getLatchActual()            → captura el latch vigente antes de registrarse.
 *   5. registrarGomonListo()       → countDown del latch de la largada.
 *   6. esperarLargada()            → await hasta que el latch llegue a 0.
 *                                    notificarCierre() lo agota si el parque cierra.
 *   7. bajarRio()
 *   8. devolverGomon*()            (siempre, en finally)
 *   9. devolverBolso()             (siempre, en finally)
 *
 * Mecanismos:
 *   - ReentrantLock + Condition : pools de bolsos y de gomones.
 *   - CountDownLatch            : barrera de largada (se recrea tras cada una).
 *   - Semaphore                 : bicicletas (ya existente).
 *   - Monitor synchronized      : Tren (ya existente).
 */
public class ActCarreraGomones {

    private final BolsoConLlave bolsoConLlave;
    private final Gomones gomones;
    private final EstacionBicicletas estacionBicicletas;
    private final Tren tren;

    public ActCarreraGomones(
            int cantBolsos,
            int cantGomonesIndividuales,
            int cantGomonesDobles,
            int hGomonesParaLargada,
            EstacionBicicletas estacionBicicletas,
            Tren tren) {

        this.bolsoConLlave = new BolsoConLlave(cantBolsos);
        this.gomones = new Gomones(cantGomonesIndividuales, cantGomonesDobles, hGomonesParaLargada);
        this.estacionBicicletas = estacionBicicletas;
        this.tren = tren;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Punto de entrada de la actividad completa para una persona.
     * En cada paso con espera, si el parque cerró el hilo sale limpiamente;
     * los finally garantizan que siempre se devuelvan los recursos tomados.
     */
    public void participar() throws InterruptedException {
        // 1. Traslado
        trasladarse();
        if (Parque.estaCerrado()) return;

        // 2. Bolso
        int numeroBolso = bolsoConLlave.tomarBolso();
        if (numeroBolso == -1) {
            Debuger.log(Parque.MSJ_BolsosCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " no tomó bolso (parque cerrado)." + Color.reset());
            return;
        }

        try {
            // 3. Gomón
            boolean usaGomonDoble = new Random().nextBoolean();
            String idGomon = null;
            boolean debeRegistrarLargada = true; // El individual siempre es responsable de sí mismo

            if (usaGomonDoble) {
                ResultadoGomon resDoble = gomones.tomarOUnirseGomonDoble();
                if (resDoble != null) {
                    idGomon = resDoble.getIdGomon();
                    debeRegistrarLargada = resDoble.esEncargadoLargada(); // True para el primero, False para el segundo
                }
            } else {
                idGomon = gomones.tomarGomonIndividual();
            }

            if (idGomon == null) {
                Debuger.log(Parque.MSJ_GomonesCGomones,
                        Color.violeta() + Thread.currentThread().getName()
                        + " no tomó gomón (parque cerrado)." + Color.reset());
                return; 
            }

            try {
                // 4-6. Largada
                CountDownLatch miLatch = gomones.getLatchActual();
                
                if (debeRegistrarLargada) {
                    gomones.registrarGomonListo(idGomon);
                } else {
                    Debuger.log(Parque.MSJ_GomonesCGomones,
                            Color.violeta() + Thread.currentThread().getName()
                            + " (" + idGomon + ") espera de forma compartida sin duplicar la largada." + Color.reset());
                }
                
                gomones.esperarLargada(miLatch);

                if (!Parque.estaCerrado()) {
                    // 7. Descenso
                    bajarRio();
                }

            } finally {
                // 8. Devolver gomón (CORREGIDO)
                if (usaGomonDoble) {
                    // Le pasamos la bandera para saber si realmente debe restaurar el stock
                    gomones.devolverGomonDoble(idGomon, debeRegistrarLargada);
                } else {
                    gomones.devolverGomonIndividual(idGomon);
                }
            }

        } finally {
            // 9. Devolver bolso
            bolsoConLlave.devolverBolso(numeroBolso);
        }
    }

    /**
     * Llamado por HoraParque al cierre del parque.
     * Propaga la notificación a BolsoConLlave y Gomones para que todos
     * los hilos bloqueados puedan salir sin espera activa.
     */
    public void notificarCierre() {
        bolsoConLlave.notificarCierre();
        gomones.notificarCierre();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void trasladarse() throws InterruptedException {
        if (new Random().nextBoolean()) {
            Debuger.log(Parque.MSJ_BicicletasActividadCarreraGomones,
                    Color.cyan() + Thread.currentThread().getName()
                    + " va en bicicleta al inicio de la carrera." + Color.reset());
            estacionBicicletas.tomarBicicleta();
            Thread.sleep(300);
            estacionBicicletas.devolverBicicleta();
        } else {
            Debuger.log(Parque.MSJ_TrenActividadCarreraGomones,
                    Color.cyan() + Thread.currentThread().getName()
                    + " toma el tren al inicio de la carrera." + Color.reset());
            tren.subir();
            tren.bajar();
        }
        Debuger.log(Parque.MSJ_PersonaActividades,
                Color.violeta() + Thread.currentThread().getName()
                + " llegó al inicio de la Carrera de Gomones." + Color.reset());
    }

    private void bajarRio() throws InterruptedException {
        Debuger.log(Parque.MSJ_PersonaActividades,
                Color.violeta() + Thread.currentThread().getName()
                + " está bajando el río en gomón..." + Color.reset());
        Thread.sleep(400);
        Debuger.log(Parque.MSJ_PersonaActividades,
                Color.violeta() + Thread.currentThread().getName()
                + " terminó la Carrera de Gomones." + Color.reset());
    }
}