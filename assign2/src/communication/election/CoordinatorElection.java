package communication.election;

import communication.message.ElectionMessage;
import communication.message.MembershipMessage;
import store.Store;

public class CoordinatorElection {
    private static boolean SendCoordinator = false;

    public static Runnable startElection(Store store) {
        return () -> {
            System.out.println("Started election algorithm.");
            setSendCoordinator(true);

            store.getMsgReceiver().setElectionTimeoutService(CoordinatorElection.startCoordinatorSend(store));

            var msg = ElectionMessage.createElection(store.getStoreId(),
                    store.getClusterInfo().getPriority());
            store.getMsgSender().executeMulticastSend(msg);

        };
    }

    public static Runnable startCoordinatorSend(Store store) {
        return () -> {
            if (!SendCoordinator) return;

            var msg = ElectionMessage.createCoordinator(store.getStoreId(),
                    store.getClusterInfo().getPriority());
            store.getMsgSender().executeMulticastSend(msg);

            store.getClusterInfo().updateCoordinator(store.getStoreId(), store.getClusterInfo().getPriority());
            store.getMsgReceiver().setCoordinatorTimeout(CoordinatorElection.startMembershipSend(store));
        };
    }

    public static Runnable startMembershipSend(Store store) {
        return () -> {
            store.getMsgReceiver().setMembershipSender(CoordinatorElection.sendMembershipMsg(store));
        };
    }

    public static Runnable sendMembershipMsg(Store store) {
        return () -> {
            if (!store.getClusterInfo().getCoordinator().equals(store.getStoreId()))
                store.getMsgReceiver().cancelMembershipSenderService();

            var msg = new MembershipMessage(store.getStoreId(), store.getClusterInfo().getLogs());
            store.getMsgSender().executeMulticastSend(msg);
        };
    }

    public synchronized static void setSendCoordinator(boolean sendCoordinator) {
        SendCoordinator = sendCoordinator;
    }

    public static boolean isSendCoordinator() {
        return SendCoordinator;
    }
}
