package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {
    void join() throws RemoteException;

    void leave() throws RemoteException;
}
