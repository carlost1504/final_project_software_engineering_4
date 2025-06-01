// server-central/src/main/java/server/VoteManager.java

package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que gestiona toda la lógica de negocio para la votación.
 * Utiliza el patrón Singleton para garantizar una única instancia centralizada
 * que gestione el estado de todos los votos.
 */
public class VoteManager {

    private static final VoteManager instance = new VoteManager();

    // --- Constantes para los nombres de archivo ---
    private static final String PARTIAL_CSV_FILENAME = "partial_votes.csv";
    private static final String RESUME_CSV_FILENAME = "resume.csv";

    private final Set<String> documentosVotantes = Collections.synchronizedSet(new HashSet<>());
    private final Map<Integer, Integer> conteoVotos = new ConcurrentHashMap<>();

    private VoteManager() {
        // Al iniciar el servidor, creamos o limpiamos los archivos CSV con sus encabezados.
        initializeCSVFiles();
        reconstruirEstadoDesdeParcial();
    }

    public static VoteManager getInstance() {
        return instance;
    }

    /**
     * Prepara los archivos CSV al inicio, escribiendo las cabeceras.
     */
    private void initializeCSVFiles() {
        // Inicializa el archivo de resumen
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESUME_CSV_FILENAME, false))) {
            writer.println("candidate_id,total_votes");
        } catch (IOException e) {
            System.err.println("Error al inicializar el archivo de resumen: " + e.getMessage());
        }

        // Inicializa el archivo parcial
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARTIAL_CSV_FILENAME, false))) {
            writer.println("timestamp,document,candidate_id");
        } catch (IOException e) {
            System.err.println("Error al inicializar el archivo parcial: " + e.getMessage());
        }
    }


    public boolean processVote(String document, int candidateId) {
        if (documentosVotantes.add(document)) {
            conteoVotos.merge(candidateId, 1, Integer::sum);

            System.out.println("LOG: Voto ACEPTADO para documento: " + document + ", candidato: " + candidateId);

            // PASO 3: Al aceptar un voto, lo registramos en el CSV parcial.
            logVoteToPartialCSV(document, candidateId);

            return true;
        } else {
            System.out.println("LOG: Voto RECHAZADO (duplicado) para documento: " + document);
            return false;
        }
    }

    /**
     * Añade una línea al archivo CSV parcial por cada voto válido.
     * Es 'synchronized' para evitar que múltiples hilos escriban al mismo tiempo y corrompan el archivo.
     *
     * @param document    El documento del votante.
     * @param candidateId El ID del candidato.
     */
    private synchronized void logVoteToPartialCSV(String document, int candidateId) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARTIAL_CSV_FILENAME, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println(timestamp + "," + document + "," + candidateId);
        } catch (IOException e) {
            System.err.println("Error al escribir en el log parcial CSV: " + e.getMessage());
        }
    }

    /**
     * Genera el archivo CSV con el resumen final de votos por candidato.
     * Este método sobreescribe el archivo cada vez que es llamado con los datos más recientes.
     */
    public void generateResumeCSV() {
    System.out.println("\nLOG: Iniciando generateResumeCSV()..."); // Log inicial
    System.out.println("LOG: Contenido de conteoVotos ANTES de escribir:");
    if (conteoVotos.isEmpty()) {
        System.out.println("LOG: ¡El mapa conteoVotos está VACÍO!");
    } else {
        for (Map.Entry<Integer, Integer> entry : conteoVotos.entrySet()) {
            System.out.println("LOG: Candidato: " + entry.getKey() + " -> Votos: " + entry.getValue());
        }
    }

    System.out.println("LOG: Escribiendo en archivo: " + RESUME_CSV_FILENAME);
    try (PrintWriter writer = new PrintWriter(new FileWriter(RESUME_CSV_FILENAME, false))) {
        writer.println("candidate_id,total_votes");
        for (Map.Entry<Integer, Integer> entry : conteoVotos.entrySet()) {
            writer.println(entry.getKey() + "," + entry.getValue());
        }
        writer.flush(); // Forzar escritura
        System.out.println("LOG: Resumen de votos generado exitosamente en disco.");
    } catch (IOException e) {
        System.err.println("Error al generar el CSV de resumen: " + e.getMessage());
        e.printStackTrace(); // Imprime el stack trace completo del error
    }
    System.out.println("LOG: Finalizando generateResumeCSV().");
    }   
    private void reconstruirEstadoDesdeParcial() {
    try (BufferedReader br = new BufferedReader(new FileReader(PARTIAL_CSV_FILENAME))) {
        String line;
        br.readLine(); // Saltar la cabecera
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String doc = parts[1];
                int candidateId = Integer.parseInt(parts[2]);

                // Evita duplicados (por si se reinicia varias veces)
                if (documentosVotantes.add(doc)) {
                    conteoVotos.merge(candidateId, 1, Integer::sum);
                }
            }
        }
        System.out.println("LOG: Estado reconstruido desde partial_votes.csv");
    } catch (IOException e) {
        System.err.println("Error al reconstruir estado desde CSV: " + e.getMessage());
    }
}
}

