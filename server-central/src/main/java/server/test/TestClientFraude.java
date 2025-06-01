package server.test;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;

public class TestClientFraude {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStationPrx.");
            }

            System.out.println("--- Cliente de Pruebas de Seguridad ---");

            // Prueba 1: Voto válido en estación correcta
            int estacionCorrecta = 1;
            System.out.println("\n[TEST 1] Voto válido de '556677' en estación 1...");
            boolean t1 = voteStation.vote("556677", 101, estacionCorrecta);
            System.out.println("Resultado: " + (t1 ? "ÉXITO" : "FALLO"));

            // Prueba 2: Voto duplicado
            System.out.println("\n[TEST 2] Voto duplicado de '556677' en estación 1...");
            boolean t2 = voteStation.vote("556677", 101, estacionCorrecta);
            System.out.println("Resultado: " + (t2 ? "ÉXITO" : "FALLO"));

            // Prueba 3: Voto desde estación incorrecta
            int estacionIncorrecta = 2;
            System.out.println("\n[TEST 3] Intento de '778899' en estación INCORRECTA (esperada 1, se usa 2)...");
            boolean t3 = voteStation.vote("778899", 102, estacionIncorrecta);
            System.out.println("Resultado: " + (t3 ? "ÉXITO" : "FALLO"));

            // Prueba 4: Documento inexistente
            System.out.println("\n[TEST 4] Documento no registrado '999000'...");
            boolean t4 = voteStation.vote("999000", 102, 1);
            System.out.println("Resultado: " + (t4 ? "ÉXITO" : "FALLO"));

            System.out.println("\n--- Fin de pruebas de seguridad ---");

        } catch (Exception e) {
            System.err.println("Error en cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
