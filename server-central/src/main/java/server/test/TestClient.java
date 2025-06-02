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

            int stationId = 1; // Debe coincidir con el assigned_station_id en la tabla voters

            String[] documents = {"112233", "445566", "778899"};
            int[] candidateIds = {101, 102, 102};

            // Test 1: Voto válido
            System.out.println("\n[TEST 1] Voto para '" + documents[0] + "'...");
            boolean result1 = voteStation.vote(documents[0], candidateIds[0], stationId);
            System.out.println("Resultado: " + (result1 ? "✅ ÉXITO" : "❌ FALLO"));

            // Test 2: Voto duplicado
            System.out.println("\n[TEST 2] Voto duplicado para '" + documents[0] + "'...");
            boolean result2 = voteStation.vote(documents[0], candidateIds[0], stationId);
            System.out.println("Resultado: " + (result2 ? "✅ ÉXITO" : "❌ FALLO"));

            // Test 3: Otro votante válido
            System.out.println("\n[TEST 3] Voto para '" + documents[1] + "'...");
            boolean result3 = voteStation.vote(documents[1], candidateIds[1], stationId);
            System.out.println("Resultado: " + (result3 ? "✅ ÉXITO" : "❌ FALLO"));

            // Test 4: Otro votante para el mismo candidato
            System.out.println("\n[TEST 4] Voto para '" + documents[2] + "'...");
            boolean result4 = voteStation.vote(documents[2], candidateIds[2], stationId);
            System.out.println("Resultado: " + (result4 ? "✅ ÉXITO" : "❌ FALLO"));

            // Reporte
            System.out.println("\n[REPORTE] Generando archivo resumen...");
            voteStation.generateReport();

            System.out.println("\n--- Pruebas Finalizadas ---");
        } catch (Exception e) {
            System.err.println("Error en el cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
