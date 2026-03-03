/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package parqueecologico;
import java.util.concurrent.Semaphore;
import java.util.Random;
/**
 *
 * @author Razor-PC V.3
 */
public class Parque {

    static final int MOLINETES = 5;
    static Semaphore semCajeros = new Semaphore(2); // Semáforo para controlar el acceso a los cajeros
    static Semaphore semMolinetes = new Semaphore(MOLINETES); // Semáforo para controlar el acceso a los molinetes
    static boolean parqueCerrado = true; // Variable para indicar si el parque está cerrado o no

    public static void main(String[] args) {
        Random random = new Random();

        Thread horaParqueThread = new Thread(new HoraParque(), "Hora Parque");
        horaParqueThread.start(); // Iniciar el hilo que simula el horario del parque   

        Colectivo colectivo = new Colectivo();//crear el colectivo(monitor)

        Thread conductorThread = new Thread(new Conductor(1, "Conductor 1 y 2", colectivo), "Conductor 1");
        conductorThread.start();//iniciar el hilo del conductor

        for(int i = 0; i < 250; i++){//inicializar las personas que van al parque  
            Thread personaThread = new Thread(new Persona(random.nextInt(2),false, colectivo), "Persona " + i);
            personaThread.start();
        }
    }

    public static boolean ingresarParque(){
        // logica para simular el ingreso al parque ecológico
        boolean tienePulsera = true; // simula que la persona recibe la pulsera antes de entrar al parque
            try {
                semMolinetes.acquire(); // Adquirir un permiso para pasar por el molinete
                Thread.sleep(150); 
                System.out.println(Thread.currentThread().getName() + " paso por el molinete.");
                semMolinetes.release(); 
            } catch (InterruptedException e) {}
        return tienePulsera;
    }

    public static void irShop(){
        // logica para simular que la persona va al shop del parque
        System.out.println(Thread.currentThread().getName() + " está en el shop.");
        try { 
            Thread.sleep(8000); // Simula el tiempo que tarda en recorrer el shop
            semCajeros.acquire(); // La persona pasa a pagar
            Thread.sleep(2000); 
            semCajeros.release(); 
        } catch (InterruptedException e) {}
    }

    public static void irActividades(){
        // logica para simular que la persona va a las actividades del parque
        System.out.println(Thread.currentThread().getName() + " está en las actividades.");
        try { 
            Thread.sleep(10000); // Simula el tiempo que tarda en disfrutar de las actividades
        } catch (InterruptedException e) {}
    }

    public static boolean estaCerrado(){
        // return para indicar si el parque está cerrado o no
        return parqueCerrado;
    }

    public static void cerrarParque(){
        // logica para simular el cierre del parque
        parqueCerrado = true;
        System.out.println("El parque ha cerrado.");
    }

    public static void abrirParque(){
        // logica para simular la apertura del parque
        parqueCerrado = false;
        System.out.println("El parque ha abierto.");
    }
}
