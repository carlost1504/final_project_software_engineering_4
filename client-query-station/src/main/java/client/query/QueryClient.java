package client.query;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.QueryStationPrx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class QueryClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("QueryStation:default -p 13000");
            QueryStationPrx queryStation = QueryStationPrx.checkedCast(base);

            if (queryStation == null) {
                throw new Error("Proxy inválido.");
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("=== Cliente de Consulta de Votante ===");
            initializeQueryLog();

            while (true) {
                System.out.println("\n1.  Consultar documento");
                System.out.println("0.  Salir");
                System.out.print("Seleccione una opción: ");
                String opcion = sc.nextLine().trim();

                if (opcion.equals("0")) {
                    System.out.println(" Saliendo...");
                    break;
                } else if (opcion.equals("1")) {
                    System.out.print(" Documento: ");
                    String document = sc.nextLine().trim();
                    String result = queryStation.query(document);
                    mostrarResultado(document, result);
                    logQuery(document, result);
                } else {
                    System.out.println(" Opción inválida");
                }
            }

        } catch (Exception e) {
            System.err.println(" Error en cliente de consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarResultado(String document, String result) {
        System.out.println("\n Resultado de la consulta para el documento " + document + ":");
        System.out.println("--------------------------------------------------");
        System.out.println(" Estado: " + result);
        System.out.println("--------------------------------------------------");
    }

    private static void logQuery(String document, String result) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("query_logs.csv", true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.printf("%s,%s,%s%n", timestamp, document, result.replace(",", " ")); // evita romper CSV
        } catch (IOException e) {
            System.err.println(" Error al escribir en query_logs.csv: " + e.getMessage());
        }
    }

    private static void initializeQueryLog() {
        try {
            File logFile = new File("query_logs.csv");
            if (!logFile.exists()) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(logFile))) {
                    writer.println("timestamp,document,result");
                }
            }
        } catch (IOException e) {
            System.err.println(" Error al inicializar query_logs.csv: " + e.getMessage());
        }
    }
}


