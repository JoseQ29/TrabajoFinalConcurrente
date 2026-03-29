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

    private Semaphore escaleras; // El semaforo con la capacidad que tinene las escaleras
    private Semaphore[] tobogan = { new Semaphore(1), new Semaphore(1) }; // Son semaforos que indican cuando está
                                                                          // habilitado el tobogan
    private ReentrantLock lockAdmin; // El lock que usa el administrador para funcionar como monitor
    private Condition esperaTobogan; // Condicion que hace esperar al administrador a que se habilite alguno de los
                                     // toboganes
    private Condition esperaVisitantes; // El admin espera a que lleguen visitantes a la cola de espera de los toboganes
    private Queue<Condition> colaTobogan; // La cola de los visitantes que ya subieron las escaleras y esperan a tirarse
                                          // por los toboganes
    private Map<Condition, Integer> toboganATirarse;// Se usa un mapeo para poder asociar el turno del visitante con el
                                                    // tobogán al que se debe tirar.

    public ActFaroTobogan(int espacioEnLasEscaleras) {
        this.escaleras = new Semaphore(espacioEnLasEscaleras);
        this.colaTobogan = new LinkedList<>();
        this.toboganATirarse = new HashMap<>();
        this.lockAdmin = new ReentrantLock();
        this.esperaTobogan = lockAdmin.newCondition();
        this.esperaVisitantes = lockAdmin.newCondition();
    }

    public void entrarEscalera() { // El visitante intenta entrar a la escalera
        try {
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " intenta entrar a la escalera" + Color.reset());
                escaleras.acquire();
            }
        } catch (InterruptedException e) {
        }
    }

    public void realizarActividadFaroTobogan() {
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                    Color.violeta() + Thread.currentThread().getName() + " está subiendo la escalera"
                            + Color.reset());
            Thread.sleep(1000);
            int toboganDesignado = esperaEnLaCola();
            if (toboganDesignado != -1) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                        Color.violeta() + Thread.currentThread().getName()
                                + " se tira del tobogan " + (toboganDesignado + 1) + Color.reset());
                bajarTobogan(tobogan[toboganDesignado]);
            }
        } catch (InterruptedException e) {
        }
    }

    private void bajarTobogan(Semaphore toboganUsado) {
        lockAdmin.lock();
        try {
            Thread.sleep(1000);
            toboganUsado.release();
            escaleras.release();
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                    + " ya terminó su actividad en el tobogan" + Color.reset());
            esperaTobogan.signal();
            if (Parque.estaCerrado()) {
                esperaTobogan.signal();
            }
        } catch (InterruptedException e) {
        } finally {
            lockAdmin.unlock();
        }
    }

    private int esperaEnLaCola() {
        lockAdmin.lock();
        int toboganATirar = -1;
        try {
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " entra en la cola de espera para tirarse del tobogan" + Color.reset());
                Condition miTurno = lockAdmin.newCondition(); // El visitante espera con su turno en la cola
                colaTobogan.add(miTurno);
                esperaVisitantes.signal(); // Avisa al admin que está esperando en la cola
                miTurno.await(); // Espera a que le toque su turno para tirarse por el tobogan
                toboganATirar = toboganATirarse.remove(miTurno);
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " se va porque el parque está cerrado" + Color.reset());
                escaleras.release();
                System.out.println(colaTobogan.size());
                System.out.println(colaTobogan.isEmpty());
                System.out.println(tobogan[0].availablePermits() + " y " + tobogan[1].availablePermits());
                System.out.println(escaleras.availablePermits());
            }
        } catch (InterruptedException e) {
        } finally {
            lockAdmin.unlock();
        }
        return toboganATirar;
    }

    public void atenderVisitantes() {
        int toboganDisponible;
        lockAdmin.lock();
        try {
            while (colaTobogan.isEmpty()) { // Verifica que haya alguien esperando para tirarse en los toboganes
                System.out.println("espera visitante");
                esperaVisitantes.await();
            }
            while (tobogan[0].availablePermits() <= 0 && tobogan[1].availablePermits() <= 0) { // Verifica que
                                                                                               // haya
                                                                                               // algun tobogan
                                                                                               // disponible
                System.out.println("espera tobogan");
                esperaTobogan.await();
            }
            if (tobogan[0].availablePermits() > 0) {
                tobogan[0].acquire();
                toboganDisponible = 0;
            } else {
                tobogan[1].acquire();
                toboganDisponible = 1;
            }
            Condition siguiente = colaTobogan.poll();
            toboganATirarse.put(siguiente, toboganDisponible);
            siguiente.signal(); // Avisa al siguiente en la cola que ya hay un tobogan disponible
        } catch (InterruptedException e) {
        } finally {
            lockAdmin.unlock();
        }
    }

    public boolean estaVacio() {
        return colaTobogan.isEmpty();
    }
}
