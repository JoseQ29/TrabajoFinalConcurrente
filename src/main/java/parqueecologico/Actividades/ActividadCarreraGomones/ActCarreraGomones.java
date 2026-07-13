package parqueecologico.Actividades.ActividadCarreraGomones;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

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

    public void participar() throws InterruptedException {
        //traslado al inicio de la carrera
        trasladarse();
        if (Parque.estaCerrado()) return;

        // adquirir bolso
        int numeroBolso = bolsoConLlave.tomarBolso();
        if (numeroBolso == -1) {
            Debuger.log(Parque.MSJ_BolsosCGomones,
                    Color.violeta() + Thread.currentThread().getName()
                    + " no tomó bolso (parque cerrado)." + Color.reset());
            return;
        }

        try {
            // adquirir gomon (individual o doble)
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
                // Largada de la carrera
                CountDownLatch miLatch = gomones.getLatchActual();
                
                if (debeRegistrarLargada) {
                    gomones.registrarGomonListo(idGomon);
                } else {
                    //Pasajero 2 del gomon doble
                    Debuger.log(Parque.MSJ_GomonesCGomones,
                            Color.violeta() + Thread.currentThread().getName()
                            + " (" + idGomon + ") espera de forma compartida sin duplicar la largada." + Color.reset());
                }
                
                gomones.esperarLargada(miLatch);

                if (!Parque.estaCerrado()) {
                    // Carrera
                    bajarRio();
                }

            } finally {
                //Devolver gomón
                if (usaGomonDoble) {
                    gomones.devolverGomonDoble(idGomon, debeRegistrarLargada);
                } else {
                    gomones.devolverGomonIndividual(idGomon);
                }
            }

        } finally {
            // Devolver bolso
            bolsoConLlave.devolverBolso(numeroBolso);
        }
    }

    public void notificarCierre() {
        bolsoConLlave.notificarCierre();
        gomones.notificarCierre();
    }

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