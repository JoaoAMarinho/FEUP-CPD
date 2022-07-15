package utils;

import java.io.IOException;
import java.net.*;

public class DatagramUtils {
    public static DatagramSocket createDatagram(InetAddress group, int port, NetworkInterface netIf) {
        DatagramSocket s;
        try {
            s = new DatagramSocket(null);

            s.setReuseAddress(true);
            s.bind(new InetSocketAddress(port));

            s.joinGroup(new InetSocketAddress(group, 0), netIf);

        } catch (Exception e) {
            System.err.println("Failure to join multicast group " + group + ":" + port);
            System.err.println(e);
            s = null;
        }

        return s;
    }

    public static void closeDatagram(DatagramSocket datagramSocket, InetSocketAddress socketAddress, NetworkInterface networkInterface) {
        try {
            datagramSocket.leaveGroup(socketAddress, networkInterface);
        } catch (IOException e) {
            System.out.println("Error to close the datagram socket.");
            e.printStackTrace();
        }
    }

    public static NetworkInterface createNetworkInterface(String netIfStr) {
        try {
            return NetworkInterface.getByName(netIfStr);
        } catch (SocketException e) {
            System.err.println("Invalid network interface name.");
            e.printStackTrace();
        }
        return null;
    }

    public static String receiveMessage(DatagramSocket datagramSocket) {
        DatagramPacket datagramPacket;
        byte[] buffer = new byte[8092];

        datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(datagramPacket);
            return new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        } catch (IOException e) {
            System.out.println("Could not receive message.");
            e.printStackTrace();
        }
        return null;
    }
}
