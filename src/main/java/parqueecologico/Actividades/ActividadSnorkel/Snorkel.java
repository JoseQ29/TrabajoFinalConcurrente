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

    public void pedirEquipo() {
        mutex.lock();
        try {
            if (!Parque.estaCerrado()) {
                visitantesEsperando++;
                while (adminsDisponibles <= 0) {
                    visitanteEsperaAdmin.await();
                }
                if (!Parque.estaCerrado()) {
                    adminsDisponibles--;
                    adminEsperaVisitantes.signal();
                    Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                            Color.violeta() + Thread.currentThread().getName() + " fué antendido y espera por su equipo"
                                    + Color.reset() + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                    + equipoDisponible);
                    visitanteEsperaEquipo.await();
                } else {
                    visitantesEsperando--;
                    System.out.println("visitantesEsperando/equipos: " + visitantesEsperando + "/"
                            + equipoDisponible);
                }
            }
        } catch (InterruptedException e) {
        } finally {
            mutex.unlock();
        }
    }

    public void regresarEquipo() {
        mutex.lock();
        try {
            System.out.println(equipoDisponible + "/" + equiposTotal);
            if (!Parque.estaCerrado() || equipoDisponible < equiposTotal) {
                equipoDisponible++;
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + Thread.currentThread().getName() + " devolvió el equipo que usó."
                                + Color.reset()
                                + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                + equipoDisponible);
                adminEsperaEquipo.signal();
            }
            if (Parque.estaCerrado() && equipoDisponible == equiposTotal) {
                adminEsperaVisitantes.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }

    public void atenderVisitante() {
        mutex.lock();
        try {
            while (visitantesEsperando <= 0) {
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
                equipoDisponible--;
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + Thread.currentThread().getName() + " encontró un equipo para un visitante"
                                + Color.reset() + "visitantesEsperando/equipos: " + visitantesEsperando + "/"
                                + equipoDisponible);
                visitanteEsperaEquipo.signal();
                adminsDisponibles++;
                visitanteEsperaAdmin.signal();
            } else {
                Debuger.log(Parque.MSJ_PersonaActividadesSnorkel,
                        Color.violeta() + "El parque ya cerró, vuelvan mañana");
                visitanteEsperaAdmin.signalAll(); // Libera a todos los visitantes que esperan por ser atendidos
                actividadFinalizada = true;
            }
        } catch (InterruptedException e) {
        } finally {
            mutex.unlock();
        }
    }

    public void hacerSnorkel() {
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
}
