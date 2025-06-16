package client.query;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.QueryStationPrx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiQuerySimulator {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso: java client.query.MultiQuerySimulator <configFile> <numClientes> <consultasPorCliente>");
            return;
        }

        String configFile = args[0];
        int numClientes = Integer.parseInt(args[1]);
        int consultasPorCliente = Integer.parseInt(args[2]);

        try (Communicator communicator = Util.initialize(new String[] { "--Ice.Config=" + configFile })) {

            // Obtener proxy del LoadBalancer desde el archivo de configuración
            ObjectPrx base = communicator.propertyToProxy("LoadBalancer.Proxy");
            LoadBalancerPrx lb = LoadBalancerPrx.checkedCast(base);

            if (lb == null) {
                throw new Error("No se pudo obtener el proxy del LoadBalancer.");
            }

            // Solicitar estación de consulta al LoadBalancer
            QueryStationPrx proxy = lb.getQueryStation();
            if (proxy == null) {
                throw new Error("No hay estaciones de consulta disponibles.");
            }

            ExecutorService pool = Executors.newFixedThreadPool(numClientes);
            long start = System.currentTimeMillis();

            for (int i = 0; i < numClientes; i++) {
                final int id = i;
                pool.submit(() -> {
                    for (int j = 0; j < consultasPorCliente; j++) {
                        String cedula = "C" + id + "_" + j;
                        try {
                            String resultado = proxy.query(cedula);
                            System.out.printf("[Cliente %d] Consulta: %s → %s%n", id, cedula, resultado);
                        } catch (Exception e) {
                            System.err.printf("[Cliente %d] Error consultando %s: %s%n", id, cedula, e.getMessage());
                        }
                    }
                });
            }

            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.MINUTES);

            long total = System.currentTimeMillis() - start;
            System.out.println("🕒 Tiempo total de ejecución: " + total + " ms");
        }
    }
}
