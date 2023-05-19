package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class Server {
    public Server() {
        try {
            //Create remote object and stub
            DataService server = new DataServiceImpl();
            DataService stub = (DataService) UnicastRemoteObject.exportObject
                    (server, 0);

            //Create registry and bind stub
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("DataService", stub);

            //Generate data
            System.err.println("Server ready");
        }
        catch (RemoteException e) {
            System.err.println("RMI error (server): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
