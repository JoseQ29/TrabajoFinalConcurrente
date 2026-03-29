/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package parqueecologico;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Actividades.ActividadFaroTobogan.ActFaroTobogan;
import parqueecologico.Actividades.ActividadFaroTobogan.AdministradorTobogan;
import parqueecologico.Actividades.ActividadMundoAventura.ActMundoAventura;
import parqueecologico.Actividades.ActividadMundoAventura.AdministradorTirolesa;
import parqueecologico.Actividades.ActividadNadoConDelfines.ActNadoDelfines;
import parqueecologico.Actividades.ActividadNadoConDelfines.AdministradorNadoDelfines;
import parqueecologico.Actividades.ActividadRestaurante.Restaurant;
import parqueecologico.Actividades.ActividadSnorkel.AdministradorSnorkel;
import parqueecologico.Actividades.ActividadSnorkel.Snorkel;
import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;

/**
 *
 * @author Razor-PC V.3
 */
public class Parque {

    static final int MOLINETES = 5;
    // Debug
    public static final boolean MSJ_AccesoParque = false;
    public static final boolean MSJ_AccesoMolinetes = false;
    public static final boolean MSJ_PersonaShop = false;
    public static final boolean MSJ_PersonaActividades = false;
    public static final boolean MSJ_PersonaActividadesRestaurant = false;
    public static final boolean MSJ_PersonaActividadesSnorkel = false;
    public static final boolean MSJ_AccionColectivos = false;
    public static final boolean MSJ_Salidas = true;
    public static final boolean MSJ_PersonaSale = false;
    public static final boolean MSJ_PersonaActividadesNadoDelfines = false;
    public static final boolean MSJ_PersonaActividadesMundoAventuraCuerdas = false;
    public static final boolean MSJ_PersonaActividadesMundoAventuraSaltos = false;
    public static final boolean MSJ_PersonaActividadesMundoAventuraTirolesa = false;
    public static final boolean MSJ_PersonaActividadesFaroTobogan = true;

    // Debug
    static Semaphore semCajeros = new Semaphore(2); // Semáforo para controlar el acceso a los cajeros del shoping
    static Semaphore semMolinetes = new Semaphore(MOLINETES); // Semáforo para controlar el acceso a los molinetes
    static boolean parqueCerrado = true; // Variable para indicar si el parque está cerrado o no
    static int personaEnParque = 0;
    // Restaurant
    static Restaurant restaurant1 = new Restaurant(10, "Burgerking");
    static Restaurant restaurant2 = new Restaurant(20, "Mostaza");
    static Restaurant restaurant3 = new Restaurant(15, "Mc Donalds");
    // Snorkel
    static Snorkel actSnorkel = new Snorkel(5);
    // Nado con delfines
    static ActNadoDelfines actNadoDelfines = new ActNadoDelfines();
    // Mundo de aventuras
    static ActMundoAventura actMundoAventura = new ActMundoAventura();
    // Faro Toboganes
    static ActFaroTobogan actFaroTobogan = new ActFaroTobogan(5);

    public static void main(String[] args) {

        System.out.println("\n" + Color.verde() + "Color verde = acceso a los molinetes" + Color.reset());
        System.out.println(Color.amarillo() + "Color amarillo = forma de acceso al parque" + Color.reset());
        System.out.println(Color.cyan() + "Color cyan = persona en el shop" + Color.reset());
        System.out.println(Color.violeta() + "Color violeta = persona en las actividades" + Color.reset());
        System.out.println(Color.azul() + "Color azul = acciones de los colectivos" + Color.reset());

        Random random = new Random();

        Colectivo colectivo = new Colectivo();// crear el colectivo(monitor)

        Lock lock = new ReentrantLock();
        Condition siguienteHora = lock.newCondition();
        Thread horaParqueThread = new Thread(
                new HoraParque(colectivo, lock, siguienteHora, actMundoAventura, actSnorkel),
                "Hora Parque");
        horaParqueThread.start(); // Iniciar el hilo que simula el horario del parque

        Thread adminSnorkel1 = new Thread(new AdministradorSnorkel(actSnorkel, "administradorSnorkel 1"),
                "administradorSnorkel 1"); // Se crean y se inician los administradores de la actividad de Snorkel
        Thread adminSnorkel2 = new Thread(new AdministradorSnorkel(actSnorkel, "administradorSnorkel 2"),
                "administradorSnorkel 2");
        adminSnorkel1.start();
        adminSnorkel2.start();

        Thread adminTobogan = new Thread(new AdministradorTobogan(actFaroTobogan, "adminToboganes"),
                "administrador Toboganes");
        adminTobogan.start();

        Thread adminNadoDelfines = new Thread(
                new AdministradorNadoDelfines(actNadoDelfines, "PEPE", lock, siguienteHora),
                "Administrador Nado Delfines");
        adminNadoDelfines.start(); // Iniciar el hilo que simula al administrador

        Thread adminMundoAventuras = new Thread(new AdministradorTirolesa("Chirinos", actMundoAventura));
        adminMundoAventuras.start();

        Thread conductorThread = new Thread(new Conductor(1, "Conductor 1 y 2", colectivo), "Conductor 1");
        conductorThread.start();// iniciar el hilo del conductor

        for (int i = 0; i < 250; i++) {// inicializar las personas que van al parque
            Thread personaThread = new Thread(new Persona(random.nextBoolean(), false, colectivo), "Persona " + i);
            personaThread.start();
        }

    }

    public synchronized static void personaSale() {
        personaEnParque--;
        Debuger.log(MSJ_PersonaSale,
                Thread.currentThread().getName() + "se va del parque" + "PERSONAS EN EL PARQUE: " + personaEnParque);
    }

    public synchronized static void personaEntra() {
        personaEnParque++;
        Debuger.log(MSJ_PersonaSale, "PERSONAS EN EL PARQUE: " + personaEnParque);
    }

    public static boolean ingresarParque() {
        // logica para simular el ingreso al parque ecológico
        boolean tienePulsera = true; // simula que la persona recibe la pulsera antes de entrar al parque
        try {
            semMolinetes.acquire(); // Adquirir un permiso para pasar por el molinete
            Thread.sleep(10);
            Debuger.log(MSJ_AccesoMolinetes,
                    Color.verde() + Thread.currentThread().getName() + " paso por el molinete y en el parque hay: "
                            + personaEnParque + " personas." + Color.reset());

            // System.out.println(Color.verde() + Thread.currentThread().getName() + " paso
            // por el molinete." + Color.reset());
            semMolinetes.release();
        } catch (InterruptedException e) {
        }
        return tienePulsera;
    }

    public static void irShop() {
        // logica para simular que la persona va al shop del parque
        Debuger.log(MSJ_PersonaShop,
                Color.cyan() + Thread.currentThread().getName() + " está en el shop." + Color.reset());
        try {
            Thread.sleep(500); // Simula el tiempo que tarda en recorrer el shop
            semCajeros.acquire(); // La persona pasa a pagar
            Thread.sleep(500);
            semCajeros.release();
        } catch (InterruptedException e) {
        }
    }

    public static void irActividades(int opcion, Persona visitante) {
        // logica para simular que la persona va a las actividades del parque
        switch (opcion) {
            case 0: // Nado con delfines
                actividadNadoDelfines();
                break;
            case 1: // Disfruta de Snorkel
                actividadSnorkel();
                break;
            case 2: // Restaurante
                actividadRestaurant(visitante);
                break;
            case 3: // Mundo de Aventuras
                actividadMundoAventura();
                break;
            case 4: // Faro/Mirador con descenso en tobogán
                actividadFaroTobogan();
                break;
            case 5: // Carreras de Gomones
                Debuger.log(MSJ_PersonaActividades, Color.violeta() + Thread.currentThread().getName()
                        + " está en las actividades." + Color.reset());
                try {
                    Thread.sleep(10); // Simula el tiempo que tarda en disfrutar de las actividades
                } catch (InterruptedException e) {
                }
                break;
        }
    }

    public static boolean estaCerrado() {
        // return para indicar si el parque está cerrado o no
        return parqueCerrado;
    }

    public synchronized static void cerrarParque() {
        // logica para simular el cierre del parque
        parqueCerrado = true;
        System.out.println("El parque ha cerrado. Nos vemos mañana...");
    }

    public static void abrirParque() {
        // logica para simular la apertura del parque
        parqueCerrado = false;
        System.out.println("El parque ha abierto.");
    }

    // Actividades
    private static void actividadRestaurant(Persona visitante) {
        Random random = new Random();
        switch (random.nextInt(3)) { // El visitante elige en cuál restaurant entrar
            case 0:
                Debuger.log(MSJ_PersonaActividadesRestaurant, Color.violeta() + Thread.currentThread().getName()
                        + " ingresó a " + restaurant1.getName() + Color.reset());
                restaurant1.entrarRestaurant();
                consumirEnRestaurant(visitante);
                restaurant1.salirRestaurant();
                break;
            case 1:
                Debuger.log(MSJ_PersonaActividadesRestaurant, Color.violeta() + Thread.currentThread().getName()
                        + " ingresó a " + restaurant2.getName() + Color.reset());
                restaurant2.entrarRestaurant();
                consumirEnRestaurant(visitante);
                restaurant2.salirRestaurant();
                break;
            case 2:
                Debuger.log(MSJ_PersonaActividadesRestaurant, Color.violeta() + Thread.currentThread().getName()
                        + " ingresó a " + restaurant3.getName() + Color.reset());
                restaurant3.entrarRestaurant();
                consumirEnRestaurant(visitante);
                restaurant3.salirRestaurant();
                break;
        }
    }

    private static void consumirEnRestaurant(Persona visitante) {
        // simula la logica del consumo en el restaurant dependiendo de las comidas
        // disponibles que tenga el visitante
        Random random = new Random();

        if (visitante.getAlmuerzo() && visitante.getMerienda()) {// Si tiene ambas opciones elige qué hacer
            switch (random.nextInt(3)) { // Depende de la opcion, consume la merienda, el almuerzo o ambos
                case 0:
                    visitante.consumirAlmuerzo();
                    break;
                case 1:
                    visitante.consumirMerienda();
                    break;
                case 2:
                    visitante.consumirAlmuerzo();
                    visitante.consumirMerienda();
                    break;
            }
        } else if (visitante.getAlmuerzo()) { // Si solo tiene el almuerzo lo consume
            visitante.consumirAlmuerzo();
        } else { // Si solo tiene la merienda la consume
            visitante.consumirMerienda();
        }

    }

    private static void actividadSnorkel() {
        if (actSnorkel.pedirEquipo()) {
            actSnorkel.hacerSnorkel();
            actSnorkel.regresarEquipo();
        }
    }

    private static void actividadFaroTobogan() {
        actFaroTobogan.entrarEscalera();
        actFaroTobogan.realizarActividadFaroTobogan();
    }

    private static void actividadNadoDelfines() {
        // simula la logica de la actividad de nado con delfines
        actNadoDelfines.entrarActividad();
        if (!Parque.estaCerrado()) {// Si el parque esta cerrado no se entro a la actividad y por eso no se puede
                                    // salir
            actNadoDelfines.salirActividad();
        }
        Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                Thread.currentThread().getName()
                        + " salio de la actividad de nado con delfines");
    }

    private static void actividadMundoAventura() {
        // simula la logica de un mundo de aventuras con tres actividades seguidas
        if (!Parque.estaCerrado()) {// Si el parque esta cerrado no se entro a la actividad y por eso no se puede
                                    // hacer nada
            actMundoAventura.hacerCuerdas();
            actMundoAventura.hacerSaltos();
            actMundoAventura.hacerTirolesa();
        }

    }
}
