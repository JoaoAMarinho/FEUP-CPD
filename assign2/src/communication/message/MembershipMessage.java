package communication.message;

public class MembershipMessage extends Message{
    public MembershipMessage(String storeId, String eventLogs) {
        super();
        this.jsonObject.put("header", "MEMBERSHIP")
                .put("id", storeId)
                .put("logs", eventLogs);
    }
}
