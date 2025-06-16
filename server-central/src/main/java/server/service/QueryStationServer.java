package server.service;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import server.impl.QueryStationImpl;

public class QueryStationServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("QueryStationAdapter", "default -p 13000");
            adapter.add(new QueryStationImpl(), Util.stringToIdentity("QueryStation"));
            adapter.activate();

            System.out.println("🔍 QueryStationServer activo en puerto 13000...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("❌ Error en QueryStationServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


