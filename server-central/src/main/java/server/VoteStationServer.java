package server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import common.VoteStation;

public class VoteStationServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "VoteStationAdapter", "default -p 13000"
            );

            VoteStation servant = new VoteStationImpl(); // tu clase real aquí
            adapter.add(servant, Util.stringToIdentity("VoteStation"));

            adapter.activate();
            System.out.println("✅ VoteStationServer listo en puerto 13000...");
            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println("❌ Error en VoteStationServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
