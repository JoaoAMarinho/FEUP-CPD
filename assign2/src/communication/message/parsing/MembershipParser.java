package communication.message.parsing;

import org.json.JSONObject;
import store.Store;
import utils.FileUtils;

import java.io.IOException;

public class MembershipParser extends MessageParser{
    public MembershipParser(JSONObject msgInfo, Store store) {
        super(msgInfo, store);
    }

    @Override
    public void run() {
        store.getMsgReceiver().refreshMembershipTimeoutService();

        if (msgInfo.get("id").equals(store.getStoreId())) return;

        String[] logs = ((String) msgInfo.get("logs")).split("\n");
        var clusterInfo = store.getClusterInfo();


        for (var str : logs) {
            if (str.isEmpty()) continue;

            var logInfo = str.split("-");
            clusterInfo.addEvent(logInfo[0], Integer.parseInt(logInfo[1]));
        }

        try {
            FileUtils.UpdateFile(store.getLogsPath(), clusterInfo.getLogs());
        } catch (IOException e) {
            System.out.println("Error writing to logs file.");
            e.printStackTrace();
        }

    }
}
