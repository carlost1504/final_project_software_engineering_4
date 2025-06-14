package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.VoteStationPrx;

public class RegisterStationsClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            System.out.println(" Iniciando registro de estación de voto...");

            // Proxy remoto al LoadBalancer
            ObjectPrx baseBalancer = communicator.stringToProxy("LoadBalancer:default -p 11000");
            LoadBalancerPrx loadBalancer = LoadBalancerPrx.checkedCast(baseBalancer);

            if (loadBalancer == null) {
                throw new RuntimeException(" No se pudo obtener el proxy LoadBalancer.");
            }

            // Proxy local de esta estación de voto que queremos registrar
            ObjectPrx baseVoteStation = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(baseVoteStation);

            if (voteStation == null) {
                throw new RuntimeException(" No se pudo obtener el proxy VoteStation.");
            }

            loadBalancer.addVoteStation(voteStation);
            System.out.println(" Estación de voto registrada correctamente.");

        } catch (Exception e) {
            System.err.println(" Error al registrar estación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
