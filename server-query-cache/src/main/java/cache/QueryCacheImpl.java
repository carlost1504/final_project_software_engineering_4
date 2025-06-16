package cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zeroc.Ice.Current;
import common.QueryCache;
import common.QueryStationPrx;
import java.util.concurrent.TimeUnit;

public class QueryCacheImpl implements QueryCache {
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    private final QueryStationPrx realQueryStation;

    public QueryCacheImpl(QueryStationPrx realQueryStation) {
        this.realQueryStation = realQueryStation;
    }

    @Override
    public String query(String document, Current current) {
        System.out.print("Consulta para doc: " + document + "... ");
        String result = cache.get(document, key -> {
            System.out.println("CACHE MISS. Contactando QueryStation real...");
            return realQueryStation.query(key);
        });
        // Esta comprobación es para mostrar el HIT en la consola de forma más fiable
        if (cache.asMap().containsKey(document)) {
            System.out.println("CACHE HIT.");
        }
        return result;
    }

    @Override
    public void invalidate(String document, Current current) {
        System.out.println("CACHE INVALIDATE: Se invalida la entrada para " + document);
        cache.invalidate(document);
        // Opcionalmente, se puede añadir directamente el resultado final
        cache.put(document, "❌ Ya ha votado.");
    }
}