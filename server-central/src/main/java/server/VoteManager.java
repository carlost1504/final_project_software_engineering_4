package server;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;

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
     * Procesa el voto de un ciudadano. Verifica condiciones de elegibilidad y registro previo.
     * Si el voto es válido, se almacena en la base de datos y se registra en un archivo CSV parcial.
     * @param document Documento del votante
     * @param candidateId ID del candidato
     * @param stationId ID de la estación de votación
     * @return true si el voto fue aceptado, false en caso contrario
     */
    public boolean processVote(String document, int candidateId, int stationId) {
        try (Connection conn = Database.getConnection()) {
            // Verificar si el votante existe, está habilitado y no ha votado
            PreparedStatement voterStmt = conn.prepareStatement("SELECT is_enabled, has_voted FROM voters WHERE document = ?");
            voterStmt.setString(1, document);
            ResultSet voterRs = voterStmt.executeQuery();

            if (!voterRs.next()) {
                logSecurityEvent(conn, document, "DOCUMENTO_NO_REGISTRADO", "Intento de voto con documento inexistente", stationId);
                return false;
            }

            boolean isEnabled = voterRs.getBoolean("is_enabled");
            boolean hasVoted = voterRs.getBoolean("has_voted");

            if (!isEnabled) {
                logSecurityEvent(conn, document, "NO_HABILITADO", "Votante no habilitado", stationId);
                return false;
            }

            if (hasVoted) {
                logSecurityEvent(conn, document, "VOTO_MULTIPLE", "Votante ya ha votado", stationId);
                return false;
            }

            // Registrar el voto en la base de datos
            PreparedStatement voteStmt = conn.prepareStatement(
                    "INSERT INTO votes (document, candidate_id, station_id) VALUES (?, ?, ?)"
            );
            voteStmt.setString(1, document);
            voteStmt.setInt(2, candidateId);
            voteStmt.setInt(3, stationId);
            voteStmt.executeUpdate();

            // Actualizar estado del votante
            PreparedStatement updateVoter = conn.prepareStatement("UPDATE voters SET has_voted = TRUE WHERE document = ?");
            updateVoter.setString(1, document);
            updateVoter.executeUpdate();

            // Registrar en CSV parcial
            logVoteToPartialCSV(document, candidateId);

            System.out.println("LOG: Voto registrado con éxito: " + document + " -> Candidato " + candidateId);
            return true;

        } catch (Exception e) {
            System.err.println("Error al procesar el voto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra eventos de seguridad como intentos de voto inválido o repetido.
     * @param conn Conexión activa a la base de datos
     * @param document Documento del votante
     * @param eventType Tipo de evento (ej. VOTO_MULTIPLE)
     * @param description Descripción del evento
     * @param stationId Estación desde la que ocurrió el evento
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
     * Inicializa los archivos CSV con cabeceras. Se ejecuta al iniciar el sistema.
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
     * @param document Documento del votante
     * @param candidateId ID del candidato votado
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
     * Se guarda como archivo CSV llamado resume.csv
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
}
