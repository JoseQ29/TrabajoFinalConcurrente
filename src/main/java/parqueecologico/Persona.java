package parqueecologico;
import java.util.Random;
/**
 *
 * @author Razor-PC V.3
 */

public class Persona implements Runnable {

    private boolean formaAcceso; // false para particular, true para Tour
    private boolean tienePulsera; // true si tiene pulsera, false si no
    private Colectivo colectivo; // Referencia al colectivo al que pertenece la persona
    
    public Persona(boolean formaAcceso, boolean tienePulsera, Colectivo colectivo) {
        // Constructor para inicializar los atributos de la persona
        this.formaAcceso = formaAcceso;
        this.tienePulsera = tienePulsera;
        this.colectivo = colectivo;
    }

    public void run() {
        // logica para simular el acceso al parque ecológico
        Random random = new Random();
        if(formaAcceso){
            System.out.println(Thread.currentThread().getName() + " accedio al parque a través de un tour.");
            try {
                colectivo.subir();
                colectivo.bajar();
            }catch (InterruptedException e) {}
            
        } else {
            System.out.println(Thread.currentThread().getName() + " accedio al parque de forma particular.");
        }
        
        this.tienePulsera = Parque.ingresarParque();

        while(!Parque.estaCerrado()){ //mientras el parque no este cerrado, la persona disfruta del shop o las actividades
            if(random.nextInt(2) == 0){
                Parque.irShop();
            }else{
                Parque.irActividades();
            }
        }

    }

}
