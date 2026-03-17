package parqueecologico.Actividades.ActividadSnorkel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import parqueecologico.Parque;
import parqueecologico.Herramientas.Color;
import parqueecologico.Herramientas.Debuger;

public class Snorkel {
    private int equipoDisponible;                               // Son la cantidad de equipos que se pueden entregar a los visitantes en el momento
    private int visitantesEnEspera;
    private int adminsDisponibles;
    private ReentrantLock locks;
    private Condition adminEsperaVisitantes;
    private Condition adminEsperaEquipo;
    private Condition visitanteEsperaAdmin;
    private Condition visitanteEsperaEquipo;

    public Snorkel(int cantEquipos){
        this.equipoDisponible = cantEquipos;
        this.visitantesEnEspera = 0;
        this.adminsDisponibles = 2;
        this.locks = new ReentrantLock();
        this.adminEsperaVisitantes = locks.newCondition();
        this.adminEsperaEquipo = locks.newCondition();
        this.visitanteEsperaAdmin = locks.newCondition();
        this.visitanteEsperaEquipo = locks.newCondition();
    }

    public void pedirEquipo(){
        locks.lock();
        try {
            visitantesEnEspera++;
            adminEsperaVisitantes.signal();
            while(adminsDisponibles <= 0) { 
                visitanteEsperaAdmin.await();
            }
            adminsDisponibles--;
            Debuger.log(Parque.MSJ_PersonaActividadesSnorkel, Color.violeta() + Thread.currentThread().getName() + " espera por su equipo " + Color.reset());
            visitanteEsperaEquipo.await();
        } catch (InterruptedException e) {} 
        finally {
            locks.unlock();
        }
    }

    public void regresarEquipo(){
        locks.lock();
        try {
            Debuger.log(Parque.MSJ_PersonaActividadesSnorkel, Color.violeta() + Thread.currentThread().getName() + " devolvió el equipo de snorkel" + Color.reset());
            equipoDisponible++;
            adminEsperaEquipo.signal();
        } catch (Exception e) {}
        finally{
            locks.unlock();
        }
    }

    public void atenderVisitante(){
        locks.lock();
        try {
            while(visitantesEnEspera<=0){
                adminEsperaVisitantes.await();
            }
            //visitantesEnEspera--;
            buscarEquipo();
            Debuger.log(Parque.MSJ_PersonaActividadesSnorkel, Color.violeta() + Thread.currentThread().getName() + " encontró un equipo para el visitante" + Color.reset());
            visitanteEsperaEquipo.signal();
            visitantesEnEspera--;
            adminsDisponibles++;
        } catch (InterruptedException e) {
        } finally {
            locks.unlock();
        }
    }

    public void buscarEquipo(){     
        locks.lock();                           // Los administradores tratan de conseguir un equipo para el visitante
        Debuger.log(Parque.MSJ_PersonaActividadesSnorkel, Color.violeta() + Thread.currentThread().getName() + " está buscando un equipo para un visitante" + Color.reset());
        try {
            while (equipoDisponible <= 0) { 
                adminEsperaEquipo.await();
            }    
            equipoDisponible--;
        } catch (InterruptedException e) {}
        finally{
            locks.unlock();
        }
    }
}
