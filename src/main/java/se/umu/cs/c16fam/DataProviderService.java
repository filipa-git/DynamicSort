package se.umu.cs.c16fam;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author: filip
 * @since: 2023-05-20.
 */
public interface DataProviderService extends Remote {
    void uploadData(LinkedList<Integer> data) throws RemoteException;
    LinkedList<Integer> getData() throws RemoteException;
}
