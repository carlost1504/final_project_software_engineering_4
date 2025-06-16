package server.impl;

import com.zeroc.Ice.Current;
import common.LoadBalancer;
import common.QueryStationPrx;
import common.VoteStationPrx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación del balanceador de carga.
 * Para las VoteStation, usa round-robin.
 * Para las QueryStation, redirige a un servidor de caché centralizado.
 */
public class LoadBalancerImpl implements LoadBalancer {

    // --- Esta parte se mantiene para el balanceo de las estaciones de voto ---
    private final List<VoteStationPrx> voteStations = new ArrayList<>();
    private final AtomicInteger voteIndex = new AtomicInteger(0);

    // --- Esta parte se mantiene por compatibilidad, pero la lógica la ignorará si la caché está activa ---
    private final List<QueryStationPrx> queryStations = new ArrayList<>();
    private final AtomicInteger queryIndex = new AtomicInteger(0);

    // "Interruptor" para activar o desactivar la redirección a la caché.
    // Déjalo en 'true' para usar el Proxy Cache.
    private final boolean useCacheForQueries = true;

    @Override
    public synchronized void addVoteStation(VoteStationPrx station, Current current) {
        voteStations.add(station);
        System.out.println(" Nueva VoteStation registrada. Total: " + voteStations.size());
    }

    @Override
    public synchronized void addQueryStation(QueryStationPrx station, Current current) {
        queryStations.add(station);
        System.out.println("  Nueva QueryStation registrada. Total: " + queryStations.size() + ". (AVISO: Será ignorada si useCacheForQueries es true)");
    }

    @Override
    public VoteStationPrx getVoteStation(Current current) {
        // La lógica para las estaciones de voto no cambia.
        if (voteStations.isEmpty()) {
            System.err.println(" No hay estaciones de voto disponibles.");
            return null;
        }
        int index = voteIndex.getAndUpdate(i -> (i + 1) % voteStations.size());
        return voteStations.get(index);
    }

    @Override
    public QueryStationPrx getQueryStation(Current current) {
        // Usamos el interruptor para decidir qué hacer
        if (useCacheForQueries) {
            // LÓGICA NUEVA: Redirigir siempre a la caché
            System.out.println("  Petición de QueryStation recibida. Redirigiendo a QueryCacheServer...");

            // Obtenemos el comunicador de ICE para poder crear un conector (proxy)
            com.zeroc.Ice.Communicator communicator = current.adapter.getCommunicator();

            // Creamos el conector que apunta a la dirección del servidor de caché (puerto 14000)
            com.zeroc.Ice.ObjectPrx proxyBase = communicator.stringToProxy("QueryCache:default -p 14000");

            // Devolvemos el conector. El cliente lo usará sin saber que es una caché
            return QueryStationPrx.uncheckedCast(proxyBase);

        } else {
            // LÓGICA ORIGINAL: Si el interruptor está en false, funciona como antes
            System.out.println(" Modo sin caché: Balanceando carga a QueryStation real.");
            if (queryStations.isEmpty()) {
                System.err.println(" No hay estaciones de consulta disponibles.");
                return null;
            }
            int index = queryIndex.getAndUpdate(i -> (i + 1) % queryStations.size());
            return queryStations.get(index);
        }
    }
}