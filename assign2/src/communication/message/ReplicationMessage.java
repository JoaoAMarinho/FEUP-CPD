package communication.message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReplicationMessage extends Message {
    private List<String> retransmitted;
    public ReplicationMessage(String key, String value) {
        super();
        this.jsonObject.put("header", "REPLICATION")
                .put("key", key)
                .put("value", value);
        this.retransmitted = new ArrayList<>();
    }

    public ReplicationMessage(JSONObject jsonObject) {
        super(jsonObject);
        this.retransmitted = new ArrayList<>();
    }

    public void retransmitted(String id) {
        this.retransmitted.add(id);
    }

    public void retransmitted(List<String> ids) {
        this.retransmitted.addAll(ids);
    }

    public List<String> getRetransmitted() {
        return retransmitted;
    }

    public boolean needsToRetransmit() {
        return this.retransmitted.size() != 2;
    }

    public String getKey() {
        return (String) this.jsonObject.get("key");
    }
}
