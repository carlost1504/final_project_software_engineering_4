package server.test;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestClientFraude {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStationPrx.");
            }

            System.out.println("--- Cliente de Pruebas de Seguridad ---");

            // Inicializar archivo CSV
            String csvFile = "fraude_tests.csv";
            PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile, false));
            csvWriter.println("timestamp,test_id,document,station_id,result");

            // Timestamp para los registros
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Prueba 1: Voto válido
            int estacionCorrecta = 1;
            String doc1 = "556677";
            System.out.println("\n[TEST 1] Voto válido de '" + doc1 + "' en estación " + estacionCorrecta);
            boolean t1 = voteStation.vote(doc1, 101, estacionCorrecta);
            csvWriter.printf("%s,TEST1,%s,%d,%s%n", now, doc1, estacionCorrecta, (t1 ? "EXITO" : "FALLO"));

            // Prueba 2: Voto duplicado
            System.out.println("\n[TEST 2] Voto duplicado de '" + doc1 + "' en estación " + estacionCorrecta);
            boolean t2 = voteStation.vote(doc1, 101, estacionCorrecta);
            csvWriter.printf("%s,TEST2,%s,%d,%s%n", now, doc1, estacionCorrecta, (t2 ? "EXITO" : "FALLO"));

            // Prueba 3: Estación incorrecta
            int estacionIncorrecta = 2;
            String doc2 = "778899";
            System.out.println("\n[TEST 3] Intento de '" + doc2 + "' en estación INCORRECTA (esperada 1, se usa 2)");
            boolean t3 = voteStation.vote(doc2, 102, estacionIncorrecta);
            csvWriter.printf("%s,TEST3,%s,%d,%s%n", now, doc2, estacionIncorrecta, (t3 ? "EXITO" : "FALLO"));

            // Prueba 4: Documento no registrado
            String docInvalido = "999000";
            System.out.println("\n[TEST 4] Documento no registrado '" + docInvalido + "'");
            boolean t4 = voteStation.vote(docInvalido, 102, 1);
            csvWriter.printf("%s,TEST4,%s,%d,%s%n", now, docInvalido, 1, (t4 ? "EXITO" : "FALLO"));

            csvWriter.close();
            System.out.println("\n--- Fin de pruebas de seguridad ---");
            System.out.println("Resultados exportados a: " + csvFile);

        } catch (IOException e) {
            System.err.println("Error al escribir CSV: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error en cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
