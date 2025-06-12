package server.test;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

public class TestClientQuery {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            ObjectPrx base = communicator.stringToProxy("QueryStation:default -p 10000");
            QueryStationPrx proxy = QueryStationPrx.checkedCast(base);

            if (proxy == null) throw new Error("Proxy inválido para QueryStation.");

            String[] docs = {"112233", "445566", "999000"};

            for (String doc : docs) {
                String result = proxy.query(doc);
                System.out.printf("Consulta para %s → %s%n", doc, result);
            }

        } catch (Exception e) {
            System.err.println("Error en cliente de consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
