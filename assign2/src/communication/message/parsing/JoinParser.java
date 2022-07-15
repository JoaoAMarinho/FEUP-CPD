package communication.message.parsing;

import communication.message.FileTransferMessage;
import communication.message.MembershipMessage;
import communication.message.ReplicationMessage;
import org.json.JSONObject;
import store.Store;
import utils.FileUtils;
import utils.HashUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class JoinParser extends MessageParser {

    public JoinParser(JSONObject message, Store store) {
        super(message, store);
    }

    @Override
    public void run() {
        String storeId = (String) msgInfo.get("id");
        int counter = Integer.parseInt((String) msgInfo.get("counter"));
        int port = Integer.parseInt((String) msgInfo.get("port"));

        System.out.println("Parsing join message: id-" + storeId + ", counter-" + counter);

        if (!storeId.equals(store.getStoreId()) && !store.getClusterInfo().lastEvent(storeId, counter)) {
            var priorityDiff = store.getClusterInfo().getPriorityDifference();
            if (priorityDiff < 3) {
                if (!storeId.equals(store.getClusterInfo().getCoordinator())) {
                    try {
                        Thread.sleep(200+100*priorityDiff);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                var msg = new MembershipMessage(store.getStoreId(), store.getClusterInfo().getLogs());
                store.getMsgSender().executeUnicastSend(msg, new InetSocketAddress(storeId.split(":")[0], port));
            }
        }

        var clusterInfo = store.getClusterInfo();
        clusterInfo.addEvent(storeId, counter);

        try {
            FileUtils.UpdateFile(store.getLogsPath(), clusterInfo.getLogs());
        } catch (IOException e) {
            System.out.println("Error writing to logs file.");
            e.printStackTrace();
        }

        if (storeId.equals(store.getStoreId()) || !clusterInfo.checkIfPrevious(store.getStoreId(), storeId)) {
            replicateMessages(storeId, port-1);
            return;
        }

        List<String> files = new ArrayList<>();
        String storagePath = store.getStoragePath();
        try {
            files = FileUtils.GetFolderSortedFiles(storagePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not read files from '"+ storagePath + "'.");
        }

        var hash = HashUtils.generateHash(storeId);
        var storeInfo = storeId.split(":");
        for (var file : files) {
            if (file == null) continue;

            if(file.matches("(.*)_tombstone")) continue;
            if (!store.getClusterInfo().getResponsibleNode(file).equals(storeId)) break;

            var path = storagePath + file + ".txt";
            String content;

            try {
                content = FileUtils.ReadFromFile(path, "\n");
                if (content.equals("")) {
                    System.out.println("Empty file.");
                    return;
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found.");
                e.printStackTrace();
                return;
            }

            var msg = new FileTransferMessage(file, content);
            store.getMsgSender().executeUnicastSend(msg,
                    new InetSocketAddress(storeInfo[0], port-1));

            var replicationPath = storagePath + "replication/" + file + ".txt";

            try {
                FileUtils.ChangeFileName(path, replicationPath);
            } catch (IOException e) {
                System.out.println("Could not rename file.");
            }
        }

        replicateMessages(storeId, port-1);
    }

    private synchronized void replicateMessages(String storeId, int port) {
        var replicationMessages = this.store.getClusterInfo().getMessagesToRetransmit();
        var newMessagesToRetransmit = new ArrayList<ReplicationMessage>();
        for (var message : replicationMessages) {
            if (store.getClusterInfo().getResponsibleNode(message.getKey()).equals(storeId)) {
                message.retransmitted(storeId);
            }
            if (!message.getRetransmitted().contains(storeId)) {
                store.getMsgSender().executeUnicastSend(message,
                        new InetSocketAddress(storeId.split(":")[0], port));
                message.retransmitted(storeId);
            }
            if (!message.needsToRetransmit()) continue;
            newMessagesToRetransmit.add(message);
        }
        this.store.getClusterInfo().setMessagesToRetransmit(newMessagesToRetransmit);
    }
}
