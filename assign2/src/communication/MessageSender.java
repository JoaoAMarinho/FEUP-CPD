package communication;

import communication.message.Message;
import communication.message.transmition.MulticastSender;
import communication.message.transmition.UnicastSender;
import store.Store;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MessageSender {
    private final Store store;
    private final ThreadPoolExecutor service;

    public MessageSender(Store store) {
        this.store = store;
        this.service =  (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public ThreadPoolExecutor getService() {
        return service;
    }

    public void executeMulticastSend(Message message) {
        var sender = new MulticastSender(message, store);
        service.execute(sender);
    }

    public void executeUnicastSend(Message message, InetSocketAddress receiver) {
        var sender = new UnicastSender(message, receiver);
        service.execute(sender);
    }

    public void executeUnicastSend(Message message, AsynchronousSocketChannel channel) {
        var sender = new UnicastSender(message, channel);
        service.execute(sender);
    }
}
