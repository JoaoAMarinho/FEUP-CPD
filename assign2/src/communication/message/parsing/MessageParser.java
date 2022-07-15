package communication.message.parsing;

import org.json.JSONObject;
import store.Store;

import java.nio.channels.AsynchronousSocketChannel;

public abstract class MessageParser implements Runnable {
    protected final JSONObject msgInfo;
    protected final Store store;

    public MessageParser(JSONObject msgInfo, Store store) {
        this.msgInfo = msgInfo;
        this.store = store;
    }

    public static MessageParser createParser(String message, Store store, AsynchronousSocketChannel channel) {
        var jsonObj = new JSONObject(message);

        return switch (jsonObj.get("header").toString()) {
            case "JOIN" -> new JoinParser(jsonObj, store);
            case "PUT" -> new PutParser(jsonObj, store);
            case "GET" -> new GetParser(jsonObj, store, channel);
            case "DELETE" -> new DeleteParser(jsonObj, store);
            case "REPLICATION_DELETE" -> new ReplicationDeleteParser(jsonObj, store);
            case "FILETRANSFER" -> new FileTranferParser(jsonObj, store);
            case "LEAVE" -> new LeaveParser(jsonObj, store);
            case "MEMBERSHIP" -> new MembershipParser(jsonObj, store);
            case "ELECTION" -> new ElectionParser(jsonObj, store);
            case "COORDINATOR" -> new CoordinatorParser(jsonObj, store);
            case "REPLICATION" -> new ReplicationParser(jsonObj, store);
            default -> throw new IllegalStateException("Unexpected value: " + jsonObj.get("header").toString());
        };
    }
}
