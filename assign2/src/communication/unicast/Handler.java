package communication.unicast;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public interface Handler {
    CompletionHandler<AsynchronousSocketChannel, Handler> createHandler(AsynchronousServerSocketChannel listener);
}
