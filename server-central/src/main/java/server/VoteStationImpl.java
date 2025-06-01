package server;

import com.zeroc.Ice.Current;

// Esta clase es la implementación del "servant" de ICE.
// Su única responsabilidad es recibir las llamadas remotas y delegarlas
// al gestor de lógica de negocio (VoteManager), que es un Singleton.
public class VoteStationImpl implements common.VoteStation {

    /**
     * Este método se ejecuta cuando un cliente vota.
     * Delega TODA la lógica a VoteManager.
     */
    @Override
    public boolean vote(String document, int candidateId, Current current) {
        System.out.println("\n--- Petición de Voto Recibida ---");
        System.out.println("Recibido desde el cliente: Documento=" + document + ", CandidatoID=" + candidateId);

        // Obtenemos la instancia única de nuestro gestor de lógica
        VoteManager manager = VoteManager.getInstance();

        // Delegamos el procesamiento y retornamos el resultado directamente.
        // Aquí es donde se actualiza el conteo de votos en memoria.
        boolean success = manager.processVote(document, candidateId);

        System.out.println("Respuesta enviada al cliente: " + (success ? "ÉXITO" : "FALLO (DUPLICADO)"));
        System.out.println("---------------------------------");
        
        return success;
    }

    /**
     * Este método se ejecuta cuando el cliente pide generar el reporte.
     * Delega TODA la lógica a VoteManager.
     */
    @Override
    public void generateReport(com.zeroc.Ice.Current current) {
        System.out.println("\n--- Petición de Generar Reporte Recibida ---");
        // Simplemente le pedimos al VoteManager que haga el trabajo de escribir el CSV.
        VoteManager.getInstance().generateResumeCSV();
    }
}