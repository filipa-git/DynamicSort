package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        //Check if server or client
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
                    System.err.println("Valid arguments: {server " +
                            "<nSorters> <cacheLimit> <dataHost> " +
                            "<dataPort>}|{client " +
                            "[<nSorters> <cacheLimit> <serverHost> " +
                            "<serverPort> <dataHost> " +
                            "<dataPort>]}|{data <cacheLimit> " +
                            "<numberOfSorters>}");
                }
            }
            //Create client if first arg is "client"
            else if (args[0].equals("client")) {
                if (args.length == 1)
                    new Sorter();
                else if (args.length == 7) {
                    try {
                        int nSorters = Integer.parseInt(args[1]);
                        int cache = Integer.parseInt(args[2]);
                        int uLimit = cache/(nSorters+1);
                        int serverPort = Integer.parseInt(args[4]);
                        int dataPort = Integer.parseInt(args[6]);
                        new Sorter(uLimit, args[1], serverPort, args[3],
                                dataPort);
                    }
                    catch (NumberFormatException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
                else {
                    System.err.println("Valid arguments: {server " +
                            "<nSorters> <cacheLimit> <dataHost> " +
                            "<dataPort>}|{client " +
                            "[<nSorters> <cacheLimit> <serverHost> " +
                            "<serverPort> <dataHost> " +
                            "<dataPort>]}|{data <cacheLimit> " +
                            "<numberOfSorters>}");
                }
            }
            else if (args[0].equals("data")) {
                if (args.length < 3) {
                    System.err.println("Valid arguments: {server " +
                            "<nSorters> <cacheLimit> <dataHost> " +
                            "<dataPort>}|{client " +
                            "[<nSorters> <cacheLimit> <serverHost> " +
                            "<serverPort> <dataHost> " +
                            "<dataPort>]}|{data <cacheLimit> " +
                            "<numberOfSorters>}");
                    System.exit(1);
                }
                try {
                    int cacheSize = Integer.parseInt(args[1]);
                    int nSorters = Integer.parseInt(args[2]);

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
                            while (!done || !resQ.isEmpty()) {
                                res = resQ.take();
                                if (!res.isEmpty()) {
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
                            case "rand":
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
                                break;
                            default:
                                done = true;
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
                System.err.println("Usage: DynSort server|client [server] " +
                        "[port]");
            }
        }

	    /*Integer[] listA = new Integer[]{1,2,3,4,5,6,7,8,9,10};
        Integer[] listB = new Integer[]{10,9,8,7,6,5,4,3,2,1};
        Integer[] listC = new Integer[]{31,54,81,59,50,9,9,395,338,3};

        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        DynSort.insertionSort(list);
        System.out.println("Insertion sort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        DynSort.quickSort(list);
        System.out.println("Quicksort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        DynSort.radixSort(list);
        System.out.println("Radix sort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        DynSort.mergeSort(list);
        System.out.println("Merge sort: " + list.toString());*/
    }
}
