// Ruta: client-vote-station/src/main/java/client/vote/VoteStationServer.java
package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.VoteStationPrx;
import server.VoteStationImpl;
import java.util.UUID;

public class VoteStationServer {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("ERROR: Se necesita la IP del Servidor Central (Broker) como primer argumento.");
            System.exit(1);
        }
        String brokerIp = args[0];
        int port = getPort(args, 10001); // Puerto por defecto 10001

        System.out.println("🚀 Iniciando Mesa de Votación en el puerto " + port);

        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "VoteStationAdapter" + port, "tcp -h 0.0.0.0 -p " + port
            );

            adapter.add(new VoteStationImpl(), Util.stringToIdentity("VoteStation-" + port));
            adapter.activate();

            String brokerProxyString = "LoadBalancer:default -h " + brokerIp + " -p 11000";
            System.out.println("Contactando al Servidor Central en: " + brokerProxyString);
            LoadBalancerPrx broker = LoadBalancerPrx.checkedCast(
                    communicator.stringToProxy(brokerProxyString)
            );

            if (broker == null) {
                throw new IllegalStateException(" No se pudo contactar al Servidor Central en " + brokerIp);
            }

            VoteStationPrx selfProxy = VoteStationPrx.uncheckedCast(
                    adapter.createProxy(Util.stringToIdentity("VoteStation-" + port))
            );
            broker.addVoteStation(selfProxy);
            System.out.println("✅ Mesa de Votación registrada en el Servidor Central.");

            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println(" Error en la Mesa de Votación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getPort(String[] args, int defaultPort) {
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                return Integer.parseInt(arg.substring("--port=".length()));
            }
        }
        return defaultPort;
    }
}