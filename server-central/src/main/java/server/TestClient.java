// server-central/src/main/java/server/TestClient.java

package server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

// Importamos la interfaz del Proxy generada por ICE
import common.VoteStationPrx;

public class TestClient {
    public static void main(String[] args) {
        // Usamos un bloque try-with-resources para asegurar que el comunicador se cierre.
        try (Communicator communicator = Util.initialize(args)) {
            // Creamos un proxy para comunicarnos con el servidor.
            // La cadena "VoteStation:default -p 10000" contiene:
            // - "VoteStation": la identidad del objeto en el servidor.
            // - "default -p 10000": la dirección y puerto del servidor.
            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");

            // Hacemos un "cast" del proxy base al tipo específico que necesitamos (VoteStationPrx).
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Invalid proxy");
            }

            System.out.println("--- Cliente de Prueba Iniciado ---");

            // --- Escenario de Prueba ---
            // 1. Primer voto: Debería ser exitoso.
            System.out.println("\nEnviando primer voto para el documento '112233'...");
            boolean result1 = voteStation.vote("112233", 101); // (documento, id_candidato)
            System.out.println("Respuesta del servidor: " + (result1 ? "ÉXITO" : "FALLO"));

            // 2. Segundo voto (duplicado): Debería fallar.
            System.out.println("\nEnviando segundo voto (duplicado) para el documento '112233'...");
            boolean result2 = voteStation.vote("112233", 101);
            System.out.println("Respuesta del servidor: " + (result2 ? "ÉXITO" : "FALLO"));

            // 3. Voto de otra persona: Debería ser exitoso.
            System.out.println("\nEnviando voto para el documento '445566'...");
            boolean result3 = voteStation.vote("445566", 102);
            System.out.println("Respuesta del servidor: " + (result3 ? "ÉXITO" : "FALLO"));
            
            // 4. Voto para el mismo candidato del voto 3: Debería ser exitoso.
            System.out.println("\nEnviando voto para el documento '778899'...");
            boolean result4 = voteStation.vote("778899", 102);
            System.out.println("Respuesta del servidor: " + (result4 ? "ÉXITO" : "FALLO"));

            System.out.println("\nSolicitando generación del reporte...");
            voteStation.generateReport();

            System.out.println("\n--- Pruebas Finalizadas ---");

        } catch (Exception e) {
            System.err.println("Ocurrió un error en el cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}