package parqueecologico;

public class HoraParque implements Runnable {

    private final Colectivo colectivo; // Referencia al colectivo para controlar su funcionamiento según el horario del
                                       // parque
    static int hora = 9;
    static int horaCierre = 17;

    public HoraParque(Colectivo colectivo) {
        this.colectivo = colectivo;
    }

    public void run() {
        // logica para simular el horario de apertura y cierre del parque
        // El parque abre a las 9:00 y cierra a las 17:00
        // Se simula una hora cada 10 segundos y el parque esta abierto 7 horas.
        Parque.abrirParque();
        do { 
            try {
                Thread.sleep(1000); // Simula el tiempo que el pasa entre hora y hora
                sumarHora();
                System.out.println("Son las " + hora + ":00 pm");
                if (hora == horaCierre) {
                    Parque.cerrarParque();
                    synchronized (colectivo) {
                        colectivo.notifyAll();      // Al cerrar el parque se les notifica a los colectivos paraque terminen su funcionamiento
                    }                    
                }
            } catch (InterruptedException e) {}
        } while (hora < horaCierre);

    }

    private void sumarHora() {
        // Un metodo sincronizado que aumenta en 1 la hora
        hora++;
        synchronized (colectivo) {
            colectivo.notifyAll(); // Notificar a los hilos que están esperando en el colectivo para que puedan
                                   // verificar la hora y actuar en consecuencia
        }
    }

    public static synchronized int getHora() {
        // Un metodo sincronizado que puede consultar la hora actual del parque
        return hora;
    }
    // Ambos metodos son sincronizados para que no se pueda consultar la hora
    // mientras se cambie la hora

}