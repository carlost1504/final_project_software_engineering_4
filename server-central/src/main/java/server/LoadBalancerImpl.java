// Ruta: server-central/src/main/java/server/LoadBalancerImpl.java
package server;

import com.zeroc.Ice.Current;
import common.LoadBalancer;
import common.QueryStationPrx;
import common.VoteStationPrx;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancerImpl implements LoadBalancer {

    // Usamos listas thread-safe. Son más eficientes que sincronizar todo el método.
    private final List<VoteStationPrx> voteStations = new CopyOnWriteArrayList<>();
    private final List<QueryStationPrx> queryStations = new CopyOnWriteArrayList<>();

    // AtomicInteger es thread-safe para contadores.
    private final AtomicInteger voteCounter = new AtomicInteger(0);
    private final AtomicInteger queryCounter = new AtomicInteger(0);

    @Override
    public void addVoteStation(VoteStationPrx station, Current current) {
        System.out.println("➕ [Broker] Registrando nueva Mesa de Votación. Total: " + (voteStations.size() + 1));
        voteStations.add(station);
    }

    @Override
    public void addQueryStation(QueryStationPrx station, Current current) {
        System.out.println("➕ [Broker] Registrando nueva Estación de Consulta. Total: " + (queryStations.size() + 1));
        queryStations.add(station);
    }

    @Override
    public VoteStationPrx getVoteStation(Current current) {
        if (voteStations.isEmpty()) {
            System.err.println("⚠️  [Broker] Petición de Mesa de Votación, pero no hay ninguna registrada.");
            return null;
        }
        int index = voteCounter.getAndIncrement() % voteStations.size();
        return voteStations.get(index);
    }

    @Override
    public QueryStationPrx getQueryStation(Current current) {
        if (queryStations.isEmpty()) {
            System.err.println("⚠️  [Broker] Petición de Estación de Consulta, pero no hay ninguna registrada.");
            return null;
        }
        int index = queryCounter.getAndIncrement() % queryStations.size();
        return queryStations.get(index);
    }
}
