package parqueecologico.Actividades.ActividadCarreraGomones;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.HoraParque;
import parqueecologico.Parque;

public class Tren {
    private int horaUltimoViajeRealizado = 0; // Variable para llevar un registro de la hora del último viaje realizado
    private final int capacidadMaxima = 15;
    private int pasajerosActuales = 0;
    private boolean viajeEnCurso = false; // Indica si el tren está en viaje o
    private boolean viajeTerminado = false; // Indica si el viaje ha terminado o no

    public synchronized void iniciarViaje() throws InterruptedException {
        // logica para iniciar el viaje del tren
        while ((HoraParque.getHora() == horaUltimoViajeRealizado) || (((pasajerosActuales != 0) && viajeTerminado) ||
                (!Parque.estaCerrado() && pasajerosActuales == 0))) {
                wait();
        }
        if (!Parque.estaCerrado()) {
            horaUltimoViajeRealizado = HoraParque.getHora();
            viajeEnCurso = true;
            Debuger.log(Parque.MSJ_TrenActividadCarreraGomones, Color.cyan() + "Viaje iniciado con " + pasajerosActuales + " pasajeros." + Color.reset());
        } else {
            Debuger.log(Parque.MSJ_TrenActividadCarreraGomones, Color.cyan() + "No se puede iniciar el viaje porque el parque está cerrado." + Color.reset());
        }
    }

    public synchronized void terminarViaje() {
        // logica para terminar el viaje del tren
        if(!viajeTerminado){
            viajeEnCurso = false;
            viajeTerminado = true;
            notifyAll(); // Notificar a las personas para bajar
            Debuger.log(Parque.MSJ_TrenActividadCarreraGomones, Color.cyan() + "Viaje terminado, pasajeros bajando..." + Color.reset());
        }
    }

    public synchronized void subir() throws InterruptedException {
        // logica para simular que una persona sube al tren
        while (viajeEnCurso || pasajerosActuales >= capacidadMaxima || ((pasajerosActuales != 0) && viajeTerminado)) {
            wait();
        }
        pasajerosActuales++;
        Debuger.log(Parque.MSJ_TrenActividadCarreraGomones, Color.cyan() + Thread.currentThread().getName() + " subió al tren. Pasajeros actuales: "
                + pasajerosActuales + Color.reset());
        if (pasajerosActuales == capacidadMaxima) {
            notifyAll(); // Notificar al conductor para iniciar el viaje
        }
    }

    public synchronized void bajar() throws InterruptedException {
        // logica para simular que una persona baja del tren
        while (!viajeTerminado) {
            wait();
        }
        pasajerosActuales--;
        Debuger.log(Parque.MSJ_TrenActividadCarreraGomones, Color.cyan() + Thread.currentThread().getName() + " bajó del tren. Pasajeros actuales: "
                        + pasajerosActuales + Color.reset());
        if (pasajerosActuales == 0) {
            viajeTerminado = false; // Reiniciar el estado de viaje terminado para permitir que otros pasajeros suban
            notifyAll(); // Notificar a las personas para subir
        }
    }
}
