package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiVoteSimulator {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Uso: java client.vote.MultiVoteSimulator <configFile> <numClientes> <votosPorCliente>");
            return;
        }

        String configFile = args[0];
        int numClientes = Integer.parseInt(args[1]);
        int votosPorCliente = Integer.parseInt(args[2]);

        String[] iceArgs = new String[] { "--Ice.Config=" + configFile };
        try (Communicator communicator = Util.initialize(iceArgs)) {

            ObjectPrx base = communicator.propertyToProxy("LoadBalancer.Proxy");
            LoadBalancerPrx loadBalancer = LoadBalancerPrx.checkedCast(base);

            if (loadBalancer == null) {
                throw new Error("No se pudo obtener el proxy del LoadBalancer.");
            }

            VoteStationPrx proxy = loadBalancer.getVoteStation();

            if (proxy == null) {
                throw new Error("No hay estaciones de voto disponibles.");
            }

            ExecutorService pool = Executors.newFixedThreadPool(numClientes);
            AtomicInteger exitosos = new AtomicInteger(0);
            AtomicInteger fallidos = new AtomicInteger(0);

            long start = System.currentTimeMillis();

            for (int i = 0; i < numClientes; i++) {
                final int clientId = i;
                pool.submit(() -> {
                    for (int j = 0; j < votosPorCliente; j++) {
                        String document = "C" + clientId + "_" + j;
                        int candidateId = (j % 3) + 1; // IDs 1, 2, 3
                        int stationId = 1;
                        String data = document + candidateId + stationId;

                        try {
                            String hmac = HmacUtil.generateHmac(data, SecurityConfig.HMAC_SECRET);
                            boolean success = proxy.vote(document, candidateId, stationId, hmac);
                            if (success) {
                                exitosos.incrementAndGet();
                            } else {
                                fallidos.incrementAndGet();
                            }
                        } catch (Exception e) {
                            fallidos.incrementAndGet();
                            System.err.printf("[Cliente %d] Error al votar con %s: %s%n", clientId, document, e.getMessage());
                        }
                    }
                });
            }

            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.MINUTES);

            long total = System.currentTimeMillis() - start;
            int totalVotos = numClientes * votosPorCliente;
            double promedioPorVoto = total / (double) totalVotos;

            System.out.println("🕒 Tiempo total de ejecución: " + total + " ms");
            System.out.println("✔️ Votos exitosos: " + exitosos.get());
            System.out.println("❌ Votos fallidos: " + fallidos.get());
            System.out.printf("⏱️ Tiempo promedio por voto: %.2f ms%n", promedioPorVoto);

            try (PrintWriter out = new PrintWriter(new FileWriter("vote_simulation_metrics.csv", true))) {
                out.printf("%d,%d,%d,%d,%d,%.2f%n",
                        numClientes,
                        votosPorCliente,
                        totalVotos,
                        exitosos.get(),
                        total,
                        promedioPorVoto);
                System.out.println("📊 Métricas guardadas en vote_simulation_metrics.csv");
            } catch (Exception e) {
                System.err.println("❌ Error al guardar métricas: " + e.getMessage());
            }
        }
    }
}
