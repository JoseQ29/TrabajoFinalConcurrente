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
    private boolean merienda; // Inidica si tiene habilitada la merienda para consumir
    private boolean almuerzo; // Inidica si tiene habilitado el almuerzo para consumir

    public Persona(boolean formaAcceso, boolean tienePulsera, Colectivo colectivo) {
        // Constructor para inicializar los atributos de la persona
        this.formaAcceso = formaAcceso;
        this.tienePulsera = tienePulsera;
        this.colectivo = colectivo;
        this.merienda = true;
        this.almuerzo = true;
    }

    public void consumirMerienda(){
        merienda = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        Debuger.log(Parque.MSJ_PersonaActividadesRestaurant, Color.violeta() + Thread.currentThread().getName() + " consumió su merienda" + Color.reset());
    }
    
    public void consumirAlmuerzo(){
        almuerzo = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        Debuger.log(Parque.MSJ_PersonaActividadesRestaurant, Color.violeta() + Thread.currentThread().getName() + " consumió su almuerzo" + Color.reset());
    }

    public boolean getMerienda(){
        return merienda;
    }

    public boolean getAlmuerzo(){
        return almuerzo;
    }

    public void run() {
        // logica para simular el acceso al parque ecológico
        boolean viajo = false; // Variable para indicar si la persona realizo el viaje en colectivo o no.
        Random random = new Random();
        if (formaAcceso) {
            Debuger.log(Parque.MSJ_AccionColectivos, Color.amarillo() + Thread.currentThread().getName()
                    + " va a acceder de forma particular." + Color.reset());
            viajo = true;
        } else {
            Debuger.log(Parque.MSJ_AccionColectivos, Color.amarillo() + Thread.currentThread().getName()
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
                if (random.nextBoolean()) { // Si es true se va al shop, si es false se va a alguna de las actividades
                    Parque.irShop();
                } else {
                    if(!this.almuerzo && !this.merienda){// En caso de que no tenga disponible ni el almuerzo, ni la merienda, elige cualquier actividad menos el restaurant
                        int opcion = random.nextInt(4);
                        if (opcion >= 3) {
                            opcion++;
                        }
                        Parque.irActividades(opcion, this);
                    }else{
                        Parque.irActividades(random.nextInt(5), this);
                    }                    
                }
            }

        }
        Debuger.log(Parque.MSJ_Salidas, Color.rojo() + Thread.currentThread().getName() + " se va del parque." + Color.reset());
    }

}
