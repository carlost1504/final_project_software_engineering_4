package server.service;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
import common.LoadBalancer;
import server.impl.LoadBalancerImpl;

public class LoadBalancerServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Crear el adaptador en el puerto 11000
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "LoadBalancerAdapter", "default -p 11000"
            );

            // Crear la implementación del balanceador
            LoadBalancer loadBalancer = new LoadBalancerImpl();

            // Registrar el objeto con el nombre "LoadBalancer"
            adapter.add(loadBalancer, Util.stringToIdentity("LoadBalancer"));

            adapter.activate();
            System.out.println(" LoadBalancerServer iniciado en el puerto 11000...");
            communicator.waitForShutdown();

        } catch (Exception e) {
            System.err.println(" Error en LoadBalancerServer: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
