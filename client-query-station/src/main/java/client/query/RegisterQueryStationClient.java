package client.query;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.QueryStationPrx;

public class RegisterQueryStationClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            System.out.println(" Iniciando registro de estación de consulta...");

            // Proxy remoto al LoadBalancer
            ObjectPrx baseBalancer = communicator.stringToProxy("LoadBalancer:default -p 11000");
            LoadBalancerPrx loadBalancer = LoadBalancerPrx.checkedCast(baseBalancer);

            if (loadBalancer == null) {
                throw new RuntimeException(" No se pudo obtener el proxy LoadBalancer.");
            }

            // Proxy local de esta estación de consulta
            ObjectPrx baseQueryStation = communicator.stringToProxy("QueryStation:default -p 13000");
            QueryStationPrx queryStation = QueryStationPrx.checkedCast(baseQueryStation);

            if (queryStation == null) {
                throw new RuntimeException(" No se pudo obtener el proxy QueryStation.");
            }

            loadBalancer.addQueryStation(queryStation);
            System.out.println(" Estación de consulta registrada correctamente.");

        } catch (Exception e) {
            System.err.println(" Error al registrar estación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
