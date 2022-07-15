package communication.message.transmition;

import communication.message.Message;
import utils.UnicastUtils;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

public class UnicastSender implements Runnable{
    private final Message message;
    private final InetSocketAddress receiverAddress;
    private final AsynchronousSocketChannel channel;

    public UnicastSender(Message message, InetSocketAddress receiverAddress) {
        this.message = message;
        this.receiverAddress = receiverAddress;
        this.channel = null;
    }

    public UnicastSender(Message message, AsynchronousSocketChannel channel) {
        this.message = message;
        this.channel = channel;
        this.receiverAddress = null;
    }

    @Override
    public void run() {
        if (channel == null) {
            System.out.println("Sending unicast message to IP-" + receiverAddress.getHostName() + " Port-" + receiverAddress.getPort());
            UnicastUtils.sendUnicastMessage(message, receiverAddress);
            return;
        }

        UnicastUtils.sendUnicastMessageToChannel(message, channel);
    }
}
