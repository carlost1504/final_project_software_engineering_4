package client.query;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.QueryStationPrx;

import java.util.Scanner;

public class QueryClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("QueryStation:default -p 10000");
            QueryStationPrx proxy = QueryStationPrx.checkedCast(base);

            if (proxy == null) {
                throw new Error("Proxy inválido.");
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("=== 🔍 Cliente de Consulta ===");
            System.out.print("Documento: ");
            String document = sc.nextLine();

            String result = proxy.query(document);
            System.out.println("Estado: " + result);

        } catch (Exception e) {
            System.err.println("Error en cliente de consulta: " + e.getMessage());
        }
    }
}
