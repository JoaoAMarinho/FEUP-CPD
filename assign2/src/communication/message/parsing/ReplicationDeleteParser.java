package communication.message.parsing;

import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;

public class ReplicationDeleteParser extends MessageParser{
    public ReplicationDeleteParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        String key = (String) msgInfo.get("fileName");

        var filePath = store.getStoragePath() + key + ".txt";
        var newFilePath = store.getStoragePath() + key + "_tombstone" + ".txt";

        try {
            FileUtils.ChangeFileName(filePath, newFilePath);
        } catch (IOException ignored) {
        }

        filePath = store.getStoragePath() + "replication/" + key + ".txt";
        newFilePath = store.getStoragePath() + "replication/" + key + "_tombstone" + ".txt";

        try {
            FileUtils.ChangeFileName(filePath, newFilePath);
        } catch (IOException ignored) {
        }
    }
}
