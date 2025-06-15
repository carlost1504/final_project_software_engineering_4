package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import common.LoadBalancerPrx; // Importación necesaria para el Broker
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class VoteClient {
    public static void main(String[] args) {
        // El cliente necesita saber la IP del Servidor Central (Broker).
        // Se la pasaremos como primer argumento al ejecutarlo desde la terminal.
        if (args.length == 0) {
            System.err.println("ERROR: Se necesita la IP del Servidor Central (Broker) como primer argumento.");
            System.err.println("Ejemplo de uso: java -cp <classpath> client.vote.VoteClient <IP_DEL_BROKER>");
            System.exit(1);
        }
        String brokerIp = args[0];

        try (Communicator communicator = Util.initialize(args)) {

            // ================== INICIO DEL CÓDIGO MODIFICADO ==================

            // 1. Conectarse al Broker usando la IP proporcionada.
            //    El Broker está en el puerto 11000.
            String brokerProxyString = "LoadBalancer:default -h " + brokerIp + " -p 11000";
            System.out.println("Contactando al Servidor Central en: " + brokerProxyString);

            LoadBalancerPrx broker = LoadBalancerPrx.checkedCast(
                    communicator.stringToProxy(brokerProxyString)
            );

            if (broker == null) {
                throw new Error(" No se pudo contactar al Servidor Central. Verifique la IP y que el Broker esté corriendo.");
            }

            // 2. Pedir una mesa de votación al Broker.
            System.out.println("Solicitando una mesa de votación disponible...");
            VoteStationPrx voteStation = broker.getVoteStation();

            // 3. Verificar si el Broker nos dio una mesa.
            if (voteStation == null) {
                throw new Error("❌ El Servidor Central no reporta mesas de votación disponibles en este momento. Intente más tarde.");
            }

            // =================== FIN DEL CÓDIGO MODIFICADO ===================

            System.out.println("✅ Conexión establecida a una mesa de votación. ¡Listo para votar!");

            Scanner sc = new Scanner(System.in);
            System.out.println("===  Cliente de Votación ===");
            initializeVoteLog();

            // El resto de la lógica del menú se mantiene igual que la tenías.
            while (true) {
                System.out.println("\n1. Emitir voto");
                System.out.println("2. Generar resumen de votos (función de admin)");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                String opcion = sc.nextLine().trim();

                if (opcion.equals("0")) {
                    System.out.println("Saliendo...");
                    break;
                } else if (opcion.equals("1")) {
                    try {
                        System.out.print("Documento: ");
                        String document = sc.nextLine().trim();
                        System.out.print("ID Candidato: ");
                        int candidateId = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("ID Estación (simulado, para registro): ");
                        int stationId = Integer.parseInt(sc.nextLine().trim());

                        // La llamada a vote() ahora se hace sobre el proxy que nos dio el Broker
                        String hmac = HmacUtil.generateHmac(document + candidateId + stationId, SecurityConfig.HMAC_SECRET);
                        boolean success = voteStation.vote(document, candidateId, stationId, hmac);

                        System.out.println("→ Resultado: " + (success ? "Voto registrado con éxito" : "Voto rechazado por el servidor"));
                        logVote(document, candidateId, stationId, success);
                    } catch (NumberFormatException e) {
                        System.err.println("Error: El ID del candidato y de la estación deben ser números enteros.");
                    } catch (Exception e) {
                        System.err.println("Ocurrió un error al procesar el voto: " + e.getMessage());
                        // Si la conexión con la mesa de votación se cae, el proxy lo detectará aquí.
                        e.printStackTrace();
                        break; // Salir del bucle si hay un error de comunicación grave.
                    }
                } else if (opcion.equals("2")) {
                    try {
                        System.out.println("Enviando solicitud para generar reporte...");
                        voteStation.generateReport();
                        System.out.println("Solicitud de reporte enviada a la estación.");
                    } catch (Exception e) {
                        System.err.println("Ocurrió un error al solicitar el reporte: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                } else {
                    System.out.println("Opción inválida");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal en el cliente de votación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Tus métodos de logging no necesitan cambios.
    private static void logVote(String document, int candidateId, int stationId, boolean success) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("vote_logs.csv", true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.printf("%s,%s,%d,%d,%s%n", timestamp, document, candidateId, stationId, success ? "EXITO" : "FALLO");
        } catch (IOException e) {
            System.err.println("Error al escribir en el log de votos: " + e.getMessage());
        }
    }

    private static void initializeVoteLog() {
        try {
            File logFile = new File("vote_logs.csv");
            if (!logFile.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                    writer.println("timestamp,document,candidate_id,station_id,result");
                }
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar el log de votos: " + e.getMessage());
        }
    }
}