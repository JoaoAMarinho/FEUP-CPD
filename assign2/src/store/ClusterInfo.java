package store;

import communication.message.Message;
import communication.message.ReplicationMessage;
import org.json.JSONObject;
import utils.HashUtils;

import java.util.*;

public class ClusterInfo {
    private HashMap<String, JSONObject> clusterMap;
    private Queue<String> clusterEvents;
    private List<String> nodeHashes;
    private List<ReplicationMessage> messagesToRetransmit;
    String coordinator;
    int coordinatorPriority;

    private final int MAX_SIZE = 32;

    public ClusterInfo(String id, int memberShipCounter) {
        this.clusterMap = new HashMap<>();
        this.clusterEvents = new LinkedList<>();
        this.nodeHashes = new ArrayList<>();
        this.coordinator = null;
        this.coordinatorPriority = 0;
        this.messagesToRetransmit = new ArrayList<>();

        var key = HashUtils.generateHash(id);
        this.nodeHashes.add(key);
        addToMap(key, id, memberShipCounter);
    }

    public void parseFile(String fileData) {
        Scanner scanner = new Scanner(fileData);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) continue;

            var storeInfo = line.split("-");

            var id = storeInfo[0];
            var key = HashUtils.generateHash(id);
            addToMap(key, id, Integer.parseInt(storeInfo[1]));
        }
        scanner.close();
    }

    public synchronized void addEvent(String id, int memberShipCounter) {
        var key = HashUtils.generateHash(id);
        var info = this.clusterMap.get(key);

        if (memberShipCounter % 2 != 0) addToHashes(key);

        if (info == null) {
            addToQueue(key);
            addToMap(key, id, memberShipCounter);
            return;
        }

        if (memberShipCounter <= (int) info.get("Counter")) return;

        if ((boolean) info.get("InEvents")) removeEvent(key);

        addToQueue(key);
        addToMap(key, id, memberShipCounter);
    }

    private void removeEvent(String key) {
        this.clusterEvents.remove(key);
    }

    private void addToQueue(String key) {
        this.clusterEvents.add(key);

        if (this.clusterEvents.size() > MAX_SIZE) this.clusterEvents.remove();
    }

    private void addToMap(String key, String id, int membershipCounter) {
        var value = new JSONObject();
        value.put("Id", id);
        value.put("Counter", membershipCounter);
        value.put("InEvents", true);

        this.clusterMap.put(key, value);
    }

    private void addToHashes(String key) {
        int index = Collections.binarySearch(nodeHashes, key);
        if (index >= 0) return;

        index = -index - 1;
        nodeHashes.add(index, key);
    }

    public void removeNode(String id) {
        var key = HashUtils.generateHash(id);
        nodeHashes.remove(key);
    }

    public String getResponsibleNode(String hash) {
        int index = Collections.binarySearch(nodeHashes, hash);
        if (index < 0) {
            index = (-index-1) == nodeHashes.size() ? 0 : (-index-1);
        }

        var nodeHash = nodeHashes.get(index);
        return (String) clusterMap.get(nodeHash).get("Id");
    }

    public synchronized String getLogs() {
        var logs = new StringBuilder();

        for (String key : clusterEvents) {
            var info = clusterMap.get(key);
            logs.append(info.get("Id")).append("-")
                .append(info.get("Counter"))
                .append("\n");
        }
        return logs.toString();
    }

    public boolean checkIfPrevious(String store, String newStore) {
        var afterKey = HashUtils.generateHash(store);
        var previousKey = HashUtils.generateHash(newStore);

        int afterIndex = Collections.binarySearch(nodeHashes, afterKey);
        int previousIndex = Collections.binarySearch(nodeHashes, previousKey);

        return (afterIndex-previousIndex == 1) || (previousIndex==nodeHashes.size()-1 && afterIndex==0);
    }

    public String getNextStore(String id) {
        if (nodeHashes.size() < 2) return null;

        var key = HashUtils.generateHash(id);
        int index = Collections.binarySearch(nodeHashes, key);
        index = (index == nodeHashes.size()-1) ? 0 : index + 1;

        return (String) clusterMap.get(nodeHashes.get(index)).get("Id");
    }

    public int getPriority() {
        return nodeHashes.size();
    }

    public boolean hasHigherPriority(int priority, String otherId, String storeId) {
        if (getPriority() > priority) return true;
        return (getPriority() == priority && storeId.compareTo(otherId) > 0);
    }

    public synchronized void updateCoordinator(String newCoordinator, int priority) {
        coordinator = newCoordinator;
        coordinatorPriority = priority;
    }

    public String getCoordinator() {
        return coordinator;
    }

    public List<String> replicateMessage(ReplicationMessage replicationMessage, String storeId) {
        int replicationPossibilities = Math.min(this.nodeHashes.size() - 1, 2);

        var toRetransmit = getReplicationPossibilities(replicationPossibilities, storeId);

        replicationMessage.retransmitted(toRetransmit);
        synchronized (this) {
            if (replicationMessage.needsToRetransmit()) this.messagesToRetransmit.add(replicationMessage);
        }
        return toRetransmit;
    }

    public List<ReplicationMessage> getMessagesToRetransmit() {
        return messagesToRetransmit;
    }

    public void setMessagesToRetransmit(List<ReplicationMessage> messagesToRetransmit) {
        this.messagesToRetransmit = messagesToRetransmit;
    }

    private List<String> getReplicationPossibilities(int replicationPossibilities, String hash) {
        if (replicationPossibilities == 0) return Collections.emptyList();

        int index = Collections.binarySearch(nodeHashes, hash);
        if (index < 0) {
            index = (-index-1) == nodeHashes.size() ? 0 : (-index-1);
        }

        var replicationHash1 = this.nodeHashes.get(index == (this.nodeHashes.size() - 1) ? 0 : index + 1);

        if (replicationPossibilities == 1) {
            return Collections.singletonList((String) this.clusterMap.get(replicationHash1).get("Id"));
        }

        var replicationHash2 = this.nodeHashes.get(index == 0 ? this.nodeHashes.size() - 1 : index - 1);

        return Arrays.asList(
                    (String) this.clusterMap.get(replicationHash1).get("Id"),
                    (String) this.clusterMap.get(replicationHash2).get("Id")
                );
    }

    public boolean lastEvent(String storeId, int counter) {
        var key = HashUtils.generateHash(storeId);

        var hashMapInfo = clusterMap.get(key);

        if (hashMapInfo == null) return false;

        return (int) hashMapInfo.get("Counter") == counter;
    }

    public int getPriorityDifference() {
        return Math.abs(getPriority()-coordinatorPriority);
    }

    public synchronized void removeReplicateMsg(String key) {
        var message = messagesToRetransmit.stream()
                .filter(msg -> msg.getKey().equals(key)).findAny().orElse(null);

        if(message==null) return;

        messagesToRetransmit.remove(message);
    }
}
