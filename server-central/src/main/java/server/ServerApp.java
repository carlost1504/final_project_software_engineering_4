package server;

import com.zeroc.Ice.*;

import java.lang.Exception;

public class ServerApp {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "VoteStationAdapter", "default -p 10000");

            VoteStationImpl voteServant = new VoteStationImpl();

            adapter.add(voteServant, Util.stringToIdentity("VoteStation"));
            adapter.activate();

            System.out.println("Servidor VoteStation listo en el puerto 10000...");
            communicator.waitForShutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}