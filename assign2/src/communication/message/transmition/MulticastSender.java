package communication.message.transmition;

import communication.message.Message;
import store.Store;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastSender implements Runnable{
    private final DatagramSocket datagramSocket;
    private final InetAddress group;
    private final int port;
    private final String message;

    public MulticastSender(Message message, Store store) {
        this.datagramSocket = store.getDatagramSocket();
        this.group = store.getMulticastAddress();
        this.port = store.getMulticastPort();
        this.message = message.toString();
    }

    @Override
    public void run() {
        DatagramPacket packet;
        byte[] sndBuffer;

        System.out.println("Sending multicast message to IP-" + group.getHostAddress() + " Port-" + port);

        sndBuffer = message.getBytes();
        packet = new DatagramPacket(sndBuffer, sndBuffer.length, group, port);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            System.out.println("Could not send message.");
            e.printStackTrace();
        }
    }
}
