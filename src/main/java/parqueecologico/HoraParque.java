package parqueecologico;

public class HoraParque implements Runnable {
    static int hora = 9;
    static int horaCierre = 17;

    public void run() {
        // logica para simular el horario de apertura y cierre del parque
        // El parque abre a las 9:00 y cierra a las 17:00
        // Se simula una hora cada 10 segundos y el parque esta abierto 7 horas.
        Parque.abrirParque();
        while (hora < horaCierre) {
            try {
                Thread.sleep(10000); // Simula el tiempo que el pasa entre hora y hora
                sumarHora();
                System.out.println("Son las " + hora + ":00 pm");
            } catch (InterruptedException e) {}
        }
        Parque.cerrarParque();
    }
    private synchronized void sumarHora(){
        // Un metodo sincronizado que aumenta en 1 la hora
        hora++;
    }

    public synchronized int getHora(){
        // Un metodo sincronizado que puede consultar la hora actual del parque
        return hora;
    }
    // Ambos metodos son sincronizados para que no se pueda consultar la hora mientras se cambie la hora
}