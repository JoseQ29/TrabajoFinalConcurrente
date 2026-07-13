package parqueecologico.Actividades.ActividadFaroTobogan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class ActFaroTobogan {

    private Semaphore escaleras; // Capacidad de las escaleras
    private Semaphore[] tobogan = { new Semaphore(1), new Semaphore(1) }; // Disponibilidad de cada tobogán
    private ReentrantLock lockAdmin; // Lock compartido para el monitor del administrador y de los visitantes
    private Condition esperaTobogan; // El admin espera a que se libere un tobogán
    private Condition esperaVisitantes; // El admin espera a que lleguen visitantes a la cola
    private Queue<Condition> colaTobogan; // Visitantes que subieron la escalera y aguardan tobogán
    private Map<Condition, Integer> toboganATirarse; // Asocia el turno del visitante con el tobogán asignado

    public ActFaroTobogan(int espacioEnLasEscaleras) {
        this.escaleras = new Semaphore(espacioEnLasEscaleras);
        this.colaTobogan = new LinkedList<>();
        this.toboganATirarse = new HashMap<>();
        this.lockAdmin = new ReentrantLock();
        this.esperaTobogan = lockAdmin.newCondition();
        this.esperaVisitantes = lockAdmin.newCondition();
    }

    //Simula la acción del visitante en subir la escalera, la cual tiene una capacidad limitada.
    public void entrarEscalera() {
        try {
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " intenta entrar a la escalera" + Color.reset());
                escaleras.acquire(); //Intenta entrar a la escalera, si no hay espacio disponible, se bloquea hasta que haya espacio.
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void realizarActividadFaroTobogan() {
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                    Color.violeta() + Thread.currentThread().getName() + " está subiendo la escalera" + Color.reset());
            Thread.sleep(200); //El visitante sube la escalera.
            int toboganDesignado = esperaEnLaCola(); //El visitante espera a que el admin le asigne un tobogán.
            if (toboganDesignado != -1) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                        Color.violeta() + Thread.currentThread().getName()
                                + " se tira del tobogan " + (toboganDesignado + 1) + Color.reset());
                bajarTobogan(tobogan[toboganDesignado]);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void bajarTobogan(Semaphore toboganUsado) {
        lockAdmin.lock();
        try {
            Thread.sleep(100);
            toboganUsado.release();
            escaleras.release();
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                    + " ya terminó su actividad en el tobogan" + Color.reset());
            // Siempre avisa al admin que hay un tobogán libre
            esperaTobogan.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockAdmin.unlock();
        }
    }
    //El visitante entra en la cola de espera para tirarse de los toboganes, esperando a que el admin le asigne un tobogán.
    //Devuelve -1 si ocurrió un problema (parque cerrado antes de asignarle un tobogán), o el índice del tobogán asignado (0 o 1) si todo salió bien.
    private int esperaEnLaCola() {
        lockAdmin.lock();
        int toboganATirar = -1;
        try {
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " entra en la cola de espera para tirarse del tobogan" + Color.reset());
                Condition miTurno = lockAdmin.newCondition();
                colaTobogan.add(miTurno);
                esperaVisitantes.signal(); // Avisa al admin que hay alguien esperando
                miTurno.await();           // Espera su turno

                // Al despertar: si el parque cerró, toboganATirarse no tendrá el tobogan por el cual se tiene que tirar.
                Integer asignado = toboganATirarse.remove(miTurno);
                if (asignado != null) { // Si el tobogan asignado es null, es porque el administrador no pudo asignarle un tobogán antes de que cerrara el parque
                    toboganATirar = asignado;
                } else {
                    // Cerró antes de que lo asignaran: debe liberar la escalera
                    Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta()
                            + Thread.currentThread().getName()
                            + " despertó por cierre del parque, sale de la cola" + Color.reset());
                    escaleras.release();
                }

            } else {
                // Ya estaba cerrado al intentar entrar a la cola
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " se va porque el parque está cerrado" + Color.reset());
                escaleras.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockAdmin.unlock();
        }
        return toboganATirar;
    }

    //Es la función que el administrador de los toboganes llama para atender a los visitantes que están esperando en la cola.
    public void atenderVisitantes() {
        int toboganDisponible;
        lockAdmin.lock();
        try {
            // Espera visitantes o sale si el parque cerró (señalado por notificarCierre)
            while (colaTobogan.isEmpty() && !Parque.estaCerrado()) {
                esperaVisitantes.await();
            }
            if (colaTobogan.isEmpty()) {
                // Parque cerrado y sin nadie esperando: nada que hacer
                return;
            }

            // Espera algun tobogán libre o sale si el parque cerró (señalado por notificarCierre)
            while (tobogan[0].availablePermits() <= 0 && tobogan[1].availablePermits() <= 0
                    && !Parque.estaCerrado()) {
                esperaTobogan.await();
            }

            if (Parque.estaCerrado() && tobogan[0].availablePermits() <= 0 && tobogan[1].availablePermits() <= 0) {
                // No hay tobogán disponible y el parque cerró:
                // Entonces libera a todos los visitantes esperando por el tobogan
                while (!colaTobogan.isEmpty()) {
                    Condition siguiente = colaTobogan.poll();
                    siguiente.signal(); // El visitante se despierta sin tener asignado un tobogan (la forma para definir que el parque cerró) y se va.
                }
                return;
            }

            // Hay tobogán disponible y se asigna al siguiente en la cola
            if (tobogan[0].availablePermits() > 0) {
                tobogan[0].acquire();
                toboganDisponible = 0;
            } else {
                tobogan[1].acquire();
                toboganDisponible = 1;
            }
            Condition siguiente = colaTobogan.poll(); //Saca al siguiente visitante (el turno correspondiente) de la cola para asignarle un tobogán
            toboganATirarse.put(siguiente, toboganDisponible); //Asocia el turno del visitante con el tobogán asignado
            siguiente.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockAdmin.unlock();
        }
    }

    public void notificarCierre() {
        lockAdmin.lock();
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                    Color.amarillo() + "FaroTobogan: notificando cierre. Visitantes en cola: "
                            + colaTobogan.size() + Color.reset());

            // Se despierta al admin si está bloqueado en alguna de sus esperas
            esperaVisitantes.signalAll();
            esperaTobogan.signalAll();

            // Despertar a cada visitante que está bloqueado en miTurno.await()
            for (Condition turno : colaTobogan) {
                turno.signal();
            }
            // No se vacía el colaTobogan para que lo haga el AdministradorTobogan, y así decida cuándo terminar.
            colaTobogan.clear();
        } finally {
            lockAdmin.unlock();
        }
    }

    public boolean estaVacio() {
        lockAdmin.lock();
        try {
            return colaTobogan.isEmpty();
        } finally {
            lockAdmin.unlock();
        }
    }
}