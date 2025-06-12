package client.vote;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;
import common.VoteStationPrx;
import utils.HmacUtil;
import utils.SecurityConfig;

import java.util.Scanner;

public class VoteClient {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectPrx base = communicator.stringToProxy("VoteStation:default -p 10000");
            VoteStationPrx proxy = VoteStationPrx.checkedCast(base);

            if (proxy == null) {
                throw new Error("Proxy inválido.");
            }

            Scanner sc = new Scanner(System.in);
            System.out.println("=== 🗳️ Cliente de Votación ===");
            System.out.print("Documento: ");
            String document = sc.nextLine();
            System.out.print("ID Candidato: ");
            int candidateId = Integer.parseInt(sc.nextLine());
            System.out.print("ID Estación: ");
            int stationId = Integer.parseInt(sc.nextLine());

            String hmac = HmacUtil.generateHmac(document + candidateId + stationId, SecurityConfig.HMAC_SECRET);
            boolean result = proxy.vote(document, candidateId, stationId, hmac);

            System.out.println("Resultado: " + (result ? "✅ Voto exitoso" : "❌ Voto rechazado"));

        } catch (Exception e) {
            System.err.println("Error en cliente de votación: " + e.getMessage());
        }
    }
}
