package utils;

import communication.message.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UnicastUtils {

    public static void sendUnicastMessage(Message message, InetSocketAddress hostAddress) {
        var msg = message.toString();
        AsynchronousSocketChannel client;
        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            System.out.println("Could not open connection.");
            e.printStackTrace();
            return;
        }

        Future<Void> future = client.connect(hostAddress);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Could not connect to socket '" + hostAddress + "'.");
            return;
        }

        byte[] byteMsg = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);

        client.write(buffer);
        buffer.clear();

        if(client.isOpen()) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to close connection.");
            }
        }
    }

    public static String readWriteUnicastMessage(Message message, InetSocketAddress hostAddress) throws ExecutionException, InterruptedException {
        var msg = message.toString();
        AsynchronousSocketChannel client;

        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            System.out.println("Could not open connection.");
            e.printStackTrace();
            return null;
        }

        Future<Void> future = client.connect(hostAddress);
        future.get();

        byte[] byteMsg = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);

        Future<Integer> writeResult = client.write(buffer);
        writeResult.get();

        buffer.clear();
        client.read(buffer).get();

        StringBuilder result = new StringBuilder();

        while (buffer.position() > 2) {
            String line = new String(buffer.array(), 0, buffer.position());

            result.append(line);
            buffer.clear();
            client.read(buffer);
        }

        if(client.isOpen()) {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Failed to close connection.");
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    public static void sendUnicastMessageToChannel(Message message, AsynchronousSocketChannel channel) {
        var msg = message.toString();

        byte[] byteMsg = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);

        channel.write(buffer);
        buffer.clear();
    }
}
