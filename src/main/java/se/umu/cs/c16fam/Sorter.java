package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class Sorter {
    private int upload_limit = 16000000;
    private Registry serverRegistry;
    private Registry dataRegistry;
    private int limitSort = 0;

    public Sorter() {
        System.err.println("Making local client");
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

    public Sorter(int uLimit, String serverHost, int serverPort, String
            dataHost, int dataPort, String limit) {
        System.err.println("making client: " + uLimit + " " + serverHost + " " +
                "" + serverPort + " " + dataHost + " " + dataPort);
        try {
            serverRegistry = LocateRegistry.getRegistry(serverHost, serverPort);
            dataRegistry = LocateRegistry.getRegistry(dataHost, dataPort);
            upload_limit = uLimit;
            switch (limit) {
                case "quick":
                    limitSort = 1;
                    break;
                case "radix":
                    limitSort = 2;
                    break;
            }
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
            //Get data
            System.err.println("Retrieving data...");
            ArrayList<Integer> data = dataProvider.getData();
            if (data.isEmpty()) {
                System.err.println("Got empty data");
                System.exit(1);
            }
            System.err.println("Got data");

            //Sort data
            DynSort dSort = new DynSort();
            switch (limitSort) {
                case 1:
                    System.err.println("Using quick sort");
                    DynSort.quickSort(data);
                    break;
                case 2:
                    System.err.println("Using radix sort");
                    dSort.radixSort(data);
                    break;
                default:
                    System.err.println("Using dynamic sort");
                    dSort.dynamicSort(data);
            }

            //send data to server
            int id = -1;
            boolean done = false;
            int currInd = 0;
            int nLoops = 1;
            while (!done) {
                ArrayList<Integer> partList = new ArrayList<>();
                if (data.size() - currInd > upload_limit) {
                    System.err.println("Sending limit");
                    for (int i = currInd; i < upload_limit*nLoops; i++) {
                        partList.add(data.get(i));
                        currInd++;
                    }
                }
                else {
                    System.err.println("Sending final");
                    for (int i = currInd; i < data.size(); i++) {
                        partList.add(data.get(i));
                        currInd++;
                    }
                    done = true;
                }

                if (!partList.isEmpty())
                if (id == -1)
                    id = server.uploadData(-1,partList,done);
                else
                    server.uploadData(id,partList,done);
                nLoops++;
            }
        }
        catch (Exception e) {
            System.err.println("RMI error (client): " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
