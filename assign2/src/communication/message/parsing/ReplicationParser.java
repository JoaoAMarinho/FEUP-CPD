package communication.message.parsing;

import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;

public class ReplicationParser extends MessageParser {
    public ReplicationParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        var filePath = store.getStoragePath() + "replication/" + msgInfo.get("key") + ".txt";
        try {
            FileUtils.CreateFile(filePath, (String) msgInfo.get("value"));
        } catch (IOException e) {
            System.out.println("Could not write to file.");
            e.printStackTrace();
        }
    }
}
