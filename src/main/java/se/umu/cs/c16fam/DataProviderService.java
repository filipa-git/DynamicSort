package se.umu.cs.c16fam;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author: filip
 * @since: 2023-05-20.
 */
public interface DataProviderService extends Remote {
    void uploadData(ArrayList<Integer> data) throws RemoteException;
    ArrayList<Integer> getData() throws RemoteException;
}
