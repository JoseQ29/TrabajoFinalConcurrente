/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package parqueecologico;

/**
 *
 * @author Razor-PC V.3
 */
public class Proyecto {

    public static void main(String[] args) {
        Colectivo colectivo = new Colectivo();//crear el colectivo(monitor)

        Thread conductorThread = new Thread(new Conductor(1, "Conductor 1 y 2", colectivo), "Conductor 1");
        conductorThread.start();//iniciar el hilo del conductor

        for(int i = 0; i < 250; i++){//inicializar las personas que van al parque  
            Thread personaThread = new Thread(new Persona(i,false, colectivo), "Persona " + i);
            personaThread.start();
        }

        
    }

}
