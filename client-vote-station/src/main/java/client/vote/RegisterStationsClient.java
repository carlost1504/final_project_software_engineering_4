package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.VoteStationPrx;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class RegisterStationsClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            System.out.println("Iniciando registro de estación de voto...");

            // Cargar configuración desde config.vote
            Properties config = new Properties();
            try (InputStream input = RegisterStationsClient.class.getClassLoader().getResourceAsStream("config.vote")) {

                config.load(input);
            }

            String balancerProxy = config.getProperty("LoadBalancer.Proxy").trim();
            String stationEndpoint = config.getProperty("VoteStationAdapter.Endpoints").trim();
            int stationId = Integer.parseInt(config.getProperty("station.id").trim());

            // Proxy remoto al LoadBalancer
            ObjectPrx baseBalancer = communicator.stringToProxy(balancerProxy);
            LoadBalancerPrx loadBalancer = LoadBalancerPrx.checkedCast(baseBalancer);

            if (loadBalancer == null) {
                throw new RuntimeException("No se pudo obtener el proxy LoadBalancer.");
            }

            // Proxy local de esta estación de voto que queremos registrar
            ObjectPrx baseVoteStation = communicator.stringToProxy("VoteStation:" + stationEndpoint);
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(baseVoteStation);

            if (voteStation == null) {
                throw new RuntimeException("No se pudo obtener el proxy VoteStation.");
            }

            loadBalancer.addVoteStation(voteStation);
            System.out.println("Estación de voto registrada correctamente (ID " + stationId + ").");

        } catch (Exception e) {
            System.err.println("Error al registrar estación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
