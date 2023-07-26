package se.umu.cs.c16fam;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main class for running the program. Starts a client, server or data
 * provider depending on the input arguments.
 * @author filipa-git
 * @since 2023-05-07
 */
public class Main {
    private static String ARG_MESS = "Valid arguments: {server " +
            "<nSorters> <cacheLimit> <dataHost> " +
            "<dataPort>}|{client " +
            "[<nSorters> <cacheLimit> <serverHost> " +
            "<serverPort> <dataHost> " +
            "<dataPort> [quick|radix]]}|{data <cacheLimit> " +
            "<numberOfSorters>}";

    /**
     * Start host program (client, server or data provider)
     * @param args Arguments for host, refer to ARG_MESS above for format
     */
    public static void main(String[] args) {
        //Parse input arguments
        if (args.length > 0) {
            //Create server if first arg is "server"
            if (args[0].equals("server")) {
                if (args.length == 5) {
                    try {
                        int expected = Integer.parseInt(args[1]);
                        int cache = Integer.parseInt(args[2]);
                        int dataPort = Integer.parseInt(args[4]);
                        new Server(expected, cache, args[3], dataPort);
                    }
                    catch (NumberFormatException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
                else {
                    System.err.println(ARG_MESS);
                }
            }
            //Create client if first arg is "client"
            else if (args[0].equals("client")) {
                if (args.length == 1)
                    new Sorter();
                else if (args.length >= 7) {
                    try {
                        int nSorters = Integer.parseInt(args[1]);
                        int cache = Integer.parseInt(args[2]);
                        int uLimit = cache/(nSorters+1);
                        int serverPort = Integer.parseInt(args[4]);
                        int dataPort = Integer.parseInt(args[6]);
                        String limit = "dynamic";
                        if (args.length >= 8)
                            limit = args[7];

                        new Sorter(uLimit, args[3], serverPort, args[5],
                                dataPort, limit);
                    }
                    catch (NumberFormatException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
                else {
                    System.err.println(ARG_MESS);
                }
            }
            //Create data provider if first arg is "data"
            else if (args[0].equals("data")) {
                if (args.length < 3) {
                    System.err.println(ARG_MESS);
                    System.exit(1);
                }
                try {
                    int nSorters = Integer.parseInt(args[1]);
                    int cacheSize = Integer.parseInt(args[2]);

                    BlockingQueue<ArrayList<Integer>> resQ = new
                            LinkedBlockingQueue<>();

                    //Create remote object and stub
                    DataProviderService data = new DataProviderServiceImpl
                            (resQ, cacheSize, nSorters);
                    DataProviderService stub = (DataProviderService)
                            UnicastRemoteObject.exportObject
                                    (data, 0);

                    //Create registry and bind stub
                    Registry registry = LocateRegistry.createRegistry(1099);
                    registry.rebind("DataProviderService", stub);
                    System.err.println("Data provider ready");

                    //Create control thread
                    Runnable upload = () -> {
                        try {
                            ArrayList<Integer> res;
                            int prev;
                            boolean sorted = true, done = false;
                            int tSize = 0;
                            //check if done or queue has objects
                            while (!done || !resQ.isEmpty()) {
                                res = resQ.take();
                                if (!res.isEmpty()) {
                                    //check if sorted
                                    prev = -1;
                                    for (int i :
                                            res) {
                                        if (prev > i)
                                            sorted = false;
                                        tSize++;
                                    }
                                }
                                else {
                                    done = true;
                                }
                            }
                            System.err.println("Thread done, data of size " +
                                            tSize + " was " + (sorted?"":"not") + "sorted");
                        }
                        catch (Exception e) {
                            System.err.println("Error (thread): " + e.getMessage());
                            e.printStackTrace();
                        }
                    };

                    //Read input
                    Scanner inLine = new Scanner(System.in);
                    Thread cThread;
                    long sTime;
                    long eTime;
                    boolean done = false;
                    String cmd;
                    while (!done) {
                        cmd = inLine.nextLine();
                        switch (cmd){
                            case "done":
                                done = true;
                                break;
                            default:
                                //Start control thread
                                cThread = new Thread(upload);
                                cThread.start();
                                //Start test
                                sTime = ((DataProviderServiceImpl) data)
                                        .initData(cmd);
                                System.err.println("Start time: " + sTime);
                                //Wait for control thread (test is done)
                                cThread.join();
                                //Get end time
                                eTime = ((DataProviderServiceImpl) data)
                                        .getEndTime();
                                System.err.println("End time: " + eTime);
                                System.err.println("Time diff: " +
                                        (eTime-sTime));
                        }
                    }
                    System.err.println("Data provider done");
                }
                catch (NumberFormatException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                catch (Exception e) {
                    System.err.println("Error (data): " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                System.err.println(ARG_MESS);
            }
        }
    }
}
