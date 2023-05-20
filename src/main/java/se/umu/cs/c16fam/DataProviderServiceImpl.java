package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: filip
 * @since: 2023-05-20.
 */
public class DataProviderServiceImpl implements DataProviderService {
    private Integer[] listA = new Integer[]{31,54,81,59,50,9,9,395,338,3};
    private Integer[] listB = new Integer[]{28,67,88,50,3,107,52,395,909,1};
    private ConcurrentLinkedQueue<ArrayList<Integer>> q = new ConcurrentLinkedQueue<>();

    public DataProviderServiceImpl() {
        q.add(new ArrayList<>(Arrays.asList(listA)));
        q.add(new ArrayList<>(Arrays.asList(listB)));
    }

    @Override
    public void uploadData(ArrayList<Integer> data) throws RemoteException {
        System.err.println("Got data " + data.toString());
    }

    @Override
    public ArrayList<Integer> getData() throws RemoteException {
        return q.remove();
    }
}
