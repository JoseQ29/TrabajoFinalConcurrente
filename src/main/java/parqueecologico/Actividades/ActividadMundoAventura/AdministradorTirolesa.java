package parqueecologico.Actividades.ActividadMundoAventura;

import parqueecologico.Herramientas.Debuger;
import parqueecologico.Parque;

public class AdministradorTirolesa implements Runnable {
    private String nombre;
    private ActMundoAventura mundoAventura;

    public AdministradorTirolesa(String nombre, ActMundoAventura mundoAventura) {
        this.nombre = nombre;
        this.mundoAventura = mundoAventura;
    }

    public void run() {
        while (!Parque.estaCerrado()) {
            mundoAventura.atenderTirolesa();
        }
        Debuger.log(Parque.MSJ_Salidas,nombre +
                ", El admin de la tirolesa se va a su casa");
    }
}
