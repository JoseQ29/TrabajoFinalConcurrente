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
    
    public Persona(int formaAcceso, boolean tienePulsera, Colectivo colectivo) {
        // Constructor para inicializar los atributos de la persona
        this.formaAcceso = formaAcceso;
        this.tienePulsera = tienePulsera;
        this.colectivo = colectivo;
    }

    public void run() {
        // logica para simular el acceso al parque ecológico
        Random random = new Random();
        if(formaAcceso == 0){
            System.out.println(Thread.currentThread().getName() + " accedio al parque de forma particular.");
        } else {
            System.out.println(Thread.currentThread().getName() + " accedio al parque a través de un tour.");
            try {
                colectivo.subir();
                colectivo.bajar();
            }catch (InterruptedException e) {}
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
