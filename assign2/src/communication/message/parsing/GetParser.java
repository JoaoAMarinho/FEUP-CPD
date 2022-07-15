package communication.message.parsing;

import communication.message.GetMessage;
import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Paths;

public class GetParser extends MessageParser{
    private final AsynchronousSocketChannel channel;

    public GetParser(JSONObject msgInfo, Store store, AsynchronousSocketChannel channel) {
        super(msgInfo, store);
        this.channel = channel;
    }

    @Override
    public void run() {
        String key = (String) msgInfo.get("fileName");

        String id = store.getClusterInfo().getResponsibleNode(key);

        var storeId = store.getStoreId();

        String absPath = Paths.get("").toAbsolutePath().toString();

        var replicationFilePath = absPath + store.getStoragePath() + "replication/" + key + ".txt";
        var replicationTombstoneFilePath = absPath + store.getStoragePath() + "replication/" + key + "_tombstone.txt";

        var replicationFile = new File(replicationFilePath);
        var replicationTombstoneFile = new File(replicationTombstoneFilePath);

        if (!id.equals(storeId) && !replicationFile.exists() && !replicationTombstoneFile.exists()) {
            store.getMsgSender().executeUnicastSend(GetMessage.getRedirectMessage(id), channel);
            return;
        }

        String content;
        var filePath = (id.equals(storeId)) ? store.getStoragePath() + key + ".txt" : store.getStoragePath() + "replication/" + key + ".txt";

        try {
            content = FileUtils.ReadFromFile(filePath, "\n");
        } catch (FileNotFoundException e) {
            var file = (id.equals(storeId)) ? new File(absPath + store.getStoragePath() + key + "_tombstone.txt") :
                    replicationTombstoneFile;

            if(file.exists()){
                store.getMsgSender().executeUnicastSend(GetMessage.getFileDeletedMessage(), channel);
                return;
            }

            store.getMsgSender().executeUnicastSend(GetMessage.getFileNotFoundMessage(), channel);
            return;
        }

        store.getMsgSender().executeUnicastSend(GetMessage.getResponseMessage(content), channel);
    }
}
