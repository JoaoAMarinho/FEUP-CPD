package communication.message;

public class LeaveMessage extends Message{
    public LeaveMessage(String storeId, int membershipCounter) {
        super();
        this.jsonObject.put("header", "LEAVE")
                .put("id", storeId)
                .put("counter", membershipCounter);
    }
}
