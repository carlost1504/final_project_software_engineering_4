package server.service;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import server.impl.VoteStationImpl;

public class VoteStationServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("VoteStationAdapter", "default -p 12000");
            adapter.add(new VoteStationImpl(), Util.stringToIdentity("VoteStation"));
            adapter.activate();

            System.out.println("🗳️ VoteStationServer activo en puerto 12000...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("❌ Error en VoteStationServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

