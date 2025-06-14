import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClientFraude {
    public static void main(String[] args) {
        System.out.println("---  Cliente de Pruebas de Seguridad ---");

        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStationPrx.");
            }

            String csvFile = "fraude_tests.csv";
            PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile, false));
            csvWriter.println("timestamp,test_id,document,station_id,result,expected");

            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            //  Test 1: Voto válido
            runFraudTest(csvWriter, voteStation, "TEST1", "556677", 101, 1, true, now, "Voto válido");

            //  Test 2: Duplicado
            runFraudTest(csvWriter, voteStation, "TEST2", "556677", 101, 1, false, now, "Voto duplicado");

            //  Test 3: Estación incorrecta
            runFraudTest(csvWriter, voteStation, "TEST3", "778899", 102, 2, false, now, "Estación incorrecta");

            //  Test 4: Documento no registrado
            runFraudTest(csvWriter, voteStation, "TEST4", "999000", 102, 1, false, now, "Documento no registrado");

            csvWriter.close();
            System.out.println("\n Resultados exportados a: " + csvFile);
            System.out.println("---  Fin de pruebas de seguridad ---");

        } catch (IOException e) {
            System.err.println(" Error al escribir CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println(" Error en cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runFraudTest(PrintWriter csvWriter, VoteStationPrx station, String testId, String doc,
                                     int candidateId, int stationId, boolean expected, String timestamp, String label) {
        try {
            String data = doc + candidateId + stationId;
            String hmac = HmacUtil.generateHmac(data, SecurityConfig.HMAC_SECRET);
            boolean result = station.vote(doc, candidateId, stationId, hmac);

            boolean passed = result == expected;
            System.out.printf("[%s] %s → Resultado: %s | Esperado: %s => %s%n",
                    testId,
                    label,
                    result ? " EXITO" : " FALLO",
                    expected ? " EXITO" : " FALLO",
                    passed ? " PASÓ" : " FALLÓ"
            );

            csvWriter.printf("%s,%s,%s,%d,%s,%s%n",
                    timestamp,
                    testId,
                    doc,
                    stationId,
                    result ? "EXITO" : "FALLO",
                    expected ? "EXITO" : "FALLO"
            );
        } catch (Exception e) {
            System.err.println(" Error en test " + testId + ": " + e.getMessage());
        }
    }
}
