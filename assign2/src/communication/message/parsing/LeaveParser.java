package communication.message.parsing;

import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;

public class LeaveParser extends MessageParser{
    public LeaveParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        var storeId = (String) msgInfo.get("id");
        var membershipCounter = (int) msgInfo.get("counter");

        var clusterInfo = store.getClusterInfo();

        clusterInfo.addEvent(storeId, membershipCounter);
        try {
            FileUtils.UpdateFile(store.getLogsPath(), clusterInfo.getLogs());
        } catch (IOException e) {
            System.out.println("Error writing to logs file.");
            e.printStackTrace();
        }

        clusterInfo.removeNode(storeId);
    }
}
