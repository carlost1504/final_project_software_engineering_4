// server-central/src/main/java/server/VoteStationImpl.java
package server;

import com.zeroc.Ice.Current;
import common.VoteStation;

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
            boolean isValid = utils.HmacUtil.verifyHmac(data, utils.SecurityConfig.HMAC_SECRET, hmac);

            if (!isValid) {
                System.out.println("-> ❌ HMAC inválido. Posible intento de manipulación.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("-> ❌ Error al validar HMAC: " + e.getMessage());
            return false;
        }

        VoteManager manager = VoteManager.getInstance();
        boolean success = manager.processVote(document, candidateId, stationId);

        System.out.println("-> Resultado: " + (success ? "✅ ÉXITO" : "❌ FALLO"));
        System.out.println("--------------------------------------------");

        return success;
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
