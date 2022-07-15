package communication.message;

import org.json.JSONObject;

public class PutMessage extends Message {
    public PutMessage(String key, String value) {
        super();
        this.jsonObject.put("header", "PUT")
                .put("key", key)
                .put("value", value);
    }

    public PutMessage(JSONObject jsonObject) {
        super(jsonObject);
    }
}
