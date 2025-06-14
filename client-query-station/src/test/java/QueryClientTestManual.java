import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.QueryStationPrx;

public class QueryClientTestManual {
    public static void main(String[] args) {
        System.out.println("🔍 Iniciando pruebas de consulta contra el servidor...");

        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("QueryStation:default -p 10000");
            QueryStationPrx queryStation = QueryStationPrx.checkedCast(base);

            if (queryStation == null) {
                throw new Error("Proxy inválido.");
            }

            runTest(queryStation, "✅ Caso válido - Juan", "112233", "✅ Habilitado. Estación asignada: 1");
            runTest(queryStation, "❌ No habilitado - Carlos", "000111", "❌ Votante no habilitado.");
            runTest(queryStation, "❌ Ya votó - María", "222333", "❌ Ya ha votado.");
            runTest(queryStation, "❌ No registrado", "000999", "❌ Documento no registrado.");

            System.out.println("\n✅ Todas las pruebas de consulta ejecutadas.");
        } catch (Exception e) {
            System.err.println("❌ Error durante ejecución del cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runTest(QueryStationPrx station, String testName, String document, String expected) {
        try {
            String result = station.query(document);
            boolean passed = result.trim().equals(expected.trim());
            String status = passed ? "✅ PASÓ" : "❌ FALLÓ";

            System.out.printf("[%s] %s → Resultado: %s | Esperado: %s%n",
                    status, testName, result, expected);
        } catch (Exception e) {
            System.err.printf("❌ Error durante test '%s': %s%n", testName, e.getMessage());
        }
    }
}
