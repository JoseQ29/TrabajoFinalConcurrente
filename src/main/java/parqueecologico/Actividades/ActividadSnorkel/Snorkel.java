package parqueecologico.Actividades.ActividadSnorkel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class Snorkel {
    private int equipoDisponible;
    private int equiposTotal;
    private int visitantesEsperando;
    private int adminsDisponibles;
    public boolean actividadFinalizada;

    private ReentrantLock mutex;
    private Condition adminEsperaVisitantes;
    private Condition adminEsperaEquipo;
    private Condition visitanteEsperaAdmin;
    private Condition visitanteEsperaEquipo;

    public Snorkel(int cantEquipos) {
        this.equipoDisponible = cantEquipos;
        this.equiposTotal = cantEquipos;
        this.visitantesEsperando = 0;
        this.adminsDisponibles = 2;
        this.actividadFinalizada = false;
        this.mutex = new ReentrantLock();
        this.adminEsperaVisitantes = mutex.newCondition();
        this.adminEsperaEquipo = mutex.newCondition();
        this.visitanteEsperaAdmin = mutex.newCondition();
        this.visitanteEsperaEquipo = mutex.newCondition();
    }

    public boolean pedirEquipo() {
        //Los visitantes esperan a una admin para ser atendidos y luego esperan a que haya 
        //un equipo disponible 
        mutex.lock();
        boolean tieneEquipo = false;
        try {
            if (!Parque.estaCerrado()) {
                visitantesEsperando++;
                while (adminsDisponibles <= 0) {
                    visitanteEsperaAdmin.await();//espera a que un admin lo atienda
                }
                if (!Parque.estaCerrado()) {
                    adminsDisponibles--;
                    adminEsperaVisitantes.signal();
                    Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                            Color.violeta() + Thread.currentThread().getName() + " fué antendido y espera por su equipo"
                                    + Color.reset() + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                    + equipoDisponible);
                    visitanteEsperaEquipo.await();//espera por un equipo disponible
                    tieneEquipo = true;
                } else {
                    visitantesEsperando--;      //Saca de la cola a los visitantes que esperaban por la actividad.
                }
            }
        } catch (InterruptedException e) {
        } finally {
            mutex.unlock();
        }
        return tieneEquipo;
    }

    public void regresarEquipo() {
        //Los visitantes devuelven el equipo que usaron en la actividad
        mutex.lock();
        try {
            //System.out.println(equipoDisponible + "/" + equiposTotal);
            //linea para debug 

            if (equipoDisponible < equiposTotal) {
                equipoDisponible++;
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + Thread.currentThread().getName() + " devolvió el equipo que usó."
                                + Color.reset()
                                + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                + equipoDisponible);
                adminEsperaEquipo.signal();
            }
            if (Parque.estaCerrado() && equipoDisponible == equiposTotal) {
                adminEsperaEquipo.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    public void atenderVisitante() {
        //Acciones que realizan los administradores de la actividad para atender a los visitantes
        mutex.lock();
        try {
            while (!Parque.estaCerrado() && visitantesEsperando <= 0) {
                adminEsperaVisitantes.await();
            }
            if (!Parque.estaCerrado()) {
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + Thread.currentThread().getName() + " atendió a un visitante " + Color.reset()
                                + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                + equipoDisponible);
                visitantesEsperando--;
                while (equipoDisponible <= 0) {
                    adminEsperaEquipo.await();
                }
                if (!Parque.estaCerrado()) {
                    equipoDisponible--;
                    Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                            Color.violeta() + Thread.currentThread().getName() + " encontró un equipo para un visitante"
                                    + Color.reset() + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                    + equipoDisponible);
                    visitanteEsperaEquipo.signal();
                    adminsDisponibles++;
                    visitanteEsperaAdmin.signal();
                } else {
                    adminsDisponibles++;
                }
            } else {
                visitanteEsperaAdmin.signalAll(); // Libera a todos los visitantes que esperan por ser atendidos
                visitanteEsperaEquipo.signalAll(); //LIbera a todos los visitantes que esperan por un equipo
                while (equipoDisponible != equiposTotal) {
                    adminEsperaEquipo.await(); // No cierra la actividad hasta que todas las personas devuelvan sus equipos
                }
                while(visitantesEsperando != 0) {
                    visitanteEsperaEquipo.signalAll(); // Libera a los visitantes que quedaron esperando su equipo
                }
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + "El parque ya cerró, vuelvan mañana");
                actividadFinalizada = true;
            }
        } catch (InterruptedException e) {
        } finally {
            mutex.unlock();
        }
    }

    public void hacerSnorkel() {
        //simula la actividad de snorkel con 1000ms
        if (!Parque.estaCerrado()) {
            Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                    Color.violeta() + Thread.currentThread().getName() + " está haciendo snorkel " + Color.reset()
                            + "visitantesEsperando/equipos: " + visitantesEsperando + "/" + equipoDisponible);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void notificarCierre() {
        //Usado por hora parque para anunciar el cierre del parque
        mutex.lock();
        try {
            adminEsperaVisitantes.signalAll();
        } finally {
            mutex.unlock();
        }

    }
}