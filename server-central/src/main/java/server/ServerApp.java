// server-central/src/main/java/server/ServerApp.java

package server;

import com.zeroc.Ice.*;

public class ServerApp {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            // --- ESTE ES EL NUEVO BLOQUE DE CÓDIGO ---
            // Registramos un "Shutdown Hook". Este código se ejecutará de forma garantizada
            // cuando la aplicación se apague (por ejemplo, con Ctrl+C).
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nSeñal de apagado recibida. Generando reporte final de votos...");
                VoteManager.getInstance().generateResumeCSV();
                System.out.println("Reporte final 'resume.csv' generado. Adiós.");
                // También es buena práctica destruir el comunicador aquí.
                communicator.destroy();
            }));
            // --- FIN DEL NUEVO BLOQUE ---

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "VoteStationAdapter", "default -p 10000");

            adapter.add(new VoteStationImpl(), Util.stringToIdentity("VoteStation"));
            adapter.activate();

            VoteManager.getInstance();
            System.out.println("VoteManager inicializado y archivos CSV preparados.");

            System.out.println("Servidor VoteStation listo y escuchando en el puerto 10000...");
            System.out.println("(Presiona Ctrl+C para detener el servidor y generar el reporte final)");

            // El servidor se queda esperando aquí.
            communicator.waitForShutdown();

        } catch (java.lang.Exception e) {
            System.err.println("Ocurrió un error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}