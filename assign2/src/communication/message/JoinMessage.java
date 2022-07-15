package communication.message;

public class JoinMessage extends Message {
    public JoinMessage(String storeId, String membershipCounter, String port) {
        super();
        this.jsonObject.put("header", "JOIN")
                .put("id", storeId)
                .put("counter", membershipCounter)
                .put("port", port);
    }
}
