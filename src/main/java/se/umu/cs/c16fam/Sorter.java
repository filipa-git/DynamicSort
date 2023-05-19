package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class Sorter {
    Registry registry;

    public Sorter() {
        try {
            registry = LocateRegistry.getRegistry();
            execute();
        }
        catch (RemoteException e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Sorter(String host, int port) {
        try {
            registry = LocateRegistry.getRegistry(host, port);
            execute();
        }
        catch (RemoteException e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Do sorter stuff
     */
    private void execute() {
        try {
            DataService server = (DataService) registry.lookup("DataService");
            ArrayList<Integer> data = server.getData();

            System.out.println("Got data " + data.toString());
            dynSort.quickSort(data);
            System.out.println("Quicksort: " + data.toString());
        }
        catch (Exception e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
