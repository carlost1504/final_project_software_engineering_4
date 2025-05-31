package server;

import common.VoteStation;
import com.zeroc.Ice.Current;

public class VoteStationImpl implements VoteStation {
    @Override
    public boolean vote(String document, int candidateId, Current current) {
        System.out.println("Voto recibido:");
        System.out.println("Documento: " + document);
        System.out.println("Candidato: " + candidateId);
        return true;
    }
}