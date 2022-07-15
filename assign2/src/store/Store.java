package store;

import communication.MessageSender;
import communication.message.FileTransferMessage;
import communication.message.LeaveMessage;
import communication.unicast.Unicast;
import interfaces.RMI;
import communication.MessageReceiver;
import utils.DatagramUtils;
import utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Store implements RMI {
    private final InetAddress storeAddress, multicastAddress;
    private final int storePort, multicastPort;

    private int membershipCounter;
    private final String membershipPath;

    private final String logsPath;
    private final String storagePath;

    private DatagramSocket datagramSocket;
    private Unicast unicast;
    private NetworkInterface networkInterface;

    private Thread msgReceiverThread;
    private MessageReceiver msgReceiver;
    private MessageSender msgSender;

    private final ClusterInfo clusterInfo;

    private Registry registry;

    private final int tcpClusterIncrement = 10;
    private final int tcpJoinIncrement = 11;

    public Store(String storeIp, int storePort, String multicastAddress, int multicastPort) throws Exception {
        this.storeAddress = InetAddress.getByName(storeIp);
        this.multicastAddress = InetAddress.getByName(multicastAddress);

        this.storePort = storePort;
        this.multicastPort = multicastPort;
        this.membershipPath = "\\src\\store\\data\\" + storeIp.replace(".","_")
                + "_" + storePort + "_membership_counter.txt";

        this.logsPath = "\\src\\store\\data\\" + storeIp.replace(".","_")
                + "_" + storePort + "_logs.txt";
        this.storagePath = "\\src\\store\\storage\\" + storeIp.replace(".","_")
                + "_" + storePort + "\\";

        this.unicast = new Unicast(this);

        setMembershipCounter();

        this.clusterInfo = new ClusterInfo(storeIp+":"+storePort, this.membershipCounter);

        setClusterInfo();
        FileUtils.CreateDir(storagePath);
        FileUtils.CreateDir(storagePath+"replication\\");
    }

    private void setMembershipCounter() throws IOException {
        try {
            var content = FileUtils.ReadFromFile(membershipPath, "\n");
            if (content.equals("")) throw new FileNotFoundException("Empty file found");
            this.membershipCounter = Integer.parseInt(content);
        } catch (FileNotFoundException e) {
            this.membershipCounter = 0;
            FileUtils.CreateFile(membershipPath, Integer.toString(membershipCounter));
        }
    }

    private void setMulticastSocket() {
        this.networkInterface = DatagramUtils.createNetworkInterface("loopback");
        this.datagramSocket = DatagramUtils.createDatagram(multicastAddress, multicastPort, networkInterface);
    }

    private void setClusterInfo() throws IOException {
        try {
            var content = FileUtils.ReadFromFile(logsPath, "\n");
            if (content.equals("")) return;
            this.clusterInfo.parseFile(content);
        } catch (FileNotFoundException e) {
            FileUtils.CreateFile(logsPath, "");
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Store <IP_mcast_addr> <IP_mcast_port> <node_id> <Store_port>");
            return;
        }

        String multicastAddress = args[0];
        int multicastPort = Integer.parseInt(args[1]);

        String nodeId = args[2];
        int storePort = Integer.parseInt(args[3]);

        try {
            Store store = new Store(nodeId, storePort, multicastAddress, multicastPort);
            var stub = (RMI) UnicastRemoteObject.exportObject(store, storePort);

            Registry registry;
            try {
                store.registry = LocateRegistry.createRegistry(storePort);
            } catch (Exception e) {
                store.registry = LocateRegistry.getRegistry();
            }

            store.registry.rebind(nodeId, stub);

            System.out.println("Store is ready!\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void incrementCounter() {
        this.membershipCounter++;
        try {
            FileUtils.UpdateFile(membershipPath, String.valueOf(membershipCounter));
        } catch (Exception e) {
            System.out.println("Update file error.");
            e.printStackTrace();
        }
    }

    private void setMessageReceiver() {
        msgReceiverThread = new Thread(msgReceiver);
        msgReceiverThread.start();
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public ClusterInfo getClusterInfo() {
        return clusterInfo;
    }

    public String getLogsPath() {
        return logsPath;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public InetAddress getMulticastAddress() {
        return multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public InetAddress getStoreAddress() {
        return storeAddress;
    }

    public String getStoreId() {
        return storeAddress.getHostAddress() + ":" + storePort;
    }

    public int getStorePort() {
        return storePort;
    }

    public int getMembershipCounter() {
        return membershipCounter;
    }

    public MessageSender getMsgSender() {
        return msgSender;
    }

    public MessageReceiver getMsgReceiver() {
        return msgReceiver;
    }

    @Override
    public void join() throws RemoteException {
        System.out.println("Received join rmi.");
        if (membershipCounter % 2 != 0) {
            System.out.println("Invalid 'Join' operation.");
            return;
        }
        this.msgReceiver = new MessageReceiver(this);
        this.msgSender = new MessageSender(this);

        incrementCounter();
        setMulticastSocket();

        unicast.startConnections(storePort+tcpClusterIncrement);
        var messages = unicast.initialConnectionHandler(storePort+tcpJoinIncrement);

        for (String message : messages) {
            System.out.println("Received tcp message:");
            System.out.println(message + "\n");
            msgReceiver.parseMessage(message, null);
        }

        setMessageReceiver();
    }

    @Override
    public void leave() throws RemoteException {
        System.out.println("Received leave rmi.");
        if (membershipCounter % 2 == 0) {
            System.out.println("Invalid 'Leave' operation.");
            return;
        }
        incrementCounter();

        var leaveMsg = new LeaveMessage(getStoreId(), membershipCounter);
        msgSender.executeMulticastSend(leaveMsg);

        var nextStore = clusterInfo.getNextStore(getStoreId());

        if (nextStore != null) {
            List<String> files = new ArrayList<>();
            try {
                files = FileUtils.GetFolderSortedFiles(storagePath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Could not read files from '"+ storagePath + "'.");
            }

            for (var file : files) {
                if (file == null) continue;

                var path = storagePath + file + ".txt";
                String content;

                try {
                    content = FileUtils.ReadFromFile(path, "\n");
                    if (content.equals("")) {
                        System.out.println("Empty file.");
                        continue;
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not found.");
                    e.printStackTrace();
                    continue;
                }

                var msg = new FileTransferMessage(file, content);
                var storeInfo = nextStore.split(":");
                msgSender.executeUnicastSend(msg,
                        new InetSocketAddress(storeInfo[0], Integer.parseInt(storeInfo[1])+tcpClusterIncrement));
            }
        }

        msgReceiver.cancelMembershipTimeoutService();
        msgReceiver.getScheduler().shutdown();

        msgSender.getService().shutdown();
        msgReceiver.getService().shutdown();

        if (msgReceiverThread != null) {
            msgReceiverThread.interrupt();
        }

        DatagramUtils.closeDatagram(datagramSocket, new InetSocketAddress(multicastAddress, multicastPort),
                networkInterface);

        while(!msgSender.getService().isTerminated()) {
            try { //Wait for half a second
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Unicast.closeChannel(unicast.getChannel());

        FileUtils.DeleteDirectory(storagePath);
        FileUtils.CreateDir(storagePath);
        FileUtils.CreateDir(storagePath+"replication\\");
    }


}
