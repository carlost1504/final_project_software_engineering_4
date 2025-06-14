package server;

import com.zeroc.Ice.Current;
import common.LoadBalancer;
import common.QueryStationPrx;
import common.VoteStationPrx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación del balanceador de carga para estaciones de votación y consulta.
 * Asigna estaciones activas usando round-robin.
 */
public class LoadBalancerImpl implements LoadBalancer {

    private final List<VoteStationPrx> voteStations = new ArrayList<>();
    private final List<QueryStationPrx> queryStations = new ArrayList<>();
    private final AtomicInteger voteIndex = new AtomicInteger(0);
    private final AtomicInteger queryIndex = new AtomicInteger(0);

    @Override
    public synchronized void addVoteStation(VoteStationPrx station, Current current) {
        voteStations.add(station);
        System.out.println(" Nueva VoteStation registrada. Total: " + voteStations.size());
    }

    @Override
    public synchronized void addQueryStation(QueryStationPrx station, Current current) {
        queryStations.add(station);
        System.out.println(" Nueva QueryStation registrada. Total: " + queryStations.size());
    }

    @Override
    public VoteStationPrx getVoteStation(Current current) {
        if (voteStations.isEmpty()) {
            System.out.println(" No hay estaciones de voto disponibles.");
            return null;
        }
        int index = voteIndex.getAndUpdate(i -> (i + 1) % voteStations.size());
        return voteStations.get(index);
    }

    @Override
    public QueryStationPrx getQueryStation(Current current) {
        if (queryStations.isEmpty()) {
            System.out.println(" No hay estaciones de consulta disponibles.");
            return null;
        }
        int index = queryIndex.getAndUpdate(i -> (i + 1) % queryStations.size());
        return queryStations.get(index);
    }
}
