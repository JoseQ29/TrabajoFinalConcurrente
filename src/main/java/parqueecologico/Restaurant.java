package parqueecologico;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Restaurant {
    private int capacidadMaxima;                            // Variable que contiene la capacidad maxima del restaurant
    private int cantPersonasComiendo;                       // Contiene la cantidad de personas actuales dentro del restaurant
    private String nombre;                                  // Nombre del restauran
    private ReentrantLock lock;                             // El lock que vamos a usar para bloquear a los visitantes
    private Queue<Condition> colaEspera;                    // La cola de condiciones que vamos a usar para poder tener un orden de llegada en el restaurant

    public Restaurant(int capMax, String nomb){
        this.cantPersonasComiendo = 0;
        this.capacidadMaxima = capMax;
        this.nombre = nomb;
        this.lock = new ReentrantLock();
        this.colaEspera = new LinkedList<>(); 
    }

    public void entrarRestaurant(Persona visitante){
        lock.lock();
        Random queCome = new Random();
        try {
            if (cantPersonasComiendo == capacidadMaxima){   // Si el restaurant esta lleno, se bloquea el visitante
                Condition miTurno = lock.newCondition();    // Crea la condición para bloquear el visitante
                colaEspera.add(miTurno);                    // Se coloca la condición en la que se bloque el visitante en la cola
                miTurno.await();
            }
            cantPersonasComiendo++;
            if(queCome.nextBoolean()){                      // Si es true come su merienda, sino come su almuerzo
                visitante.consumirMerienda();
            }else{
                visitante.consumirAlmuerzo();
            }
        } 
        catch (InterruptedException e) {}
        finally {
            lock.unlock();
        }
    }

    public void salirRestaurant(){
        lock.lock();
        try {
            cantPersonasComiendo--;
            if (!colaEspera.isEmpty()) {
                Condition siguiente = colaEspera.poll();    // Obtengo el primero de la fila de espera en siguiente
                siguiente.signal();                         // Despierto al siguiente en la fila para que entre al restaurant
            }
        }
        finally{
            lock.unlock();
        }
    }
}
