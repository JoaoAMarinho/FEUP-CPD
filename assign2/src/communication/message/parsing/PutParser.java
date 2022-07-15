package communication.message.parsing;

import communication.message.PutMessage;
import communication.message.ReplicationMessage;
import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;
import java.net.InetSocketAddress;

public class PutParser extends MessageParser{
    public PutParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        String key = (String) msgInfo.get("key");

        String id = store.getClusterInfo().getResponsibleNode(key);

        var storeId = store.getStoreId();

        if (id.equals(storeId)) {
            String content = (String) msgInfo.get("value");
            String filePath = store.getStoragePath() + key + ".txt";

            try {
                FileUtils.CreateFile(filePath, content);
            } catch (IOException e) {
                System.out.println("Could not write to file.");
                e.printStackTrace();
            }

            var replicationMessage = new ReplicationMessage(key, content);
            var toReplicate = this.store.getClusterInfo().replicateMessage(replicationMessage, storeId);
            for (var replicateId : toReplicate) {
                var replicateInfo = replicateId.split(":");
                store.getMsgSender().executeUnicastSend(replicationMessage,
                        new InetSocketAddress(replicateInfo[0], Integer.parseInt(replicateInfo[1])+10));
            }

            return;
        }

        var receiverInfo = id.split(":");
        store.getMsgSender().executeUnicastSend(new PutMessage(msgInfo),
                new InetSocketAddress(receiverInfo[0], Integer.parseInt(receiverInfo[1])+10));
    }
}
