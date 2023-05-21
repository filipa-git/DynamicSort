package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: filip
 * @since: 2023-05-20.
 */
public class DataProviderServiceImpl implements DataProviderService {
    private final int LIST_SIZE = 1000;
    private final int MAX_VAL = 16000000;
    private Integer[] listA = new Integer[]{31,54,81,59,50,9,9,395,338,3};
    private Integer[] listB = new Integer[]{28,67,88,50,3,107,52,395,909,1};
    private ConcurrentLinkedQueue<ArrayList<Integer>> q = new ConcurrentLinkedQueue<>();
    private ArrayList<Integer> bigList = new ArrayList<>();
    private ArrayList<Integer> resList = new ArrayList<>();

    public DataProviderServiceImpl() {
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i=0; i<LIST_SIZE; i++)
        {
            Integer r = rand.nextInt(MAX_VAL);
            bigList.add(r);
        }
        q.add(bigList);
    }

    @Override
    public void uploadData(ArrayList<Integer> data) throws RemoteException {
        int prev = -1;
        boolean sorted = true;
        for (int i:
             data) {
            if (prev > i)
                sorted = false;
        }
        System.err.println("Got data, it was " + (sorted ? "" : "not") +
                "sorted");
        resList.addAll(data);
        System.err.println("Total size: " + resList.size());
    }

    @Override
    public ArrayList<Integer> getData() throws RemoteException {
        return q.remove();
    }
}
