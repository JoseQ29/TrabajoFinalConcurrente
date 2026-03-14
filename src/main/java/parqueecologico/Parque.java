/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package parqueecologico;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Razor-PC V.3
 */
public class Parque {

    static final int MOLINETES = 5;
    //Debug
    public static final boolean MSJ_AccesoParque=false;
    public static final boolean MSJ_AccesoMolinetes = false;
    public static final boolean MSJ_PersonaShop = false;
    public static final boolean MSJ_PersonaActividades = false;
    public static final boolean MSJ_AccionColectivos = false;
    public static final boolean MSJ_Salidas = false;
    //Debug
    static Semaphore semCajeros = new Semaphore(2); // Semáforo para controlar el acceso a los cajeros del shoping
    static Semaphore semMolinetes = new Semaphore(MOLINETES); // Semáforo para controlar el acceso a los molinetes
    static boolean parqueCerrado = true; // Variable para indicar si el parque está cerrado o no

    public static void main(String[] args) {

        System.out.println("\n" + Color.verde() + "Color verde = acceso a los molinetes" + Color.reset());
        System.out.println(Color.amarillo() + "Color amarillo = forma de acceso al parque" + Color.reset());
        System.out.println(Color.cyan() + "Color cyan = persona en el shop" + Color.reset());
        System.out.println(Color.violeta() + "Color violeta = persona en las actividades" + Color.reset());
        System.out.println(Color.azul() + "Color azul = acciones de los colectivos" + Color.reset());

        

        Random random = new Random();

        Colectivo colectivo = new Colectivo();// crear el colectivo(monitor)

        Thread horaParqueThread = new Thread(new HoraParque(colectivo), "Hora Parque");
        horaParqueThread.start(); // Iniciar el hilo que simula el horario del parque

        Thread conductorThread = new Thread(new Conductor(1, "Conductor 1 y 2", colectivo), "Conductor 1");
        conductorThread.start();// iniciar el hilo del conductor

        for(int i = 0; i < 250; i++){//inicializar las personas que van al parque  
            Thread personaThread = new Thread(new Persona(random.nextBoolean(),false, colectivo), "Persona " + i);
            personaThread.start();
        }
    }

    public static boolean ingresarParque() {
        // logica para simular el ingreso al parque ecológico
        boolean tienePulsera = true; // simula que la persona recibe la pulsera antes de entrar al parque
        try {
            semMolinetes.acquire(); // Adquirir un permiso para pasar por el molinete
            Thread.sleep(10);
            Debuger.log(MSJ_AccesoMolinetes, Color.verde() + Thread.currentThread().getName() + " paso por el molinete." + Color.reset());
            //System.out.println(Color.verde() + Thread.currentThread().getName() + " paso por el molinete." + Color.reset());
            semMolinetes.release();
        } catch (InterruptedException e) {
        }
        return tienePulsera;
    }

    public static void irShop() {
        // logica para simular que la persona va al shop del parque
        Debuger.log(MSJ_PersonaShop, Color.cyan() + Thread.currentThread().getName() + " está en el shop." + Color.reset());
        //System.out.println(Color.cyan() + Thread.currentThread().getName() + " está en el shop." + Color.reset());
        try {
            Thread.sleep(500); // Simula el tiempo que tarda en recorrer el shop
            semCajeros.acquire(); // La persona pasa a pagar
            Thread.sleep(500);
            semCajeros.release();
        } catch (InterruptedException e) {
        }
    }

    public static void irActividades() {
        // logica para simular que la persona va a las actividades del parque
        Debuger.log(MSJ_PersonaActividades, Color.violeta() + Thread.currentThread().getName() + " está en las actividades." + Color.reset());
        //System.out.println(Color.violeta() + Thread.currentThread().getName() + " está en las actividades." + Color.reset());
        try {
            Thread.sleep(1000); // Simula el tiempo que tarda en disfrutar de las actividades
        } catch (InterruptedException e) {
        }
    }

    public static boolean estaCerrado() {
        // return para indicar si el parque está cerrado o no
        return parqueCerrado;
    }

    public static void cerrarParque() {
        // logica para simular el cierre del parque
        parqueCerrado = true;
        System.out.println("El parque ha cerrado. Nos vemos mañana...");
    }

    public static void abrirParque() {
        // logica para simular la apertura del parque
        parqueCerrado = false;
        System.out.println("El parque ha abierto.");
    }
}
