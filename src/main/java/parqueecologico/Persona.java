package parqueecologico;
import java.util.Random;
/**
 *
 * @author Razor-PC V.3
 */

public class Persona implements Runnable {

    private int formaAcceso; // 0 para particular, 1 para Tour
    private boolean tienePulsera; // true si tiene pulsera, false si no
    private Colectivo colectivo; // Referencia al colectivo al que pertenece la persona
    
    public Persona(int formaAcceso, boolean tienePulsera, Colectivo colectivo){
        // Constructor para inicializar los atributos de la persona
        this.formaAcceso = formaAcceso;
        this.tienePulsera = tienePulsera;
        this.colectivo = colectivo;
    }

    public void run() {
        // Lógica para simular el acceso al parque ecológico
        //aca va el random para determinar si la persona tiene pulsera o no, y si es particular o tour
            try {
                colectivo.subir();
                colectivo.bajar();
            }catch (InterruptedException e) {}
        
    }
}
