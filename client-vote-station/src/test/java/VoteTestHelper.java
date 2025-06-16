import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Util;
import utils.HmacUtil;
import utils.SecurityConfig;

public class VoteTestHelper {
    public static boolean sendVote(String doc, int candidateId, int stationId) {
        try (Communicator communicator = Util.initialize()) {
            VoteStationPrx proxy = VoteStationPrx.checkedCast(
                    communicator.stringToProxy("VoteStation:default -p 10000")
            );
            String hmac = HmacUtil.generateHmac(doc + candidateId + stationId, SecurityConfig.HMAC_SECRET);
            return proxy.vote(doc, candidateId, stationId, hmac);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar voto: " + e.getMessage());
            return false;
        }
    }
}
