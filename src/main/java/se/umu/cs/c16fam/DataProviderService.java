package se.umu.cs.c16fam;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Remote class for data provider service
 * @author filipa-git
 * @since 2023-05-20.
 */
public interface DataProviderService extends Remote {
    void uploadData(ArrayList<Integer> data) throws RemoteException;
    ArrayList<Integer> getData() throws RemoteException;
}
