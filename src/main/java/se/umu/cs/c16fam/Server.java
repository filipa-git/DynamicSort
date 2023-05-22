package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class Server {
    private boolean done = false;
    private ReentrantLock doneLock = new ReentrantLock();
    private BlockingQueue<ArrayList<Integer>> out = new
            LinkedBlockingQueue<>();

    public Server(int expected, int cache, String dataProviderName, int
            dataProviderPort) {
        try {
            //Create remote object and stub
            DataService server = new DataServiceImpl(out, expected, cache);
            DataService stub = (DataService) UnicastRemoteObject.exportObject
                    (server, 0);

            //Create registry and bind stub
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("DataService", stub);

            //Create upload thread
            Runnable upload = () -> {
                try {
                    Registry dataRegistry = LocateRegistry.getRegistry
                            (dataProviderName, dataProviderPort);
                    DataProviderService provider = (DataProviderService)
                            dataRegistry.lookup
                            ("DataProviderService");

                    ArrayList<Integer> data;
                    while (!done || !out.isEmpty()) {
                        data = out.take();
                        if (!data.isEmpty()) {
                            provider.uploadData(data);
                            System.err.println("Uploaded data");
                        }
                    }
                    System.err.println("Thread done");
                }
                catch (Exception e) {
                    System.err.println("Error (thread): " + e.getMessage());
                    e.printStackTrace();
                }
            };
            Thread uThread = new Thread(upload);
            uThread.start();

            //Sort data
            System.err.println("Server ready");
            ((DataServiceImpl)server).sortData();

            //stop thread
            done = true;
            uThread.join();
            System.err.println("Server done");
        }
        catch (Exception e) {
            System.err.println("Error (server): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
