package parqueecologico;

public class HoraParque implements Runnable {
    
    public void run(){
        //logica para simular el horario de apertura y cierre del parque
        //El parque abre a las 9:00 y cierra a las 17:00
        //Se simula una hora cada 10 segundos y el parque esta abierto 7 horas.
        try {
            Parque.abrirParque();
            Thread.sleep(70000); // Simula el tiempo que el parque esta abierto
            Parque.cerrarParque(); 
        } catch (InterruptedException e) {}
    }

}