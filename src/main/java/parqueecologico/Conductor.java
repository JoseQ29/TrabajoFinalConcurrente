package parqueecologico;    

public class Conductor implements Runnable {
    
    private int idConductor; // Identificador del conductor
    private String nombre; // Nombre del conductor
    private Colectivo colectivo; // Referencia al colectivo

    public Conductor(int idConductor, String nombre, Colectivo colectivo) {
        this.idConductor = idConductor;
        this.nombre = nombre;
        this.colectivo = colectivo;
    }

    public void run(){
        // Lógica para simular el trabajo del conductor
        while (true) {
            try {
                colectivo.iniciarViaje();
                Thread.sleep(5000); // Simula un viaje de 5 segundos   *************REVISAR******
                colectivo.terminarViaje();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }    
            
        }
    }

}
