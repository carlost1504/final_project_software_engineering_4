package cache;

import com.zeroc.Ice.*;
import common.QueryStationPrx;

public class QueryCacheServer {
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando Servidor de Caché (QueryCacheServer)...");

        try (Communicator communicator = Util.initialize(args, "config.cache")) {

            ObjectPrx realQueryStationProxyBase = communicator.propertyToProxy("RealQueryStation.Proxy");
            QueryStationPrx realQueryStationProxy = QueryStationPrx.checkedCast(realQueryStationProxyBase);

            if (realQueryStationProxy == null) {
                throw new Error("No se pudo obtener el proxy al QueryStation real desde la propiedad 'RealQueryStation.Proxy'");
            }
            System.out.println("✅ Conectado al QueryStation real.");

            ObjectAdapter adapter = communicator.createObjectAdapter("QueryCacheAdapter");

            QueryCacheImpl cacheImpl = new QueryCacheImpl(realQueryStationProxy);

            adapter.add(cacheImpl, Util.stringToIdentity("QueryCache"));
            adapter.activate();

            System.out.println("✅ Servidor de Caché listo y escuchando...");
            communicator.waitForShutdown();

        } catch (java.lang.Exception e) { // <-- ¡LA CORRECCIÓN ESTÁ AQUÍ! Se especifica 'java.lang.Exception'
            System.err.println("❌ Error en el Servidor de Caché: " + e.getMessage());
            e.printStackTrace();
        }
    }
}