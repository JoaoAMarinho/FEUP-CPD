package communication.unicast;

import communication.MessageReceiver;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReceiverHandler implements Handler{
    private final MessageReceiver receiver;

    public ReceiverHandler(MessageReceiver msgReceiver) {
        this.receiver = msgReceiver;
    }

    @Override
    public CompletionHandler<AsynchronousSocketChannel, Handler> createHandler(AsynchronousServerSocketChannel listener) {
        return new CompletionHandler<>()
        {
            @Override
            public void completed(AsynchronousSocketChannel ch, Handler info)
            {
                var connectionInfo = (ReceiverHandler) info;

                System.out.println("Accepted connection:");
                listener.accept(connectionInfo, this);

                ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                StringBuilder msg = new StringBuilder();

                try {
                    ch.read(byteBuffer).get();
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Exception raised");
                    e.printStackTrace();
                    return;
                }

                while (byteBuffer.position() > 2) {
                    String line = new String(byteBuffer.array(), 0, byteBuffer.position());

                    msg.append(line);
                    byteBuffer.clear();
                    ch.read(byteBuffer);
                }

                byteBuffer.flip();

                System.out.println("Received tcp message: " + msg.toString());
                connectionInfo.receiver.parseMessage(msg.toString(), ch);
            }

            @Override
            public void failed(Throwable exc, Handler att)
            {
                System.out.println("Failed to establish connection.");
            }
        };
    }
}
