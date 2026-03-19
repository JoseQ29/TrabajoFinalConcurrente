package parqueecologico;


import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
/**
 *
 * @author Razor-PC V.3
 */
public class Colectivo {
    private int horaUltimoViajeRealizado = 0; // Variable para llevar un registro de la hora del último viaje realizado
    private final int capacidadMaxima = 50;
    private int pasajerosActuales = 0;
    private boolean viajeEnCurso = false; // Indica si el colectivo está en viaje o no
    private boolean viajeTerminado = false; // Indica si el viaje ha terminado o no

    public synchronized void iniciarViaje() throws InterruptedException {
        // logica para iniciar el viaje del colectivo
        while ((HoraParque.getHora() == horaUltimoViajeRealizado) || (((pasajerosActuales != 0) && viajeTerminado) ||
                (!Parque.estaCerrado() && pasajerosActuales == 0))) {
            wait();
        }
        if (!Parque.estaCerrado()) {
            horaUltimoViajeRealizado = HoraParque.getHora();
            viajeEnCurso = true;
            Debuger.log(Parque.MSJ_AccionColectivos, Color.azul() + "Viaje iniciado con " + pasajerosActuales + " pasajeros." + Color.reset());
            //System.out.println(Color.azul() + "Viaje iniciado con " + pasajerosActuales + " pasajeros." + Color.reset());
        } else {
            Debuger.log(Parque.MSJ_AccionColectivos, Color.rojo() + "No se puede iniciar el viaje porque el parque está cerrado." + Color.reset());
            //System.out.println(
            //        Color.rojo() + "No se puede iniciar el viaje porque el parque está cerrado." + Color.reset());
        }

    }

    public synchronized void terminarViaje() {
        // logica para terminar el viaje del colectivo
        viajeEnCurso = false;
        viajeTerminado = true;
        notifyAll(); // Notificar a las personas para bajar
        if (!Parque.estaCerrado()) {
            Debuger.log(Parque.MSJ_AccionColectivos, Color.azul() + "Viaje terminado, pasajeros bajando..." + Color.reset());
            //System.out.println(Color.azul() + "Viaje terminado, pasajeros bajando..." + Color.reset());
        }
    }

    public synchronized void subir() throws InterruptedException {
        // logica para simular que una persona sube al colectivo
        while (viajeEnCurso || pasajerosActuales >= capacidadMaxima || ((pasajerosActuales != 0) && viajeTerminado)) {
            wait();
        }
        pasajerosActuales++;
        Debuger.log(Parque.MSJ_AccionColectivos, Color.azul() + Thread.currentThread().getName() + " subió al colectivo. Pasajeros actuales: "
                + pasajerosActuales + Color.reset());
        //System.out.println(Color.azul() + Thread.currentThread().getName() + " subió al colectivo. Pasajeros actuales: "
        //        + pasajerosActuales + Color.reset());
        if (pasajerosActuales == capacidadMaxima) {
            notifyAll(); // Notificar al conductor para iniciar el viaje
        }
    }

    public synchronized boolean bajar() throws InterruptedException {
        // logica para simular que una persona baja del colectivo
        boolean viajo = false;
        while (!viajeTerminado) {
            wait();
        }
        if (!Parque.estaCerrado()) {
            viajo = true;
            pasajerosActuales--;
            Debuger.log(Parque.MSJ_AccionColectivos, Color.azul() + Thread.currentThread().getName() + " bajó del colectivo. Pasajeros actuales: "
                            + pasajerosActuales + Color.reset());
            //System.out.println(
            //        Color.azul() + Thread.currentThread().getName() + " bajó del colectivo. Pasajeros actuales: "
            //                + pasajerosActuales + Color.reset());
            if (pasajerosActuales == 0) {
                viajeTerminado = false; // Reiniciar el estado del viaje para el próximo grupo de pasajeros
                notifyAll(); // Notificar a las personas para subir
            }
        } else {
            Debuger.log(Parque.MSJ_Salidas, Color.rojo() + Thread.currentThread().getName()
                    + " no pudo viajar en colectivo porque el parque está cerrado." + Color.reset());
            //System.out.println(Color.rojo() + Thread.currentThread().getName()
            //        + " no pudo viajar en colectivo porque el parque está cerrado." + Color.reset());
        }
        return viajo;
    }

}
