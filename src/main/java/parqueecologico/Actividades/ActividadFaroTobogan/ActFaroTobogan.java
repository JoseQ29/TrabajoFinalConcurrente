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

    // ─── Flujo del visitante ─────────────────────────────────────────────────

    public void entrarEscalera() {
        try {
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan, Color.violeta() + Thread.currentThread().getName()
                        + " intenta entrar a la escalera" + Color.reset());
                escaleras.acquire();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void realizarActividadFaroTobogan() {
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                    Color.violeta() + Thread.currentThread().getName() + " está subiendo la escalera" + Color.reset());
            Thread.sleep(200);
            int toboganDesignado = esperaEnLaCola();
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

    /**
     * El visitante se encola y espera a que el admin le asigne un tobogán.
     * Si el parque cierra mientras espera (su Condition es señalada por
     * notificarCierre), sale limpiamente devolviendo -1 y liberando la escalera.
     */
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

                // Al despertar: si el parque cerró, toboganATirarse no tendrá su entrada
                Integer asignado = toboganATirarse.remove(miTurno);
                if (asignado != null) {
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

    // ─── Flujo del administrador ─────────────────────────────────────────────

    /**
     * Atiende de a un visitante por llamada.
     * Sale inmediatamente si el parque ya cerró y la cola está vacía.
     * Las esperas internas también se desbloquean por notificarCierre().
     */
    public void atenderVisitantes() {
        int toboganDisponible;
        lockAdmin.lock();
        try {
            // Espera visitantes; sale si el parque cerró (señalado por notificarCierre)
            while (colaTobogan.isEmpty() && !Parque.estaCerrado()) {
                esperaVisitantes.await();
            }
            if (colaTobogan.isEmpty()) {
                // Parque cerrado y sin nadie esperando: nada que hacer
                return;
            }

            // Espera tobogán libre; sale si el parque cerró (señalado por notificarCierre)
            while (tobogan[0].availablePermits() <= 0 && tobogan[1].availablePermits() <= 0
                    && !Parque.estaCerrado()) {
                esperaTobogan.await();
            }

            if (Parque.estaCerrado() && tobogan[0].availablePermits() <= 0 && tobogan[1].availablePermits() <= 0) {
                // No hay tobogán disponible y el parque cerró:
                // descarta a todos los visitantes en cola señalándolos sin asignarles tobogán
                while (!colaTobogan.isEmpty()) {
                    Condition siguiente = colaTobogan.poll();
                    siguiente.signal(); // El visitante despertará, no encontrará su entrada en toboganATirarse y saldrá
                }
                return;
            }

            // Hay tobogán disponible: asignarlo al siguiente en la cola
            if (tobogan[0].availablePermits() > 0) {
                tobogan[0].acquire();
                toboganDisponible = 0;
            } else {
                tobogan[1].acquire();
                toboganDisponible = 1;
            }
            Condition siguiente = colaTobogan.poll();
            toboganATirarse.put(siguiente, toboganDisponible);
            siguiente.signal();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockAdmin.unlock();
        }
    }

    // ─── Cierre del parque ───────────────────────────────────────────────────

    /**
     * Llamado por HoraParque al cierre del parque.
     *
     * Desbloquea todos los puntos de espera dentro del lock compartido:
     *  - esperaVisitantes: saca al admin del await si no hay visitantes.
     *  - esperaTobogan:    saca al admin del await si no hay tobogán libre.
     *  - colaTobogan:      señala a cada visitante que aún espera en la cola;
     *                      como no se agrega entrada en toboganATirarse, cada uno
     *                      detectará el cierre y liberará la escalera por su cuenta.
     *
     * No es necesario llamar a escaleras.release() aquí: cada visitante lo hace
     * desde esperaEnLaCola() al detectar que su entrada no está en toboganATirarse.
     */
    public void notificarCierre() {
        lockAdmin.lock();
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesFaroTobogan,
                    Color.amarillo() + "FaroTobogan: notificando cierre. Visitantes en cola: "
                            + colaTobogan.size() + Color.reset());

            // Despertar al admin si está bloqueado en alguna de sus esperas
            esperaVisitantes.signalAll();
            esperaTobogan.signalAll();

            // Despertar a cada visitante que está bloqueado en miTurno.await()
            // SIN agregar entrada en toboganATirarse → el visitante saldrá limpiamente
            for (Condition turno : colaTobogan) {
                turno.signal();
            }
            // Nota: no vaciamos colaTobogan aquí; estaVacio() lo usará AdministradorTobogan
            // para decidir cuándo terminar, y cada visitante se elimina de la cola implícitamente
            // cuando el admin hace colaTobogan.poll() o queda vacía tras los signals.
            // Para mayor seguridad, la vaciamos nosotros también:
            colaTobogan.clear();
        } finally {
            lockAdmin.unlock();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    public boolean estaVacio() {
        lockAdmin.lock();
        try {
            return colaTobogan.isEmpty();
        } finally {
            lockAdmin.unlock();
        }
    }
}