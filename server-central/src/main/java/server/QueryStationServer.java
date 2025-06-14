package server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class QueryStationServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            System.out.println(" Iniciando servidor QueryStation...");

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("QueryStationAdapter", "default -p 12000");
            adapter.add(new QueryStationImpl(), Util.stringToIdentity("QueryStation"));
            adapter.activate();

            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println(" Error en QueryStationServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

