package server;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import utils.HmacUtil;
import utils.SecurityConfig;

/**
 * Clase que gestiona la lógica principal del sistema de votación.
 * Implementa el patrón Singleton para garantizar una única instancia global.
 * Se encarga de registrar votos válidos, prevenir duplicados, registrar eventos de seguridad
 * y generar archivos de registro CSV.
 */
public class VoteManager {
    private static final VoteManager instance = new VoteManager();

    // Nombres de los archivos CSV
    private static final String PARTIAL_CSV_FILENAME = "partial_votes.csv";
    private static final String RESUME_CSV_FILENAME = "resume.csv";

    private VoteManager() {
        initializeCSVFiles();
    }

    /**
     * Retorna la única instancia de la clase VoteManager.
     * @return instancia singleton
     */
    public static VoteManager getInstance() {
        return instance;
    }

    /**
     * Procesa el voto de un ciudadano. Verifica la firma HMAC, condiciones de elegibilidad y registro previo.
     * Si el voto es válido, se almacena en la base de datos y se registra en un archivo CSV parcial.
     * @param document Documento del votante
     * @param candidateId ID del candidato
     * @param stationId ID de la estación de votación
     * @param hmac Firma de seguridad para verificar la integridad del voto
     * @return true si el voto fue aceptado, false en caso contrario
     */
    public boolean processVote(String document, int candidateId, int stationId, String hmac) {
        System.out.println("[VoteManager] Iniciando validación de voto para: " + document);

        // --- BLOQUE DE VERIFICACIÓN DE HMAC ---
        // Asegura que el voto que viene de la cola es legítimo.
        try {
            String dataToVerify = document + candidateId + stationId;
            if (!HmacUtil.verifyHmac(dataToVerify, hmac, SecurityConfig.HMAC_SECRET)) {
                System.err.println("❌ FRAUDE DETECTADO: HMAC inválido para el documento: " + document);
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error crítico al verificar HMAC: " + e.getMessage());
            return false;
        }
        // --- FIN DEL BLOQUE AÑADIDO ---

        try (Connection conn = Database.getConnection()) {

            // Paso 1: Verificar existencia y estado del votante
            PreparedStatement voterStmt = conn.prepareStatement(
                    "SELECT is_enabled, has_voted, assigned_station_id FROM voters WHERE document = ?"
            );
            voterStmt.setString(1, document);
            ResultSet voterRs = voterStmt.executeQuery();

            if (!voterRs.next()) {
                System.out.println(" Documento no registrado: " + document);
                logSecurityEvent(conn, document, "DOCUMENTO_NO_REGISTRADO",
                        "Intento de voto con documento inexistente", stationId);
                return false;
            }

            boolean isEnabled = voterRs.getBoolean("is_enabled");
            boolean hasVoted = voterRs.getBoolean("has_voted");
            int assignedStationId = voterRs.getInt("assigned_station_id");

            System.out.printf(" Votante encontrado: habilitado=%s, ha_votado=%s, estación_asignada=%d\n",
                    isEnabled, hasVoted, assignedStationId);

            // Paso 2: Validaciones
            if (!isEnabled) {
                System.out.println(" El votante no está habilitado.");
                logSecurityEvent(conn, document, "NO_HABILITADO", "Votante no habilitado", stationId);
                return false;
            }

            if (hasVoted) {
                System.out.println(" El votante ya ha votado.");
                logSecurityEvent(conn, document, "VOTO_MULTIPLE", "Votante ya ha votado", stationId);
                return false;
            }

            if (assignedStationId != stationId) {
                System.out.println(" Estación incorrecta. Esperada: " + assignedStationId + ", Recibida: " + stationId);
                logSecurityEvent(conn, document, "MESA_INCORRECTA",
                        "Intento de votar en estación incorrecta. Asignada: " + assignedStationId + ", Actual: " + stationId,
                        stationId);
                return false;
            }

            // Paso 3: Registrar el voto
            PreparedStatement voteStmt = conn.prepareStatement(
                    "INSERT INTO votes (document, candidate_id, station_id) VALUES (?, ?, ?)"
            );
            voteStmt.setString(1, document);
            voteStmt.setInt(2, candidateId);
            voteStmt.setInt(3, stationId);
            voteStmt.executeUpdate();
            System.out.println(" Voto insertado en base de datos.");

            PreparedStatement updateVoter = conn.prepareStatement(
                    "UPDATE voters SET has_voted = TRUE WHERE document = ?"
            );
            updateVoter.setString(1, document);
            updateVoter.executeUpdate();
            System.out.println(" Estado del votante actualizado.");

            logVoteToPartialCSV(document, candidateId);

            System.out.println(" Voto procesado correctamente para " + document);
            return true;

        } catch (Exception e) {
            System.err.println(" Error al procesar el voto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra eventos de seguridad como intentos de voto inválido o repetido.
     */
    private void logSecurityEvent(Connection conn, String document, String eventType, String description, int stationId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO security_events (document, event_type, description, station_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, document);
            stmt.setString(2, eventType);
            stmt.setString(3, description);
            stmt.setInt(4, stationId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error al registrar evento de seguridad: " + e.getMessage());
        }
    }

    /**
     * Inicializa los archivos CSV con cabeceras.
     */
    private void initializeCSVFiles() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARTIAL_CSV_FILENAME, false))) {
            writer.println("timestamp,document,candidate_id");
        } catch (IOException e) {
            System.err.println("Error al inicializar partial_votes.csv: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(RESUME_CSV_FILENAME, false))) {
            writer.println("candidate_id,candidate_name,total_votes");
        } catch (IOException e) {
            System.err.println("Error al inicializar resume.csv: " + e.getMessage());
        }
    }

    /**
     * Registra cada voto aceptado en el archivo partial_votes.csv.
     */
    private synchronized void logVoteToPartialCSV(String document, int candidateId) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARTIAL_CSV_FILENAME, true))) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.println(timestamp + "," + document + "," + candidateId);
        } catch (IOException e) {
            System.err.println("Error al registrar voto en CSV parcial: " + e.getMessage());
        }
    }

    /**
     * Genera el reporte resumen de votos por candidato a partir de la base de datos.
     */
    public void generateResumeCSV() {
        try (Connection conn = Database.getConnection();
             PrintWriter writer = new PrintWriter(new FileWriter(RESUME_CSV_FILENAME, false))) {

            writer.println("candidate_id,candidate_name,total_votes");

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.candidate_id, c.name, COUNT(v.vote_id) AS total_votes " +
                            "FROM candidates c LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                            "GROUP BY c.candidate_id, c.name ORDER BY total_votes DESC"
            );

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("candidate_id");
                String name = rs.getString("name");
                int total = rs.getInt("total_votes");
                writer.println(id + "," + name + "," + total);
            }

            System.out.println("LOG: Resumen generado correctamente en resume.csv");
        } catch (Exception e) {
            System.err.println("Error al generar el resumen: " + e.getMessage());
        }
    }

    /**
     * Devuelve una lista de candidatos para que el cliente pueda mostrarla.
     * @return Un arreglo de strings con los candidatos.
     */
    public String[] getCandidateList() {
        // Por ahora, es una lista fija. En el futuro, podría venir de la tabla 'candidates'.
        return new String[] {
                "ID: 101 - Ada Lovelace",
                "ID: 102 - Grace Hopper",
                "ID: 999 - Voto en Blanco"
        };
    }
}