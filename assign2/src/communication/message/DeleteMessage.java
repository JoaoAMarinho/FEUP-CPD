package communication.message;

import org.json.JSONObject;

public class DeleteMessage extends Message{
    public DeleteMessage(String key) {
        super();
        this.jsonObject.put("header", "DELETE")
                .put("fileName", key);
    }

    public DeleteMessage(JSONObject jsonObject) {
        super(jsonObject);
    }

    public static DeleteMessage createReplicationDelete(String key) {
        var result = new DeleteMessage(key);
        result.jsonObject.put("header", "REPLICATION_DELETE");
        return result;
    }
}
