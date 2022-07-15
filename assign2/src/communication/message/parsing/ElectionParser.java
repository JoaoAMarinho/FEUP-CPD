package communication.message.parsing;

import communication.election.CoordinatorElection;
import org.json.JSONObject;
import store.Store;

public class ElectionParser extends MessageParser{
    public ElectionParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        if (store.getMsgReceiver().getElectionTimeout() == null) return;

        if (msgInfo.get("id").equals(store.getStoreId()) || store.getMsgReceiver().isElectionOver()) return;

        store.getMsgReceiver().refreshElectionTimeoutService();

        var priority = (int) msgInfo.get("priority");
        var id = (String) msgInfo.get("id");
        if (store.getClusterInfo().hasHigherPriority(priority, id, store.getStoreId())) return;

        CoordinatorElection.setSendCoordinator(false);
    }
}
