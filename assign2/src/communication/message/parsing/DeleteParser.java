package communication.message.parsing;

import communication.message.DeleteMessage;
import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.NoSuchFileException;

public class DeleteParser extends MessageParser{
    public DeleteParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        String key = (String) msgInfo.get("fileName");

        String id = store.getClusterInfo().getResponsibleNode(key);

        var storeId = store.getStoreId();

        if (id.equals(storeId)) {
            var filePath = store.getStoragePath() + key + ".txt";
            var newFilePath = store.getStoragePath() + key + "_tombstone" + ".txt";

            this.store.getClusterInfo().removeReplicateMsg(key);

            try {
                FileUtils.ChangeFileName(filePath, newFilePath);
            } catch (NoSuchFileException e) {
                System.out.println("No file to be deleted.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not rename file.");
                return;
            }

            var msg = DeleteMessage.createReplicationDelete(key);
            store.getMsgSender().executeMulticastSend(msg);
            return;
        }

        var receiverInfo = id.split(":");
        store.getMsgSender().executeUnicastSend(new DeleteMessage(msgInfo),
                new InetSocketAddress(receiverInfo[0], Integer.parseInt(receiverInfo[1])+10));
    }
}
