package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class Sorter {
    private final int UPLOAD_LIMIT = 16000000;
    private Registry serverRegistry;
    private Registry dataRegistry;

    public Sorter() {
        try {
            serverRegistry = LocateRegistry.getRegistry();
            dataRegistry = serverRegistry;
            execute();
        }
        catch (RemoteException e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Sorter(String serverHost, int serverPort, String dataHost, int
            dataPort) {
        try {
            serverRegistry = LocateRegistry.getRegistry(serverHost, serverPort);
            dataRegistry = LocateRegistry.getRegistry(dataHost, dataPort);
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
            DataService server = (DataService) serverRegistry.lookup("DataService");
            DataProviderService dataProvider = (DataProviderService)
                    dataRegistry.lookup("DataProviderService");
            LinkedList<Integer> data = dataProvider.getData();
            if (data.isEmpty()) {
                System.err.println("Got empty data");
                System.exit(1);
            }
            System.err.println("Got data");

            //Sort data
            DynSort.dynamicSort(data);

            //send data to server
            int id = -1;
            boolean done = false;
            System.err.println("Sending data of size " + data.size());
            while (!done) {
                LinkedList<Integer> partList = new LinkedList<>();
                if (data.size() > UPLOAD_LIMIT) {
                    for (int i = 0; i < UPLOAD_LIMIT; i++) {
                        partList.add(data.poll());
                    }
                }
                else {
                    for (int i = 0; i < data.size(); i++) {
                        partList.add(data.poll());
                    }
                    done = true;
                }
                if (id == -1)
                    id = server.uploadData(-1,partList,done);
                else
                    server.uploadData(id,partList,done);
            }
        }
        catch (Exception e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
