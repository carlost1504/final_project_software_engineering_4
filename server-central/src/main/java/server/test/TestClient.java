package server.test;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

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

            for (int i = 0; i < documents.length; i++) {
                String doc = documents[i];
                int cid = candidateIds[i];
                String data = doc + cid + stationId;

                // Generar HMAC
                String hmac = HmacUtil.generateHmac(data, SecurityConfig.HMAC_SECRET);

                System.out.printf("\n[TEST %d] Voto para '%s'...\n", i + 1, doc);
                boolean result = voteStation.vote(doc, cid, stationId, hmac);
                System.out.println("Resultado: " + (result ? "✅ ÉXITO" : "❌ FALLO"));
            }

            // Intentar voto duplicado para el primero
            String duplicateDoc = documents[0];
            int duplicateCid = candidateIds[0];
            String dupData = duplicateDoc + duplicateCid + stationId;
            String dupHmac = HmacUtil.generateHmac(dupData, SecurityConfig.HMAC_SECRET);

            System.out.println("\n[TEST DUPLICADO] Voto repetido para '" + duplicateDoc + "'...");
            boolean dupResult = voteStation.vote(duplicateDoc, duplicateCid, stationId, dupHmac);
            System.out.println("Resultado: " + (dupResult ? "✅ ÉXITO" : "❌ FALLO"));

            // Generar reporte
            System.out.println("\n[REPORTE] Generando archivo resumen...");
            voteStation.generateReport();

            System.out.println("\n--- Pruebas Finalizadas ---");

        } catch (Exception e) {
            System.err.println("Error en el cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
