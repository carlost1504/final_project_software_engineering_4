// server-central/src/main/java/server/VoteStationImpl.java
package server.impl;

import com.zeroc.Ice.Current;
import common.VoteStation;
import server.Database;
import server.VoteManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la interfaz remota VoteStation definida en ICE.
 * Esta clase actúa como puente entre el cliente y la lógica de negocio,
 * delegando la responsabilidad de registrar votos y generar reportes
 * al VoteManager (que sigue el patrón Singleton).
 */
public class VoteStationImpl implements VoteStation {

    /**
     * Procesa una solicitud de voto proveniente del cliente.
     *
     * @param document Documento de identificación del votante.
     * @param candidateId ID del candidato seleccionado.
     * @param stationId ID de la estación donde se emite el voto.
     * @param hmac Firma HMAC (por ahora no validada).
     * @param current Contexto de la llamada remota.
     * @return true si el voto fue registrado exitosamente, false si fue rechazado.
     */
    @Override
    public boolean vote(String document, int candidateId, int stationId, String hmac, Current current) {
        System.out.println("\n--- [SERVIDOR] Petición de Voto Recibida ---");
        System.out.printf("-> Documento: %s, Candidato ID: %d, Estación ID: %d%n", document, candidateId, stationId);

        String data = document + candidateId + stationId;
        try {

            String expectedHmac = utils.HmacUtil.generateHmac(data, utils.SecurityConfig.HMAC_SECRET);
            System.out.println(" HMAC esperado  : " + expectedHmac);
            System.out.println(" HMAC recibido  : " + hmac);

            if (!expectedHmac.equals(hmac)) {
                System.out.println("->  HMAC inválido. Posible intento de manipulación.");
                return false;
            }

        } catch (Exception e) {
            System.out.println("->  Error al validar HMAC: " + e.getMessage());
            return false;
        }

        VoteManager manager = VoteManager.getInstance();
        boolean success = manager.processVote(document, candidateId, stationId);

        System.out.println("-> Resultado: " + (success ? " ÉXITO" : " FALLO"));
        System.out.println("--------------------------------------------");

        return success;
    }

    @Override
    public String[] getCandidates(Current current) {
        List<String> candidatos = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT candidate_id, name, party FROM candidates");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                candidatos.add("ID: " + rs.getInt("candidate_id") +
                        " | Nombre: " + rs.getString("name") +
                        " | Partido: " + rs.getString("party"));
            }
        } catch (Exception e) {
            System.err.println("Error al consultar candidatos: " + e.getMessage());
        }
        return candidatos.toArray(new String[0]);  // Conversión correcta
    }







    /**
     * Solicita la generación del archivo de resumen de votos.
     *
     * @param current Contexto de la llamada remota.
     */
    @Override
    public void generateReport(Current current) {
        System.out.println("\n--- [SERVIDOR] Petición de generación de reporte recibida ---");
        VoteManager.getInstance().generateResumeCSV();
        System.out.println("-> Reporte generado correctamente.");
    }
}
