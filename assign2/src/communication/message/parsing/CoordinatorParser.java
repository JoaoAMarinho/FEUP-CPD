package communication.message.parsing;

import communication.election.CoordinatorElection;
import communication.message.ElectionMessage;
import org.json.JSONObject;
import store.Store;

import java.util.concurrent.TimeUnit;

public class CoordinatorParser extends MessageParser{
    public CoordinatorParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        if (msgInfo.get("id").equals(store.getStoreId())) return;

        var receiver = store.getMsgReceiver();

        if (receiver.getElectionTimeout() != null) {
            while (!receiver.getElectionTimeout().isDone()){
                if (!receiver.isElectionOver()) {
                    try {
                        Thread.sleep(receiver.getElectionTimeout().getDelay(TimeUnit.MILLISECONDS));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        var priority = (int) msgInfo.get("priority");
        var id = (String) msgInfo.get("id");

        if (CoordinatorElection.isSendCoordinator()) {
            if (store.getClusterInfo().hasHigherPriority(priority, id, store.getStoreId())) {
                var msg = ElectionMessage.createCoordinator(store.getStoreId(),
                        store.getClusterInfo().getPriority());
                store.getMsgSender().executeMulticastSend(msg);
                receiver.refreshCoordinatorTimeout();
                return;
            }
            CoordinatorElection.setSendCoordinator(false);
            receiver.cancelCoordinatorTimeoutService();
        }

        store.getClusterInfo().updateCoordinator(id, priority);
    }
}
