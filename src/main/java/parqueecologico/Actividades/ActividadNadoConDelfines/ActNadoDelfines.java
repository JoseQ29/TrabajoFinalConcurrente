package parqueecologico.Actividades.ActividadNadoConDelfines;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class ActNadoDelfines {
    private int visitantesEnEspera; // cantidad de personas que ingresaron al lugar esperando iniciar la actividad
    private final int minimoParaComenzar; // cantidad minima de personas para iniciar la actividad
    private final int capacidadMaxima; // cantidad maxima de personas que pueden participar en la actividad
    private final ReentrantLock locks;
    private final Condition visitanteEsperaEntrar; // condicion para que los visitantes esperen a que la actividad este
    // disponible
    private final Condition visitanteEsperaAdmin; // condicion para que los visitantes esperen a que un admin los
                                                  // atienda
    private final Condition visitanteEsperaTerminar; // condicion para que los visitantes esperen a que la actividad
                                                     // termine
    private final Condition adminEsperaSalida; // condicion para que el admin espere a que los visitantes salgan antes de atender a los siguientes
    private boolean actividadEnCurso; // indica si la actividad esta en curso o no

    public ActNadoDelfines() {
        this.visitantesEnEspera = 0;
        this.minimoParaComenzar = 30;
        this.capacidadMaxima = 40;
        this.locks = new ReentrantLock();
        this.visitanteEsperaEntrar = locks.newCondition();
        this.visitanteEsperaAdmin = locks.newCondition();
        this.visitanteEsperaTerminar = locks.newCondition();
        this.adminEsperaSalida = locks.newCondition();
        this.actividadEnCurso = false;
    }

    public void entrarActividad() {
        locks.lock();
        try {
            while (!Parque.estaCerrado() && (actividadEnCurso || visitantesEnEspera >= capacidadMaxima)) {
                visitanteEsperaEntrar.await();
            }
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                        Thread.currentThread().getName()
                                + " ingresó al lugar del nado con delfines, esperando a que la actividad comience."
                                + Color.reset());
                visitantesEnEspera++;
                while(!actividadEnCurso){
                    visitanteEsperaAdmin.await();
                } 
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                        Color.rojo() + Thread.currentThread().getName()
                                + " no pudo ingresar al lugar del nado con delfines porque el parque está cerrado."
                                + Color.reset());
            }

        } catch (InterruptedException e) {
        } finally {
            locks.unlock();
        }
    }

    public void salirActividad() {
        locks.lock();
        try {
            while(!actividadEnCurso){
                visitanteEsperaTerminar.await();
            }
            visitantesEnEspera--;
            Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                    Thread.currentThread().getName() + " salió del lugar del nado con delfines." + Color.reset());
            if (visitantesEnEspera == 0) {
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                        Color.violeta() + "No hay más visitantes en el nado con delfines, esperando a que ingresen más personas para comenzar la actividad." + Color.reset());
                actividadEnCurso = false;
                visitanteEsperaEntrar.signalAll();// si ya no hay mas personas en el lugar de la actividad, se notifica
                                                  // a los que esperan afuera para entrar
                adminEsperaSalida.signalAll(); // se notifica al admin para que pueda atender a los siguientes visitantes
            }
        } catch (Exception e) {
        } finally {
            locks.unlock();
        }
    }

    public void atenderVisitante() {
        locks.lock();
        try {
            if (visitantesEnEspera >= minimoParaComenzar) {
                actividadEnCurso = true;
                visitanteEsperaAdmin.signalAll(); // Si hay suficientes personas para comenzar la actividad, se notifica
                                                  // a todos los visitantes que esperan para que ingresen a la actividad
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                        Color.violeta() + "Comenzando actividad de nado con delfines con " + visitantesEnEspera
                                + " visitantes." + Color.reset());
                Thread.sleep(700);
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines,
                        Color.violeta() + "La actividad de nado con delfines ha terminado." + Color.reset());
                visitanteEsperaTerminar.signalAll(); // Se notifica a los visitantes que la actividad ha terminado para
                                                     // que puedan salir
                adminEsperaSalida.await();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesNadoDelfines, Color.violeta()
                        + "Como no hay suficientes personas, se suspende este horario." + Color.reset());
            }
        } catch (InterruptedException e) {
        } finally {
            locks.unlock();
        }
    }

    public void terminarActividad() {
        locks.lock();
        try {
            visitanteEsperaAdmin.signalAll();// avisa a los visitantes que esperan para entrar que el
                                                // parque esta cerrado y no se va a realizar la actividad
        } finally {
            locks.unlock();
        }
    }
}