import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.io.File;

public class TestClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStationPrx.");
            }

            System.out.println("---  Cliente de Pruebas Iniciado ---");

            int stationId = 1;
            String[] documents = {"112233", "445566", "778899"};
            int[] candidateIds = {101, 102, 102};

            for (int i = 0; i < documents.length; i++) {
                runVoteTest(voteStation, i + 1, documents[i], candidateIds[i], stationId, true);
            }

            // Test de voto duplicado
            runVoteTest(voteStation, -1, documents[0], candidateIds[0], stationId, false);

            // Generar reporte
            System.out.println("\n[REPORTE] Solicitando generación de resumen...");
            voteStation.generateReport();

            File reportFile = new File("resume.csv");
            if (reportFile.exists() && reportFile.length() > 0) {
                System.out.println(" Reporte generado exitosamente: resume.csv");
            } else {
                System.out.println(" Error: archivo de resumen no generado o vacío.");
            }

            System.out.println("\n---  Todas las pruebas finalizadas ---");

        } catch (Exception e) {
            System.err.println(" Error en el cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runVoteTest(VoteStationPrx voteStation, int testId, String document, int candidateId, int stationId, boolean expectedSuccess) {
        String label = (testId > 0) ? "[TEST " + testId + "]" : "[TEST DUPLICADO]";
        System.out.printf("\n%s Voto para '%s'...\n", label, document);

        try {
            String data = document + candidateId + stationId;
            String hmac = HmacUtil.generateHmac(data, SecurityConfig.HMAC_SECRET);

            boolean result = voteStation.vote(document, candidateId, stationId, hmac);
            boolean passed = result == expectedSuccess;

            System.out.printf("%s Resultado: %s | Esperado: %s\n",
                    passed ? " PASÓ" : " FALLÓ",
                    result ? " ÉXITO" : " FALLO",
                    expectedSuccess ? "" : "");

        } catch (Exception e) {
            System.out.printf(" ERROR durante el test '%s': %s\n", label, e.getMessage());
            e.printStackTrace();
        }
    }

}

