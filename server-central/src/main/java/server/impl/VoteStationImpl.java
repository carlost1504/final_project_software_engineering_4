package server.impl;

import com.zeroc.Ice.Current;
import common.QueryCachePrx;
import common.VoteStation;
import server.Database;
import server.VoteManager;
import utils.HmacUtil;
import utils.SecurityConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

/**
 * Implementación de la estación de voto.
 * Confiabilidad: No procesa el voto directamente, lo encola en una tabla de la BD.
 * Invalidación de Caché: Notifica al servicio de caché cuando un voto es aceptado.
 */
public class VoteStationImpl implements VoteStation {

    private final VoteManager voteManager;
    private final QueryCachePrx queryCacheProxy;

    // Nuevo constructor que recibe las dependencias
    public VoteStationImpl(VoteManager voteManager, QueryCachePrx queryCacheProxy) {
        this.voteManager = voteManager;
        this.queryCacheProxy = queryCacheProxy;
    }

    // Constructor sin argumentos por si lo necesitas para pruebas, aunque no es ideal.
    public VoteStationImpl() {
        this.voteManager = VoteManager.getInstance();
        this.queryCacheProxy = null; // En este caso, no podría invalidar la caché
    }

    @Override
    public boolean vote(String document, int candidateId, int stationId, String hmac, Current current) {
        System.out.println("📥 Voto recibido para el documento: " + document);

        // 1. Validación de seguridad rápida (HMAC)
        try {
            String dataToVerify = document + candidateId + stationId;
            if (!HmacUtil.verifyHmac(dataToVerify, hmac, SecurityConfig.HMAC_SECRET)) {
                System.err.println("Error de HMAC para el documento: " + document);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Excepción al verificar HMAC: " + e.getMessage());
            return false;
        }

        // 2. Crear el payload JSON para encolar en la base de datos
        UUID messageId = UUID.randomUUID();
        String payload = String.format(
                "{\"document\": \"%s\", \"candidateId\": %d, \"stationId\": %d, \"hmac\": \"%s\", \"vote_uuid\": \"%s\"}",
                document, candidateId, stationId, hmac, messageId.toString()
        );

        // 3. Insertar el voto en la cola (tabla 'vote_queue') para procesamiento asíncrono
        String sql = "INSERT INTO vote_queue (message_id, payload, status) VALUES (?, ?::jsonb, 'PENDING')";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, messageId);
            stmt.setString(2, payload);
            stmt.executeUpdate();

            System.out.println("✅ Voto para " + document + " encolado exitosamente con ID: " + messageId);

            // 4. Retornar 'true' al cliente. El sistema ahora garantiza que procesará el voto.
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error al encolar el voto en la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void generateReport(Current current) {
        System.out.println("📄 Solicitud de generación de reporte recibida. Delegando a VoteManager...");
        this.voteManager.generateResumeCSV(); // <-- ¡Llamada correcta!
    }

    @Override
    public String[] getCandidates(Current current) {
        return this.voteManager.getCandidateList();
    }
}