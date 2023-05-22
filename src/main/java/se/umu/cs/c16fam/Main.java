package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {
        //Check if server or client
        if (args.length > 0) {
            //Create server if first arg is "server"
            if (args[0].equals("server")) {
                if (args.length == 4) {
                    try {
                        int expected = Integer.parseInt(args[1]);
                        int dataPort = Integer.parseInt(args[3]);
                        new Server(expected, args[2], dataPort);
                    }
                    catch (NumberFormatException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
                else {
                    System.err.println("Usage: DynSort {server " +
                            "<expected> <dataHost> <dataPort>}|{client " +
                            "[<serverHost> <serverPort> <dataHost> " +
                            "<dataPort>]}|data");
                }
            }
            //Create client if first arg is "client"
            else if (args[0].equals("client")) {
                if (args.length < 5)
                    new Sorter();
                else {
                    try {
                        int serverPort = Integer.parseInt(args[2]);
                        int dataPort = Integer.parseInt(args[4]);
                        new Sorter(args[1], serverPort, args[3], dataPort);
                    }
                    catch (NumberFormatException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                }
            }
            else if (args[0].equals("data")) {
                try {
                    BlockingQueue<LinkedList<Integer>> resQ = new
                            LinkedBlockingQueue<>();

                    //Create remote object and stub
                    DataProviderService data = new DataProviderServiceImpl
                            (resQ);
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
                            LinkedList<Integer> res;
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
                                        prev = i;
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
