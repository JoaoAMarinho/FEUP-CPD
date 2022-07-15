package communication.unicast;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class InitialHandler implements Handler{
    private int connectionsCounter;
    private List<String> messages;

    InitialHandler() {
        this.connectionsCounter = 0;
        this.messages = new ArrayList<>();
    }

    private void addMsg(String msg) {
        messages.add(msg);
    }

    private void incrementCounter() {
        this.connectionsCounter++;
    }

    public int getConnectionsCounter() {
        return connectionsCounter;
    }

    public List<String> getMessages() {
        return messages;
    }

    @Override
    public CompletionHandler<AsynchronousSocketChannel, Handler> createHandler(AsynchronousServerSocketChannel listener) {
        return new CompletionHandler<>()
        {
            @Override
            public void completed(AsynchronousSocketChannel ch, Handler info)
            {
                var connectionInfo = (InitialHandler) info;

                System.out.println("Connected");
                if (connectionInfo.connectionsCounter > 2) {
                    System.out.println("No more connections");
                    return;
                }

                connectionInfo.incrementCounter();
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

                connectionInfo.addMsg(msg.toString());
            }

            @Override
            public void failed(Throwable exc, Handler att)
            {
                System.out.println("Failed to establish connection.");
            }
        };
    }

}
