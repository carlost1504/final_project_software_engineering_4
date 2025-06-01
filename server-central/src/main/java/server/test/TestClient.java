// server-central/src/main/java/server/TestClient.java

package server.test;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;

public class TestClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStationPrx.");
            }

            System.out.println("--- Cliente de Prueba Iniciado ---");

            int stationId = 1; // Puedes cambiar este ID según la estación de prueba

            // 1. Primer voto: Debería ser exitoso.
            System.out.println("\n[TEST 1] Enviando primer voto para documento '112233'...");
            boolean result1 = voteStation.vote("112233", 101, stationId);
            System.out.println("Respuesta del servidor: " + (result1 ? "ÉXITO" : "FALLO"));

            // 2. Segundo voto (duplicado): Debería fallar.
            System.out.println("\n[TEST 2] Enviando segundo voto (duplicado) para '112233'...");
            boolean result2 = voteStation.vote("112233", 101, stationId);
            System.out.println("Respuesta del servidor: " + (result2 ? "ÉXITO" : "FALLO"));

            // 3. Voto de otro ciudadano
            System.out.println("\n[TEST 3] Enviando voto para '445566'...");
            boolean result3 = voteStation.vote("445566", 102, stationId);
            System.out.println("Respuesta del servidor: " + (result3 ? "ÉXITO" : "FALLO"));

            // 4. Otro voto diferente para el mismo candidato
            System.out.println("\n[TEST 4] Enviando voto para '778899'...");
            boolean result4 = voteStation.vote("778899", 102, stationId);
            System.out.println("Respuesta del servidor: " + (result4 ? "ÉXITO" : "FALLO"));

            // Generar el reporte
            System.out.println("\n[REPORTE] Solicitando generación del reporte...");
            voteStation.generateReport();

            System.out.println("\n--- Pruebas Finalizadas ---");

        } catch (Exception e) {
            System.err.println("Error en el cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}