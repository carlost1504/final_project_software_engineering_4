import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.QueryStationPrx;

import java.util.Scanner;

public class TestClientQuery {

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("QueryStation:default -p 10000");
            QueryStationPrx queryStation = QueryStationPrx.checkedCast(base);

            if (queryStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a QueryStationPrx.");
            }

            System.out.println("--- Cliente de Consulta de Voto ---");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("\nIngrese documento a consultar (o 'salir'): ");
                String document = scanner.nextLine().trim();
                if (document.equalsIgnoreCase("salir")) break;

                String respuesta = queryStation.query(document);
                System.out.println(" Resultado: " + respuesta);
            }

            System.out.println("\n--- Cliente de consulta finalizado ---");

        } catch (Exception e) {
            System.err.println(" Error en cliente de consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
