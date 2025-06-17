package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

public class VoteClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // Leer configuración desde config.vote
            Properties config = new Properties();
            try (InputStream input = VoteClient.class.getClassLoader().getResourceAsStream("config.vote")) {
                config.load(input);
            }

            String endpoint = config.getProperty("VoteStationAdapter.Endpoints").trim();
            int stationId = Integer.parseInt(config.getProperty("station.id").trim());

            // Crear proxy a la estación de voto
            ObjectPrx base = communicator.stringToProxy("VoteStation:" + endpoint);
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido.");
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("=== Cliente de Votación ===");
            initializeVoteLog();

            while (true) {
                System.out.println("\n1. Emitir voto");
                System.out.println("2. Generar resumen de votos");
                System.out.println("0. Salir");
                System.out.print("Seleccione una opción: ");
                String opcion = sc.nextLine().trim();

                if (opcion.equals("0")) {
                    System.out.println("Saliendo...");
                    break;
                } else if (opcion.equals("1")) {
                    mostrarCandidatos(voteStation);
                    System.out.print("Documento: ");
                    String document = sc.nextLine().trim();
                    System.out.print("ID Candidato: ");
                    int candidateId = Integer.parseInt(sc.nextLine().trim());

                    String hmac = HmacUtil.generateHmac(document + candidateId + stationId, SecurityConfig.HMAC_SECRET);
                    boolean success = voteStation.vote(document, candidateId, stationId, hmac);
                    System.out.println("→ Resultado: " + (success ? "Voto registrado" : "Voto rechazado"));
                    logVote(document, candidateId, stationId, success);

                } else if (opcion.equals("2")) {
                    voteStation.generateReport();
                } else {
                    System.out.println("Opción inválida");
                }
            }

        } catch (Exception e) {
            System.err.println("Error en cliente de votación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarCandidatos(VoteStationPrx voteStation) {
        try {
            String[] candidatos = voteStation.getCandidates();
            System.out.println("\n--- Candidatos Disponibles ---");
            for (String c : candidatos) {
                System.out.println(c);
            }
            System.out.println("-------------------------------");
        } catch (Exception e) {
            System.err.println("Error al obtener candidatos del servidor: " + e.getMessage());
        }
    }

    private static void logVote(String document, int candidateId, int stationId, boolean success) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("vote_logs.csv", true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.printf("%s,%s,%d,%d,%s%n", timestamp, document, candidateId, stationId, success ? "EXITO" : "FALLO");
        } catch (IOException e) {
            System.err.println("Error al escribir en vote_logs.csv: " + e.getMessage());
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
            System.err.println("Error al inicializar vote_logs.csv: " + e.getMessage());
        }
    }
}

