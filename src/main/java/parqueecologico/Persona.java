package parqueecologico;

import java.util.Random;

/**
 *
 * @author Razor-PC V.3
 */

public class Persona implements Runnable {

    private boolean formaAcceso; // false para particular, true para Tour
    private boolean tienePulsera; // true si tiene pulsera, false si no
    private final Colectivo colectivo; // Referencia al colectivo al que pertenece la persona

    public Persona(boolean formaAcceso, boolean tienePulsera, Colectivo colectivo) {
        // Constructor para inicializar los atributos de la persona
        this.formaAcceso = formaAcceso;
        this.tienePulsera = tienePulsera;
        this.colectivo = colectivo;
    }

    public void run() {
        // logica para simular el acceso al parque ecológico
        boolean viajo = false; // Variable para indicar si la persona realizo el viaje en colectivo o no.
        Random random = new Random();
        if (formaAcceso) {
            System.out.println(Color.amarillo() + Thread.currentThread().getName()
                    + " va a acceder de forma particular." + Color.reset());
            viajo = true;
        } else {
            System.out.println(Color.amarillo() + Thread.currentThread().getName()
                    + " va a acceder a través de un tour." + Color.reset());
            try {
                colectivo.subir();
                viajo = colectivo.bajar();
            } catch (InterruptedException e) {
            }
        }

        if (viajo) {
            this.tienePulsera = Parque.ingresarParque();
            while (!Parque.estaCerrado()) { // mientras el parque no este cerrado, la persona disfruta del shop o las
                                            // actividades
                if (random.nextInt(2) == 0) {
                    Parque.irShop();
                } else {
                    Parque.irActividades();
                }
            }

        }
        System.out.println(Color.rojo() + Thread.currentThread().getName() + " se va del parque." + Color.reset());
    }

}
