import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import utils.HmacUtil;
import utils.SecurityConfig;

public class VoteClientTestManual {
    public static void main(String[] args) {
        System.out.println("🔍 Iniciando pruebas de votación contra el servidor...");

        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx voteStation = VoteStationPrx.checkedCast(base);

            if (voteStation == null) {
                throw new Error("Proxy inválido: no se pudo castear a VoteStation.");
            }

            runTest(voteStation, "✅ Voto válido", "112233", 101, 1, true);
            runTest(voteStation, "❌ Mesa incorrecta", "999000", 101, 1, false);
            runTest(voteStation, "❌ Ya ha votado", "445566", 102, 1, false);
            runTest(voteStation, "❌ No habilitado", "000111", 101, 1, false);
            runTest(voteStation, "❌ Documento no registrado", "123123", 101, 1, false);

            System.out.println("\n✅ Todas las pruebas ejecutadas.");

        } catch (Exception e) {
            System.err.println("❌ Error en cliente de prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runTest(VoteStationPrx proxy, String testName, String doc, int candidateId, int stationId, boolean expected) {
        try {
            String data = doc + candidateId + stationId;
            String hmac = HmacUtil.generateHmac(data, SecurityConfig.HMAC_SECRET);

            boolean result = proxy.vote(doc, candidateId, stationId, hmac);
            String outcome = result == expected ? "✅ PASÓ" : "❌ FALLÓ";

            System.out.printf("[%s] %s → Resultado: %s | Esperado: %s%n",
                    outcome, testName, result ? "✅" : "❌", expected ? "✅" : "❌");

        } catch (Exception e) {
            System.err.println("❌ Error durante test '" + testName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
}
