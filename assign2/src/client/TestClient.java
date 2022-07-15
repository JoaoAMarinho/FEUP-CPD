package client;

import communication.message.DeleteMessage;
import communication.message.GetMessage;
import communication.message.Message;
import communication.message.PutMessage;
import interfaces.RMI;
import org.json.JSONObject;
import utils.FileUtils;
import utils.HashUtils;
import utils.UnicastUtils;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public class TestClient {
    private final RMI stub;

    public TestClient(RMI stub) {
        this.stub = stub;
    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: java TestClient <node_ap> <operation> [<opnd>]");
            return;
        }

        var nodeInfo = args[0].split(":");
        String storeIp = nodeInfo[0];
        int storePort = Integer.parseInt(nodeInfo[1]);

        String operation = args[1];

        try {
            Registry registry = LocateRegistry.getRegistry(storePort);
            RMI stub = (RMI) registry.lookup(storeIp);

            TestClient testClient = new TestClient(stub);

            System.out.println("Sending request to Store!");

            switch (operation) {
                case "JOIN" -> testClient.join();
                case "LEAVE" -> testClient.leave();
                case "PUT" -> testClient.put(storeIp, storePort, args[2]);
                case "GET" -> {
                    var content = testClient.get(storeIp, storePort, args[2]);
                    System.out.println(content);
                }
                case "DELETE" -> testClient.delete(storeIp, storePort, args[2]);
                default -> System.out.println("Invalid Operation!");
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }

    private void join() throws RemoteException {
        stub.join();
    }

    private void leave() throws RemoteException {
        stub.leave();
    }

    private void put(String storeIp, int storePort, String filepath) throws NoSuchAlgorithmException {
        var path = "\\src\\store\\storage\\" + filepath;
        String content;

        try {
            content = FileUtils.ReadFromFile(path, "\n");
            if (content.equals("")) {
                System.out.println("Empty file.");
                return;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
            return;
        }

        var key = HashUtils.generateHash(content);
        var msg = new PutMessage(key, content);

        UnicastUtils.sendUnicastMessage(msg, new InetSocketAddress(storeIp, storePort+10));

        System.out.println(key);
    }

    private String get(String storeIp, int storePort, String key) {
        var msg = new GetMessage(key);
        String response;

        try {
            response = UnicastUtils.readWriteUnicastMessage(msg, new InetSocketAddress(storeIp, storePort+10));
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        JSONObject jsonResponse = new JSONObject(response);
        switch ((String)jsonResponse.get("header")) {
            case "CONTENT" -> {
                return (String) jsonResponse.get("value");
            }
            case "REDIRECT" -> {
                return get((String) jsonResponse.get("ip"), jsonResponse.getInt("port"), key);
            }
            case "FILENOTFOUND" -> System.err.println("File '" + key + "' was not found!");
            case "DELETED" -> System.err.println("File '" + key + "' was deleted!");
            default -> throw new IllegalStateException("Unexpected value: " + jsonResponse.get("header"));
        }

        return "";
    }

    private void delete(String storeIp, int storePort, String key) {
        var msg = new DeleteMessage(key);

        UnicastUtils.sendUnicastMessage(msg, new InetSocketAddress(storeIp, storePort+10));
    }
}
