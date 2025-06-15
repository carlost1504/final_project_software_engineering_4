// Ruta: server-central/src/main/java/server/BrokerServer.java
package server;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class BrokerServer {
    public static void main(String[] args) {
        System.out.println(" Iniciando Servidor Central (implementa el patrón Broker)...");
        try (Communicator communicator = Util.initialize(args)) {
            // La clave es -h 0.0.0.0 para que escuche en toda la red, no solo en localhost.
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints(
                    "LoadBalancerAdapter", "tcp -h 0.0.0.0 -p 11000"
            );

            // Creamos una instancia de nuestra implementación del Broker.
            LoadBalancerImpl brokerServant = new LoadBalancerImpl();

            // Publicamos el servicio del Broker con una identidad fija y conocida.
            adapter.add(brokerServant, Util.stringToIdentity("LoadBalancer"));

            adapter.activate();
            System.out.println("Servidor Central listo y escuchando en el puerto 11000.");
            System.out.println("   Esperando que las mesas de votación y consulta se registren...");

            communicator.waitForShutdown();
        } catch (Exception e) {
            System.err.println(" Error en el Servidor Central: " + e.getMessage());
            e.printStackTrace();
        }
    }
}