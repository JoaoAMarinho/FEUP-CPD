package communication.message;

public class ElectionMessage extends Message{
    public ElectionMessage(String header, String storeId, int priority) {
        super();
        this.jsonObject.put("header", header)
                        .put("id", storeId)
                        .put("priority", priority);
    }

    public static ElectionMessage createElection(String storeId, int priority) {
        return new ElectionMessage("ELECTION", storeId, priority);
    }

    public static ElectionMessage createCoordinator(String storeId, int priority) {
        return new ElectionMessage("COORDINATOR", storeId, priority);
    }
}
