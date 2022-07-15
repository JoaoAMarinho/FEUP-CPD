package communication.unicast;

import communication.message.JoinMessage;
import store.Store;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Date;
import java.util.List;

public class Unicast {
    private final Store store;
    private AsynchronousServerSocketChannel listener;

    public Unicast(Store store) {
        this.store = store;
    }

    public AsynchronousServerSocketChannel getChannel() {
        return listener;
    }

    public static AsynchronousServerSocketChannel createChannel(InetAddress ip, int port) {
        try {
            return AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(ip, port));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void closeChannel(AsynchronousServerSocketChannel channel) {
        try {
            // Close the connection if we need to
            if (channel.isOpen()) {
                channel.close();
                System.out.println("Closed connections.");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public List<String> initialConnectionHandler(int port) {
        var handler = new InitialHandler();
        var storeAddress = store.getStoreAddress();

        AsynchronousServerSocketChannel listener = createChannel(storeAddress, port);
        listener.accept(handler, handler.createHandler(listener));

        var msg = new JoinMessage(store.getStoreId(),
                String.valueOf(store.getMembershipCounter()), String.valueOf(port));

        store.getMsgSender().executeMulticastSend(msg);

        int tries = 1;
        long timeStart = new Date().getTime();

        do {
            var timeNow = new Date().getTime();

            if ((timeNow - timeStart) > 3000) {
                System.out.println("Retransmit multicast msg.");
                store.getMsgSender().executeMulticastSend(msg);

                timeStart = new Date().getTime();
                tries+=1;
            }

            try { //Wait for half a second
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (tries < 3 && handler.getConnectionsCounter() != 3);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeChannel(listener);
        return handler.getMessages();
    }

    public void startConnections(int port) {
        var handler = new ReceiverHandler(store.getMsgReceiver());
        var storeAddress = store.getStoreAddress();

        this.listener = createChannel(storeAddress, port);

        System.out.println("Started listening for tcp connections.");
        this.listener.accept(handler, handler.createHandler(listener));

    }
}
