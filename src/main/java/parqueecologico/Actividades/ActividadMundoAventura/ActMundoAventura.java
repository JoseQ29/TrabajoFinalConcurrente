package parqueecologico.Actividades.ActividadMundoAventura;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class ActMundoAventura {
    // cuerdas
    private final ReentrantLock lockCuerdas;
    private final Queue<Condition> colaEsperaCuerdas;
    private boolean cuerdasOcupadas;
    // saltos
    private final ReentrantLock lockSaltos;
    private final Queue<Condition> colaEsperaSaltos;
    private int cantPersonasPorSaltar;
    private final int cantidadADespertar = 2; // Cantidad de personas a despertar cuando se libera el salto
    // tirolesa
    private final ReentrantLock lockTirolesa;
    private final Queue<Condition> colaEsperaTirolesaEste;
    private final Queue<Condition> colaEsperaTirolesaOeste;
    private boolean viajeEnCurso; // la tirolesa se esta usando o no
    private int personasTirolesa; // Cantidad de personas que van a usar la tirolesa
    private final int capacidadTirolesa; // Capacidad máxima de la tirolesa
    private final Condition notificarAdminLlegada; // Condición para notificar al admin que llego alguien a la espera
    private final Condition notificarAdminSalida; // Condicion para notificar al admin que la tirolesa se vacio

    public ActMundoAventura() {
        this.lockCuerdas = new ReentrantLock();
        this.lockSaltos = new ReentrantLock();
        this.lockTirolesa = new ReentrantLock();
        this.colaEsperaCuerdas = new LinkedList<>();
        this.colaEsperaSaltos = new LinkedList<>();
        this.colaEsperaTirolesaEste = new LinkedList<>();
        this.colaEsperaTirolesaOeste = new LinkedList<>();
        this.cuerdasOcupadas = false;
        this.cantPersonasPorSaltar = 0;
        this.viajeEnCurso = false;
        this.personasTirolesa = 0;
        this.capacidadTirolesa = 2;
        this.notificarAdminLlegada = lockTirolesa.newCondition();
        this.notificarAdminSalida = lockTirolesa.newCondition();
    }

    public void hacerCuerdas() {
        lockCuerdas.lock();
        try {
            entrarCuerdas();
            if (!Parque.estaCerrado()) {
                Thread.sleep(333);
                salirCuerdas();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraCuerdas,
                        Thread.currentThread().getName() + " quiere entrar a cuerdas pero el parque esta cerrado.");
            }
        } catch (InterruptedException e) {
        } finally {
            lockCuerdas.unlock();
        }
    }

    private void entrarCuerdas() {
        try {
            if (cuerdasOcupadas) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraCuerdas,
                        Thread.currentThread().getName() + " quiere entrar a cuerdas pero esta ocupado, se bloquea");
                Condition miTurno = lockCuerdas.newCondition(); // crea el turno para el visitante
                colaEsperaCuerdas.add(miTurno); // lo agrega a la cola de espera
                miTurno.await();
            }
            if (!Parque.estaCerrado()) {
                cuerdasOcupadas = true;
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraCuerdas,
                        Thread.currentThread().getName() + " entró a cuerdas");
            }
        } catch (InterruptedException e) {
        }
    }

    private void salirCuerdas() {
        cuerdasOcupadas = false;
        Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraCuerdas,
                Thread.currentThread().getName() + " salió de cuerdas");
        if (!Parque.estaCerrado()) {
            if (!colaEsperaCuerdas.isEmpty()) {
                Condition siguiente = colaEsperaCuerdas.poll(); // obtiene el siguiente de la fila de espera
                siguiente.signal(); // lo despierta para que entre a cuerdas
            }
        } else {
            while (!colaEsperaCuerdas.isEmpty()) {
                Condition siguiente = colaEsperaCuerdas.poll(); // obtiene el siguiente de la fila de espera
                siguiente.signal(); // avisa a todos que el parque cerró
            }
        }
    }

    public void hacerSaltos() {
        try {
            entrarSaltos();
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraSaltos,
                        Thread.currentThread().getName() + " está saltando");
                Thread.sleep(333);
                salirSaltos();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraSaltos,
                        Thread.currentThread().getName() + " queria saltar pero el parque esta cerrado");
            }
        } catch (InterruptedException e) {
        }
    }

    private void entrarSaltos() {
        lockSaltos.lock();
        try {
            if (cantPersonasPorSaltar >= cantidadADespertar) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraSaltos,
                        Thread.currentThread().getName()
                                + " espera para entrar a saltos, ya hay 2 personas por saltar");
                Condition miTurno = lockSaltos.newCondition(); // crea el turno para el visitante
                colaEsperaSaltos.add(miTurno); // lo agrega a la cola de espera
                miTurno.await();
            }
            if (!Parque.estaCerrado()) {
                cantPersonasPorSaltar++;
            }
        } catch (InterruptedException e) {
        } finally {
            lockSaltos.unlock();
        }
    }

    private void salirSaltos() {
        int i = 0; // contador para controlar cuantas personas se despiertan de la cola de espera
        cantPersonasPorSaltar--;
        Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraSaltos,
                Thread.currentThread().getName() + " salió de saltos");
        Condition siguiente;
        if (!Parque.estaCerrado()) {
            while (!colaEsperaSaltos.isEmpty() && i < cantidadADespertar) {
                siguiente = colaEsperaSaltos.poll(); // obtiene el siguiente de la fila de espera
                siguiente.signal(); // lo despierta para que entre a saltos
                i++;
            }
        } else {
            while (!colaEsperaSaltos.isEmpty()) {
                siguiente = colaEsperaSaltos.poll(); // obtiene el siguiente de la fila de espera
                siguiente.signal(); // Lo despierta para indicarle que el parque cerro
            }
        }
    }

    public void hacerTirolesa() {
        try {
            entrarTirolesa();
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        Thread.currentThread().getName() + " esta usando la tirolesa");
                Thread.sleep(100);
                salirTirolesa();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        Thread.currentThread().getName() + " quiere usar la tirolesa pero el parque esta cerrado.");
            }
        } catch (InterruptedException e) {
        }
    }

    private void entrarTirolesa() {
        lockTirolesa.lock();
        try {
            if (!Parque.estaCerrado()) {
                Random random = new Random();
                boolean quiereIrEsteOeste = random.nextBoolean(); // Simula la decisión del visitante
                hacerColaTirolesa(quiereIrEsteOeste);
            } 
        } finally {
            lockTirolesa.unlock();
        }
    }

    private void hacerColaTirolesa(Boolean EsteOeste) {
        try {
            if (EsteOeste) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        Thread.currentThread().getName()
                                + " espera a ser llamado para realizar el viaje en la cola este");
                Condition miTurno = lockTirolesa.newCondition();
                colaEsperaTirolesaEste.add(miTurno);
                notificarAdminLlegada.signalAll();
                miTurno.await();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        Thread.currentThread().getName()
                                + " espera a ser llamado para el viaje en la cola oeste");
                Condition miTurno = lockTirolesa.newCondition();
                colaEsperaTirolesaOeste.add(miTurno);
                notificarAdminLlegada.signalAll();
                miTurno.await();
            }
            if (!Parque.estaCerrado())
                personasTirolesa++;
        } catch (InterruptedException e) {
        }
    }

    private void salirTirolesa() {
        lockTirolesa.lock();
        try {
            personasTirolesa--;
            Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                    Thread.currentThread().getName() + " salió de la tirolesa");
            if (personasTirolesa == 0) {
                viajeEnCurso = false;
                notificarAdminSalida.signalAll();
            }
        } finally {
            lockTirolesa.unlock();
        }
    }

    public void atenderTirolesa() {
        lockTirolesa.lock();
        try {
            while (!Parque.estaCerrado() && colaEsperaTirolesaEste.isEmpty() && colaEsperaTirolesaOeste.isEmpty()) {
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        "El admin de la tirolesa espera a que lleguen visitantes.");
                notificarAdminLlegada.await();
            }
            if (!Parque.estaCerrado()) {
                boolean viajeEsteOeste = true;
                if (!colaEsperaTirolesaEste.isEmpty()) {// si la tirolesa este no esta vacia, despierta a los siguientes
                                                        // en la cola
                    administrarViajeTirolesa(viajeEsteOeste);
                } else {// si esta vacía significa que la cola con personas era la oeste, entoces hace
                        // el viaje sola
                    Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                            "La tirolesa va sola de este a oeste");
                    Thread.sleep(400);
                }
                if (!colaEsperaTirolesaOeste.isEmpty()) {
                    viajeEsteOeste = false;
                    administrarViajeTirolesa(viajeEsteOeste);
                } else {// si la cola de espera oeste a este esta vacia el control vuelve solo al otro
                        // lado
                    Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                            "La tirolesa vuelve sola del lado oeste a este");
                    Thread.sleep(400);
                }
            }
        } catch (InterruptedException e) {
        } finally {
            lockTirolesa.unlock();
        }
    }

    private void administrarViajeTirolesa(Boolean esteOeste) throws InterruptedException {
        viajeEnCurso = true;
        int i = 0;
        if (esteOeste) {
            Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                    "El admin de la tirolesa habilita el viaje este a oeste.");
            do {
                Condition siguienteEste = colaEsperaTirolesaEste.poll();
                siguienteEste.signal();
                i++;
            } while (!colaEsperaTirolesaEste.isEmpty() && i < capacidadTirolesa);

            while (!Parque.estaCerrado() && viajeEnCurso) {// espera a que termine el viaje de este a oeste antes de
                                                           // habilitar el de oeste a este
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        "El admin de la tirolesa espera a que termine el viaje de este a oeste.");
                notificarAdminSalida.await();
            }
        } else {
            Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                    "El admin de la tirolesa habilita el viaje oeste a este.");
            do {
                Condition siguienteOeste = colaEsperaTirolesaOeste.poll();
                siguienteOeste.signal();
                i++;
            } while (!colaEsperaTirolesaOeste.isEmpty() && i < capacidadTirolesa);

            while (!Parque.estaCerrado() && viajeEnCurso) {// espera a que termine el viaje de oeste a este antes de
                                                           // habilitar el de este a oeste
                Debuger.log(Parque.MSJ_PersonaActividadesMundoAventuraTirolesa,
                        "El admin de la tirolesa espera a que termine el viaje de oeste a este.");
                notificarAdminSalida.await();
            }
        }
    }

    public void notificarCierreTirolesa() {
        lockTirolesa.lock();
        try {
            notificarAdminLlegada.signalAll(); // notifica al administrador que el parque cerro y debe terminar la
                                               // actividad
            if (!colaEsperaTirolesaEste.isEmpty()) {
                do {
                    Condition siguienteEste = colaEsperaTirolesaEste.poll();
                    siguienteEste.signal();
                } while (!colaEsperaTirolesaEste.isEmpty());// avisa a todos que cerro el parque en la cola este
            } else if (!colaEsperaTirolesaOeste.isEmpty()) {
                do {
                    Condition siguienteOeste = colaEsperaTirolesaOeste.poll();
                    siguienteOeste.signal();
                } while (!colaEsperaTirolesaOeste.isEmpty());// avisa a todos que cerro el parque en la cola oeste
            }
        } finally {
            lockTirolesa.unlock();
        }
    }

}
