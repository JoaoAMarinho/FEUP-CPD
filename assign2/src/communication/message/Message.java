package communication.message;

import org.json.JSONObject;

public abstract class Message {
    protected final JSONObject jsonObject;

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public Message() {
        this.jsonObject = new JSONObject();
    }

    public Message(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
